package com.tien.tensor.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tien.tensor.domain.model.AppUsageStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists raw launch events in DataStore (JSON via org.json — no extra deps).
 * Computes AppUsageStats on the fly using a 24-hour half-life decay score.
 *
 * Auto-pruning on every write:
 *   - Drops events older than MAX_AGE_MS (30 days)
 *   - Caps history at MAX_EVENTS (500) keeping the most recent
 */
class AppUsageDataSource(private val dataStore: DataStore<Preferences>) {

    private val eventsKey = stringPreferencesKey("launch_events_v1")

    val usageStats: Flow<List<AppUsageStats>> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> computeStats(parseEvents(prefs[eventsKey] ?: "[]")) }

    suspend fun trackLaunch(packageName: String) {
        dataStore.edit { prefs ->
            val events = parseEvents(prefs[eventsKey] ?: "[]").toMutableList()
            events += RawEvent(packageName, System.currentTimeMillis())
            prefs[eventsKey] = serialize(prune(events))
        }
    }

    suspend fun clearAllHistory() {
        dataStore.edit { prefs -> prefs[eventsKey] = "[]" }
    }

    // ── Internal model ───────────────────────────────────────────────────────

    private data class RawEvent(val pkg: String, val ts: Long)

    // ── Pruning ──────────────────────────────────────────────────────────────

    private fun prune(events: MutableList<RawEvent>): List<RawEvent> {
        val cutoff = System.currentTimeMillis() - MAX_AGE_MS
        return events.filter { it.ts > cutoff }.takeLast(MAX_EVENTS)
    }

    // ── Score computation ────────────────────────────────────────────────────

    /**
     * Decay-frequency score: score = Σ 1/(1 + age_hours/24)
     * - Event from 1 h ago  → contributes ≈ 0.96
     * - Event from 24 h ago → contributes  = 0.50
     * - Event from 7 d ago  → contributes ≈ 0.13
     */
    private fun computeStats(events: List<RawEvent>): List<AppUsageStats> {
        val now = System.currentTimeMillis()
        return events
            .groupBy { it.pkg }
            .map { (pkg, pkgEvents) ->
                val score = pkgEvents.sumOf { e ->
                    1.0 / (1.0 + (now - e.ts) / 3_600_000.0 / 24.0)
                }
                AppUsageStats(
                    packageName  = pkg,
                    launchCount  = pkgEvents.size,
                    lastLaunchAt = pkgEvents.maxOf { it.ts },
                    score        = score
                )
            }
    }

    // ── JSON serialization (org.json — Android built-in) ────────────────────

    private fun parseEvents(json: String): List<RawEvent> = runCatching {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            RawEvent(o.getString("p"), o.getLong("t"))
        }
    }.getOrDefault(emptyList())

    private fun serialize(events: List<RawEvent>): String {
        val arr = JSONArray()
        events.forEach { e ->
            arr.put(JSONObject().apply { put("p", e.pkg); put("t", e.ts) })
        }
        return arr.toString()
    }

    private companion object {
        const val MAX_EVENTS = 500
        val MAX_AGE_MS = 30L * 24 * 3_600_000
    }
}
