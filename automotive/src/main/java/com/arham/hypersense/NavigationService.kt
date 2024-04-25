package com.arham.hypersense

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.CountDownTimer
import android.telephony.SmsManager
import androidx.car.app.CarAppService
import androidx.car.app.CarToast
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import androidx.core.app.ActivityCompat.requestPermissions
import kotlin.math.abs
import kotlin.math.sqrt
import androidx.car.app.Screen

class NavigationService : CarAppService(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var accelerationThreshold = 15.0f // Adjust this threshold as needed
    private var permissionTimer: CountDownTimer? = null
    override fun createHostValidator(): HostValidator {
         return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        return NavigationSession()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val acceleration = calculateAcceleration(it)
                if (abs(acceleration) > accelerationThreshold) {
                    AccidentSession()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }
    private fun calculateAcceleration(event: SensorEvent): Float {
        val alpha = 0.8f // Adjust this filter factor if needed
        val gravity = FloatArray(3)
        val linearAcceleration = FloatArray(3)

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        linearAcceleration[0] = event.values[0] - gravity[0]
        linearAcceleration[1] = event.values[1] - gravity[1]
        linearAcceleration[2] = event.values[2] - gravity[2]

        // Calculate magnitude of linear acceleration vector

        return sqrt(
            (linearAcceleration[0] * linearAcceleration[0] +
                    linearAcceleration[1] * linearAcceleration[1] +
                    linearAcceleration[2] * linearAcceleration[2]).toDouble()
        ).toFloat()
    }
}
