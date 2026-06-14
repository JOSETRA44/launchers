package com.tien.tensor.data.arsenal

import android.content.Context
import android.net.wifi.WifiManager
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
import java.net.Socket

/**
 * Active TCP probe of localhost and the local gateway (router).
 *
 * All port probes for a host run concurrently; both hosts are also
 * scanned in parallel. Each socket has a 500 ms connect timeout to
 * keep total scan time under ~2 s. Requires the INTERNET permission.
 *
 * Read-only: no data is sent after a successful connect — the socket
 * is closed immediately, confirming only whether the port accepted
 * the handshake (open) or refused / timed out (closed / filtered).
 */
class PortScanModule(private val context: Context) : SecurityModule {

    override val meta = ModuleMeta(
        id          = "port_scan",
        name        = "PORT SCAN",
        tagline     = "TCP probe — localhost & gateway · 18 ports",
        isStreaming = false
    )

    private val PORTS = mapOf(
        21 to "FTP",   22 to "SSH",    23 to "TELNET",  25  to "SMTP",
        53 to "DNS",   80 to "HTTP",   110 to "POP3",   143 to "IMAP",
        443 to "HTTPS", 445 to "SMB", 3306 to "MYSQL", 3389 to "RDP",
        5900 to "VNC", 8080 to "HTTP-X", 8443 to "HTTPS-X",
        6379 to "REDIS", 9200 to "ELASTIC", 27017 to "MONGO"
    )

    override fun observe(): Flow<ModuleReport> = channelFlow {
        send(ModuleReport(meta.id, "SCANNING…", emptyList(), System.currentTimeMillis()))

        @Suppress("DEPRECATION")
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val rawGw = wm.dhcpInfo.gateway
        val gateway = if (rawGw == 0) null else intToIp(rawGw)

        val targets = buildList {
            add("127.0.0.1" to "LOCALHOST")
            if (gateway != null) add(gateway to "GATEWAY")
        }

        val findings = coroutineScope {
            targets.map { (host, label) ->
                async {
                    val open = coroutineScope {
                        PORTS.entries.map { (port, svc) ->
                            async(Dispatchers.IO) {
                                try {
                                    Socket().use { s ->
                                        s.connect(InetSocketAddress(host, port), 500)
                                        "$port/$svc"
                                    }
                                } catch (_: Exception) { null }
                            }
                        }.awaitAll().filterNotNull()
                    }
                    buildFindings(label, host, open)
                }
            }.awaitAll().flatten()
        }

        val openCount = findings.count { it.id.endsWith("_open") }
        val portsOpen = findings.filter { it.id.endsWith("_open") }
            .sumOf { it.detail.split("  ").size }
        send(ModuleReport(
            meta.id,
            "${targets.size} HOSTS · $openCount FLAGGED · $portsOpen OPEN PORTS",
            findings,
            System.currentTimeMillis()
        ))
    }.flowOn(Dispatchers.IO)

    private fun buildFindings(label: String, host: String, open: List<String>): List<Finding> {
        if (open.isEmpty()) return listOf(
            Finding("ps_${host}_clean", "$label — ALL CLOSED", "No response on ${PORTS.size} probed ports.", Severity.INFO)
        )
        val dangerous = open.any { it.startsWith("23/") || it.startsWith("21/") }
        return listOf(
            Finding(
                "ps_${host}_open", "$label — ${open.size} OPEN",
                open.joinToString("  "),
                if (dangerous) Severity.HIGH else Severity.MEDIUM
            )
        )
    }

    private fun intToIp(i: Int) =
        "${i and 0xFF}.${(i shr 8) and 0xFF}.${(i shr 16) and 0xFF}.${(i shr 24) and 0xFF}"
}
