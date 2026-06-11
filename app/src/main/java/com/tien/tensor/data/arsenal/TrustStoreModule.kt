package com.tien.tensor.data.arsenal

import com.tien.tensor.domain.model.Finding
import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.model.Severity
import com.tien.tensor.domain.port.SecurityModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.security.KeyStore
import java.security.cert.X509Certificate

/**
 * Audits the Android CA trust store ("AndroidCAStore"). A user-installed CA
 * certificate lets whoever holds its key transparently intercept TLS traffic
 * (corporate proxies, debugging tools — or an attacker). Read-only.
 */
class TrustStoreModule : SecurityModule {

    override val meta = ModuleMeta(
        id          = "trust_store",
        name        = "TRUST STORE",
        tagline     = "User-installed CA certificates · TLS MITM surface",
        isStreaming = false
    )

    override fun observe(): Flow<ModuleReport> =
        flow { emit(scan()) }.flowOn(Dispatchers.Default)

    private fun scan(): ModuleReport {
        val findings   = mutableListOf<Finding>()
        var systemCount = 0
        var userCount   = 0

        try {
            val ks = KeyStore.getInstance("AndroidCAStore").apply { load(null) }
            for (alias in ks.aliases()) {
                when {
                    alias.startsWith("system:") -> systemCount++
                    alias.startsWith("user:") -> {
                        userCount++
                        val subject = (ks.getCertificate(alias) as? X509Certificate)
                            ?.subjectX500Principal?.name?.take(80) ?: alias
                        findings += Finding(
                            "ca_$alias", "USER CA INSTALLED",
                            "$subject — this authority can issue certificates your device will trust. If you did not install it deliberately, remove it (Settings → Security → Credentials).",
                            Severity.HIGH
                        )
                    }
                }
            }
        } catch (e: Exception) {
            findings += Finding("ca_error", "TRUST STORE UNREADABLE", "Could not enumerate the CA store: ${e.message}", Severity.LOW)
        }

        if (userCount == 0 && findings.isEmpty()) {
            findings += Finding(
                "ca_clean", "NO USER CAS",
                "Only the $systemCount system-trusted authorities are present — no third-party TLS interception surface.",
                Severity.INFO
            )
        }
        findings += Finding("ca_sys", "SYSTEM AUTHORITIES", "$systemCount system CAs in the trust store.", Severity.INFO)

        return ModuleReport(
            moduleId  = meta.id,
            headline  = "$systemCount SYSTEM CAS · $userCount USER CAS${if (userCount > 0) " !" else ""}",
            findings  = findings.sortedByDescending { it.severity.ordinal },
            updatedAt = System.currentTimeMillis()
        )
    }
}
