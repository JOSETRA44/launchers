package com.tien.tensor.data.arsenal

import com.tien.tensor.domain.model.Finding
import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.model.Severity
import com.tien.tensor.domain.port.SecurityModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * TLS chain audit — connects to each target, completes the handshake,
 * extracts the leaf certificate, and reports expiry / issuer / protocol.
 *
 * No data is sent beyond the TLS handshake itself. All connections are
 * read-only; sockets are closed immediately after session inspection.
 * Requires the INTERNET permission.
 */
class SslInspectorModule : SecurityModule {

    override val meta = ModuleMeta(
        id          = "ssl_inspect",
        name        = "SSL INSPECTOR",
        tagline     = "TLS chain audit — expiry · protocol · issuer",
        isStreaming = false
    )

    private val TARGETS = listOf("google.com", "cloudflare.com", "github.com", "amazon.com")

    override fun observe(): Flow<ModuleReport> = channelFlow {
        send(ModuleReport(meta.id, "HANDSHAKING…", emptyList(), System.currentTimeMillis()))

        val findings = coroutineScope {
            TARGETS.map { host -> async(Dispatchers.IO) { probe(host) } }.awaitAll()
        }.flatten()

        val warnings = findings.count { it.severity >= Severity.MEDIUM }
        val headline = "${TARGETS.size} DOMAINS · $warnings WARNINGS"
        send(ModuleReport(meta.id, headline, findings, System.currentTimeMillis()))
    }.flowOn(Dispatchers.IO)

    private fun probe(host: String): List<Finding> {
        return try {
            val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
            val socket  = factory.createSocket() as SSLSocket
            socket.soTimeout = 6_000
            socket.connect(InetSocketAddress(host, 443), 6_000)
            socket.startHandshake()
            val session = socket.session
            val cert    = session.peerCertificates.firstOrNull() as? X509Certificate
            socket.close()

            if (cert == null) return listOf(
                Finding("ssl_${host}_nocert", host.uppercase(), "No leaf certificate in chain.", Severity.CRITICAL)
            )

            val daysLeft = (cert.notAfter.time - System.currentTimeMillis()) / 86_400_000L
            val issuer   = cert.issuerX500Principal.name
                .splitToSequence(",")
                .map { it.trim() }
                .firstOrNull { it.startsWith("O=") || it.startsWith("CN=") }
                ?.substringAfter("=") ?: "unknown"

            val proto    = session.protocol
            val cipher   = session.cipherSuite
            val sevCert  = when {
                daysLeft < 0  -> Severity.CRITICAL
                daysLeft < 14 -> Severity.HIGH
                daysLeft < 30 -> Severity.MEDIUM
                else          -> Severity.INFO
            }
            val sevProto = if (proto == "TLSv1" || proto == "TLSv1.1" || proto == "SSLv3")
                Severity.HIGH else Severity.INFO

            buildList {
                add(Finding("ssl_$host",
                    "${host.uppercase()} — ${daysLeft}d LEFT",
                    "issuer=$issuer  proto=$proto  cipher=${cipher.take(30)}",
                    maxOf(sevCert, sevProto)))
                if (sevProto >= Severity.HIGH)
                    add(Finding("ssl_${host}_proto",
                        "${host.uppercase()} — WEAK PROTOCOL",
                        "$proto is deprecated and insecure.",
                        sevProto))
            }
        } catch (e: Exception) {
            listOf(Finding(
                "ssl_${host}_err",
                "${host.uppercase()} — UNREACHABLE",
                "${e.javaClass.simpleName}: ${e.message?.take(80) ?: ""}",
                Severity.LOW
            ))
        }
    }
}
