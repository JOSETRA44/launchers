package com.tien.tensor.data.arsenal

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.tien.tensor.domain.model.Finding
import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.model.Severity
import com.tien.tensor.domain.port.SecurityModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * Live, read-only inspection of the active network path.
 *
 * Watches LinkProperties + NetworkCapabilities of the default network and
 * surfaces interception-relevant facts: an HTTP proxy on the link (possible
 * MITM), Private DNS (DoT) state, captive portals, unvalidated networks,
 * VPN tunnels, resolver addresses and local addressing. Passive only —
 * no probes are sent anywhere.
 */
class NetworkIntelModule(private val context: Context) : SecurityModule {

    override val meta = ModuleMeta(
        id          = "net_intel",
        name        = "NET INTEL",
        tagline     = "Live link inspection · proxy/MITM · DNS path",
        isStreaming = true
    )

    override fun observe(): Flow<ModuleReport> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun pushSnapshot(network: Network?) {
            val lp   = network?.let { cm.getLinkProperties(it) }
            val caps = network?.let { cm.getNetworkCapabilities(it) }
            trySend(buildReport(lp, caps))
        }
        pushSnapshot(cm.activeNetwork)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network)                                   = pushSnapshot(network)
            override fun onLinkPropertiesChanged(network: Network, lp: LinkProperties)   = pushSnapshot(network)
            override fun onCapabilitiesChanged(network: Network, nc: NetworkCapabilities) = pushSnapshot(network)
            override fun onLost(network: Network) { trySend(buildReport(null, null)) }
        }
        cm.registerDefaultNetworkCallback(callback)

        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.flowOn(Dispatchers.IO)

    private fun buildReport(lp: LinkProperties?, caps: NetworkCapabilities?): ModuleReport {
        val findings = mutableListOf<Finding>()

        if (lp == null && caps == null) {
            findings += Finding("net_offline", "NO DEFAULT NETWORK", "Device is offline — no link to inspect.", Severity.INFO)
            return ModuleReport(meta.id, "NO ACTIVE NETWORK", findings, System.currentTimeMillis())
        }

        // ── Interception surface ─────────────────────────────────────────────
        val proxy = lp?.httpProxy
        if (proxy != null) {
            findings += Finding(
                "net_proxy", "HTTP PROXY ON LINK",
                "Traffic routes through ${proxy.host}:${proxy.port} — verify you configured this; proxies can intercept traffic.",
                Severity.MEDIUM
            )
        }
        if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL) == true) {
            findings += Finding(
                "net_captive", "CAPTIVE PORTAL",
                "The network is redirecting traffic to a sign-in page. Avoid sensitive logins until validated.",
                Severity.MEDIUM
            )
        }
        if (caps != null && !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            findings += Finding(
                "net_unvalidated", "NETWORK NOT VALIDATED",
                "Android could not confirm internet reachability on this link.",
                Severity.LOW
            )
        }

        // ── DNS path ─────────────────────────────────────────────────────────
        if (lp != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (lp.isPrivateDnsActive) {
                findings += Finding(
                    "net_pdns", "PRIVATE DNS ACTIVE",
                    "DNS-over-TLS to ${lp.privateDnsServerName ?: "opportunistic resolver"} — queries are encrypted.",
                    Severity.INFO
                )
            } else {
                findings += Finding(
                    "net_pdns_off", "PRIVATE DNS DISABLED",
                    "DNS queries travel in cleartext; the network operator can observe every domain you resolve.",
                    Severity.LOW
                )
            }
        }
        lp?.dnsServers?.takeIf { it.isNotEmpty() }?.let { servers ->
            findings += Finding(
                "net_dns", "RESOLVERS",
                servers.joinToString(" · ") { it.hostAddress ?: it.toString() },
                Severity.INFO
            )
        }

        // ── Topology ─────────────────────────────────────────────────────────
        if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) {
            findings += Finding("net_vpn", "VPN TUNNEL ACTIVE", "All traffic is routed through a VPN interface.", Severity.INFO)
        }
        if (caps != null && !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
            findings += Finding("net_metered", "METERED LINK", "The system treats this connection as metered.", Severity.INFO)
        }
        lp?.linkAddresses?.takeIf { it.isNotEmpty() }?.let { addrs ->
            findings += Finding(
                "net_addr", "LOCAL ADDRESSING",
                addrs.joinToString(" · ") { it.toString() },
                Severity.INFO
            )
        }

        val iface = lp?.interfaceName ?: "?"
        val dnsCount = lp?.dnsServers?.size ?: 0
        return ModuleReport(
            moduleId  = meta.id,
            headline  = "IF $iface · ${lp?.linkAddresses?.size ?: 0} ADDR · $dnsCount DNS${if (proxy != null) " · PROXY!" else ""}",
            findings  = findings,
            updatedAt = System.currentTimeMillis()
        )
    }
}
