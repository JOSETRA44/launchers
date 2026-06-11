package com.tien.tensor.data.source

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tien.tensor.domain.model.StepData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TYPE_STEP_COUNTER reports cumulative steps since boot. To show "steps today"
 * we persist a per-day baseline: on the first reading of each calendar day the
 * raw counter value is stored, and stepsToday = raw - baseline. A raw value
 * lower than the stored baseline means the device rebooted, so the baseline
 * resets to the current raw value.
 *
 * Requires ACTIVITY_RECOGNITION (runtime, API 29+) — without it the sensor
 * never fires and the flow only emits availability.
 */
class StepCounterDataSource(
    private val context: Context,
    private val dataStore: DataStore<Preferences>
) {

    private val dateKey = stringPreferencesKey("step_date")
    private val baseKey = longPreferencesKey("step_base")
    private val dayFmt  = SimpleDateFormat("yyyyMMdd", Locale.US)

    fun getSteps(): Flow<StepData> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (sensor == null) {
            trySend(StepData(stepsToday = 0, sensorAvailable = false))
            awaitClose { }
            return@callbackFlow
        }

        trySend(StepData(stepsToday = 0, sensorAvailable = true))

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val raw = event.values.firstOrNull()?.toLong() ?: return
                launch {
                    val prefs    = dataStore.data.first()
                    val today    = dayFmt.format(Date())
                    val savedDay = prefs[dateKey]
                    val savedBase = prefs[baseKey] ?: raw

                    val base = when {
                        savedDay != today -> raw  // new day: today starts at the current raw value
                        raw < savedBase   -> raw  // reboot: counter restarted from zero
                        else              -> savedBase
                    }
                    if (savedDay != today || base != savedBase) {
                        dataStore.edit { it[dateKey] = today; it[baseKey] = base }
                    }
                    trySend(StepData(stepsToday = (raw - base).toInt(), sensorAvailable = true))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
