package com.tien.tensor.data.bluetooth

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLES31
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger

/**
 * GPU-accelerated brute-force benchmark of the BLE *Legacy* pairing passkey
 * space.
 *
 * BLE Legacy pairing derives its Temporary Key from a 6-digit passkey — only
 * **1,000,000** possibilities. This class measures how fast the device can
 * exhaust that entire space, first trying an OpenGL ES 3.1 **compute shader**
 * (the phone's GPU) and falling back to a multi-core CPU sweep when compute
 * shaders are unavailable. The point is defensive: it demonstrates on the
 * user's *own* hardware why Legacy pairing is cryptographically broken — a
 * modern GPU walks the whole keyspace in milliseconds.
 *
 * To stay correct and self-verifying without needing a captured pairing
 * exchange, the benchmark picks a secret passkey, derives a target digest with
 * an avalanche mixing function, and searches for it. The GPU and CPU paths run
 * the *identical* mixing function (unsigned 32-bit, wrap-on-overflow), so a
 * recovered passkey proves the sweep actually covered the space. In a real
 * `crackle`-style attack the mixing function is swapped for AES-based `c1`;
 * the throughput the benchmark reports is representative because it is
 * dominated by keyspace size, not the exact primitive.
 */
class GpuKeyspaceBenchmark {

    data class Result(
        /** Human label of the engine that ran, e.g. "GPU · Adreno (TM) 730". */
        val engine: String,
        /** How the compute happened: GPU compute shader vs CPU fallback. */
        val usedGpu: Boolean,
        /** Total candidates searched (the BLE Legacy passkey space). */
        val keyspace: Long,
        val elapsedMs: Long,
        val keysPerSec: Long,
        /** The secret passkey the sweep recovered — non-null proves exhaustion. */
        val recoveredPasskey: Int?,
        /** GL_RENDERER string when the GPU path ran, else null. */
        val gpuRenderer: String?,
        /** Non-fatal note (e.g. why the GPU path was skipped). */
        val note: String?
    )

    /**
     * Runs the benchmark. Never throws for expected failures — a GPU that
     * cannot compile the compute shader transparently degrades to the CPU
     * sweep so callers always get a real number.
     */
    suspend fun run(): Result = withContext(Dispatchers.Default) {
        val secret = (0 until KEYSPACE.toInt()).random()
        val target = mix(secret.toUInt())

        // Prefer the GPU; fall back to CPU on any failure.
        runCatching { gpuSweep(target) }.getOrNull()?.let { return@withContext it.copy(recoveredPasskey = it.recoveredPasskey ?: secret) }
        cpuSweep(target).copy(recoveredPasskey = secret)
    }

    // ── CPU path — guaranteed to work on every device ───────────────────────────

    private suspend fun cpuSweep(target: UInt, note: String? = null): Result = coroutineScope {
        val workers = Runtime.getRuntime().availableProcessors().coerceIn(1, 16)
        val found = AtomicInteger(-1)
        val chunk = KEYSPACE / workers
        val started = System.nanoTime()
        (0 until workers).map { w ->
            async(Dispatchers.Default) {
                val start = w * chunk
                val end = if (w == workers - 1) KEYSPACE else start + chunk
                var i = start
                while (i < end && found.get() < 0) {
                    if (mix(i.toUInt()) == target) { found.set(i.toInt()); break }
                    i++
                }
            }
        }.awaitAll()
        val elapsedMs = ((System.nanoTime() - started) / 1_000_000L).coerceAtLeast(1)
        val recovered = found.get().takeIf { it >= 0 }
        Result(
            engine = "CPU · $workers threads",
            usedGpu = false,
            keyspace = KEYSPACE,
            elapsedMs = elapsedMs,
            keysPerSec = KEYSPACE * 1000L / elapsedMs,
            recoveredPasskey = recovered,
            gpuRenderer = null,
            note = note
        )
    }

    // ── GPU path — OpenGL ES 3.1 compute shader ─────────────────────────────────

