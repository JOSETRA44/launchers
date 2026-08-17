package com.tien.tensor.data.arsenal

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.tien.tensor.data.bluetooth.GpuKeyspaceBenchmark
import com.tien.tensor.domain.model.Finding
import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.model.Severity
import com.tien.tensor.domain.port.SecurityModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.ConcurrentHashMap

/**
 * Bluetooth security reconnaissance + passkey-strength analysis.
 *
 * Read-only, defensive audit of the user's *own* Bluetooth environment:
 *  1. Adapter state and discoverability (a discoverable adapter is an exposure).
 *  2. Bonded (paired) devices — classified by type and bond strength.
 *  3. A short passive BLE scan of nearby advertisers with signal strength.
 *  4. A GPU-accelerated brute-force benchmark of the BLE *Legacy* pairing
 *     passkey space (10^6), run on the user's own GPU, to demonstrate why
 *     Legacy "Just Works"/passkey pairing is cryptographically weak.
 *
 * Android does not let an app inject pairing attempts against third-party
 * devices, and this module never tries to — it only ever reads local state and
 * benchmarks local hardware. Scan/connect APIs need runtime Bluetooth
 * permissions (API 31+) or location (pre-31); the module degrades gracefully
 * when they are absent, exactly like the WiFi recon module.
 */
class BluetoothScannerModule(private val context: Context) : SecurityModule {

    override val meta = ModuleMeta(
        id          = "bt_recon",
        name        = "BLUETOOTH RECON",
        tagline     = "Paired · near-field · GPU passkey benchmark",
        isStreaming = false
    )

    override fun observe(): Flow<ModuleReport> = flow {
        val findings = mutableListOf<Finding>()
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = manager?.adapter

        if (adapter == null) {
            emit(ModuleReport(meta.id, "NO BLUETOOTH HARDWARE",
                listOf(Finding("bt_none", "UNSUPPORTED", "This device has no Bluetooth adapter.", Severity.INFO)),
                System.currentTimeMillis()))
            return@flow
        }

        var bonded = 0
        var nearby = 0

        if (!adapter.isEnabled) {
            findings += Finding("bt_off", "ADAPTER OFF",
                "Bluetooth is disabled — smallest attack surface. Enable it for full recon.", Severity.INFO)
        } else {
            // ── Discoverability ─────────────────────────────────────────────────
            if (adapter.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                findings += Finding("bt_disco", "DISCOVERABLE",
                    "Adapter is broadcasting and visible to any nearby device — pairing surface is open.",
                    Severity.MEDIUM)
            }

            // ── Bonded (paired) devices ─────────────────────────────────────────
            val paired = try { adapter.bondedDevices.orEmpty() } catch (_: SecurityException) { emptySet() }
            bonded = paired.size
            paired.forEach { device -> classifyBonded(device)?.let { findings += it } }
            if (bonded > 0) findings += Finding("bt_bonded", "PAIRED DEVICES",
                "$bonded bonded · ${paired.count { it.type == BluetoothDevice.DEVICE_TYPE_LE }} BLE · " +
                    "${paired.count { it.type == BluetoothDevice.DEVICE_TYPE_CLASSIC }} classic",
                Severity.INFO)

            // ── Near-field BLE scan ─────────────────────────────────────────────
            val scanned = scanBle(adapter)
            nearby = scanned.size
            if (scanned.isNotEmpty()) {
                val strongest = scanned.maxByOrNull { it.rssi }
                findings += Finding("bt_scan", "ADVERTISERS IN RANGE",
                    "$nearby BLE device(s) · strongest ${strongest?.rssi} dBm ${bars(strongest?.rssi ?: -127)}",
                    Severity.INFO)
                scanned.filter { it.rssi > -55 }.take(3).forEach {
                    findings += Finding("bt_close_${it.address}", "VERY CLOSE ADVERTISER",
                        "${it.label} · ${it.rssi} dBm — within arm's reach", Severity.LOW)
                }
            } else if (!scanPermitted()) {
                findings += Finding("bt_scan_perm", "SCAN UNAVAILABLE",
                    "Grant Nearby devices (Android 12+) or Location permission to enumerate advertisers.",
                    Severity.INFO)
            }
        }

        // ── GPU passkey brute-force benchmark ───────────────────────────────────
        findings += benchmarkFinding()

        val hdr = "BT ${if (adapter.isEnabled) "ON" else "OFF"} · $bonded PAIRED · $nearby NEARBY"
        emit(ModuleReport(meta.id, hdr, findings, System.currentTimeMillis()))
    }.flowOn(Dispatchers.IO)

    // ── Device risk classification ──────────────────────────────────────────────

    /**
     * Classifies a bonded device into a [Finding], or null when nothing is
     * noteworthy. This is deliberately a small, self-contained judgement:
     * which device classes and bond states are worth flagging is a defensible
     * design choice, so it lives in one readable place.
     */
    private fun classifyBonded(device: BluetoothDevice): Finding? {
        val name = try { device.name } catch (_: SecurityException) { null } ?: "(unnamed)"
        val major = try { device.bluetoothClass?.majorDeviceClass } catch (_: SecurityException) { null }
        val isInput = major == BluetoothClass.Device.Major.PERIPHERAL
        val isAudio = major == BluetoothClass.Device.Major.AUDIO_VIDEO

        return when {
            // Input peripherals (keyboards/mice) can inject keystrokes if hijacked.
            isInput -> Finding("bt_hid_${device.address}", "INPUT PERIPHERAL PAIRED",
                "\"$name\" is a HID device — a hijacked link can inject keystrokes.", Severity.LOW)
            // Unnamed bonds are stale/opaque and worth review.
            name == "(unnamed)" -> Finding("bt_anon_${device.address}", "OPAQUE BOND",
                "A paired device exposes no name (${device.address}) — consider un-pairing if unknown.",
                Severity.LOW)
            isAudio -> null // benign, common
            else -> null
        }
    }

    // ── Passive BLE scan (time-boxed) ───────────────────────────────────────────

    private data class ScanRow(val address: String, val label: String, val rssi: Int)

    private suspend fun scanBle(adapter: BluetoothAdapter): List<ScanRow> {
        val scanner = try { adapter.bluetoothLeScanner } catch (_: SecurityException) { null } ?: return emptyList()
        val rows = ConcurrentHashMap<String, ScanRow>()
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val dev = result.device ?: return
                val label = try { dev.name } catch (_: SecurityException) { null }
                    ?: result.scanRecord?.deviceName ?: dev.address
                rows[dev.address] = ScanRow(dev.address, label, result.rssi)
            }
        }
        return try {
            scanner.startScan(callback)
            delay(SCAN_WINDOW_MS)
            scanner.stopScan(callback)
            rows.values.sortedByDescending { it.rssi }
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun scanPermitted(): Boolean = try {
        val p = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            "android.permission.BLUETOOTH_SCAN" else "android.permission.ACCESS_FINE_LOCATION"
        context.checkSelfPermission(p) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } catch (_: Exception) { false }

    // ── GPU benchmark → finding ─────────────────────────────────────────────────

    private suspend fun benchmarkFinding(): Finding {
        val r = GpuKeyspaceBenchmark().run()
        val verified = if (r.recoveredPasskey != null) "✓ passkey recovered" else "no match"
        val detail = buildString {
            append("${r.engine}\n")
            append("Exhausted ${"%,d".format(r.keyspace)} BLE Legacy passkeys in ${r.elapsedMs} ms ")
            append("(${"%,d".format(r.keysPerSec)} keys/s, $verified).\n")
            append("A 6-digit passkey has only 10^6 values — trivially brute-forced offline. ")
            append("This is why BLE Legacy / \"Just Works\" pairing is considered broken; prefer LE Secure Connections.")
            r.note?.let { append("\n$it") }
        }
        return Finding(
            id = "bt_bench",
            title = if (r.usedGpu) "GPU PASSKEY BENCHMARK" else "CPU PASSKEY BENCHMARK",
            detail = detail,
            severity = Severity.MEDIUM
        )
    }

    private fun bars(rssi: Int): String {
        val level = when {
            rssi >= -55 -> 4; rssi >= -67 -> 3; rssi >= -80 -> 2; rssi >= -90 -> 1; else -> 0
        }
        return "█".repeat(level) + "░".repeat(4 - level)
    }

    private companion object { const val SCAN_WINDOW_MS = 4_000L }
}