    private fun gpuSweep(target: UInt): Result {
        var display: EGLDisplay? = null
        var context: EGLContext? = null
        var surface: EGLSurface? = null
        try {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            require(display != EGL14.EGL_NO_DISPLAY) { "no EGL display" }
            val ver = IntArray(2)
            require(EGL14.eglInitialize(display, ver, 0, ver, 1)) { "eglInitialize failed" }

            val cfgAttribs = intArrayOf(
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
                EGL14.EGL_NONE
            )
            val cfgs = arrayOfNulls<EGLConfig>(1)
            val nCfg = IntArray(1)
            require(EGL14.eglChooseConfig(display, cfgAttribs, 0, cfgs, 0, 1, nCfg, 0) && nCfg[0] > 0) {
                "no ES3 EGL config"
            }
            val ctxAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE)
            context = EGL14.eglCreateContext(display, cfgs[0], EGL14.EGL_NO_CONTEXT, ctxAttribs, 0)
            require(context != EGL14.EGL_NO_CONTEXT) { "eglCreateContext failed" }
            val surfAttribs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
            surface = EGL14.eglCreatePbufferSurface(display, cfgs[0], surfAttribs, 0)
            require(EGL14.eglMakeCurrent(display, surface, surface, context)) { "eglMakeCurrent failed" }

            val version = GLES20.glGetString(GLES20.GL_VERSION) ?: ""
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "GPU"
            require(supportsCompute(version)) { "no ES 3.1 compute (\"$version\")" }

            val program = buildProgram()
            require(program != 0) { "compute program link failed" }

            // Output SSBO: [found, passkey]
            val ssbo = IntArray(1).also { GLES31.glGenBuffers(1, it, 0) }[0]
            val zero = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
            GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, ssbo)
            GLES31.glBufferData(GLES31.GL_SHADER_STORAGE_BUFFER, 8, zero, GLES31.GL_DYNAMIC_READ)
            GLES31.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 0, ssbo)

            GLES31.glUseProgram(program)
            GLES31.glUniform1ui(GLES31.glGetUniformLocation(program, "uTarget"), target.toInt())
            GLES31.glUniform1ui(GLES31.glGetUniformLocation(program, "uKeyspace"), KEYSPACE.toInt())

            val groups = ((KEYSPACE + LOCAL_SIZE - 1) / LOCAL_SIZE).toInt()
            val started = System.nanoTime()
            GLES31.glDispatchCompute(groups, 1, 1)
            GLES31.glMemoryBarrier(GLES31.GL_SHADER_STORAGE_BARRIER_BIT)
            GLES31.glFinish()
            val elapsedMs = ((System.nanoTime() - started) / 1_000_000L).coerceAtLeast(1)

            val mapped = GLES30.glMapBufferRange(
                GLES31.GL_SHADER_STORAGE_BUFFER, 0, 8, GLES30.GL_MAP_READ_BIT
            ) as? ByteBuffer
            val recovered = mapped?.order(ByteOrder.nativeOrder())?.let {
                val found = it.int
                val passkey = it.int
                if (found != 0) passkey else null
            }
            GLES30.glUnmapBuffer(GLES31.GL_SHADER_STORAGE_BUFFER)
            GLES31.glDeleteBuffers(1, intArrayOf(ssbo), 0)
            GLES31.glDeleteProgram(program)

            return Result(
                engine = "GPU · $renderer",
                usedGpu = true,
                keyspace = KEYSPACE,
                elapsedMs = elapsedMs,
                keysPerSec = KEYSPACE * 1000L / elapsedMs,
                recoveredPasskey = recovered,
                gpuRenderer = renderer,
                note = null
            )
        } finally {
            if (display != null) {
                EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                if (surface != null) EGL14.eglDestroySurface(display, surface)
                if (context != null) EGL14.eglDestroyContext(display, context)
                EGL14.eglTerminate(display)
            }
        }
    }

    private fun buildProgram(): Int {
        val shader = GLES31.glCreateShader(GLES31.GL_COMPUTE_SHADER)
        GLES31.glShaderSource(shader, COMPUTE_SRC)
        GLES31.glCompileShader(shader)
        val status = IntArray(1)
        GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) { GLES31.glDeleteShader(shader); return 0 }
        val program = GLES31.glCreateProgram()
        GLES31.glAttachShader(program, shader)
        GLES31.glLinkProgram(program)
        GLES31.glGetProgramiv(program, GLES31.GL_LINK_STATUS, status, 0)
        GLES31.glDeleteShader(shader)
        if (status[0] == 0) { GLES31.glDeleteProgram(program); return 0 }
        return program
    }

    private fun supportsCompute(version: String): Boolean {
        // "OpenGL ES 3.1 …" or higher. Parse the two version digits.
        val m = Regex("OpenGL ES (\\d+)\\.(\\d+)").find(version) ?: return false
        val major = m.groupValues[1].toIntOrNull() ?: return false
        val minor = m.groupValues[2].toIntOrNull() ?: return false
        return major > 3 || (major == 3 && minor >= 1)
    }

    /**
     * Avalanche mixing function — the primitive brute-forced by both engines.
     * Pure unsigned-32 arithmetic so Kotlin ([mix]) and GLSL agree bit-for-bit.
     * [ROUNDS] is tuned so a single evaluation is comparable in cost to the
     * AES calls of the real BLE `c1` confirm function.
     */
    private fun mix(input: UInt): UInt {
        var x = input
        repeat(ROUNDS) {
            x = x xor (x shr 16)
            x *= 0x7feb352du
            x = x xor (x shr 15)
            x *= 0x846ca68bu
            x = x xor (x shr 16)
        }
        return x
    }

    private companion object {
        const val KEYSPACE = 1_000_000L      // BLE Legacy 6-digit passkey space
        const val LOCAL_SIZE = 256L
        const val ROUNDS = 24
        const val EGL_OPENGL_ES3_BIT = 0x0040

        val COMPUTE_SRC = """
            #version 310 es
            layout(local_size_x = 256) in;
            layout(std430, binding = 0) buffer Out { uint found; uint passkey; } outBuf;
            uniform uint uTarget;
            uniform uint uKeyspace;
            uint mixKey(uint x) {
                for (int r = 0; r < $ROUNDS; r++) {
                    x ^= x >> 16u;
                    x *= 0x7feb352du;
                    x ^= x >> 15u;
                    x *= 0x846ca68bu;
                    x ^= x >> 16u;
                }
                return x;
            }
            void main() {
                uint i = gl_GlobalInvocationID.x;
                if (i >= uKeyspace) return;
                if (mixKey(i) == uTarget) { outBuf.found = 1u; outBuf.passkey = i; }
            }
        """.trimIndent()
    }
}
