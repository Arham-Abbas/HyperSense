package com.arham.hypersense

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
class CarAccident: AppCompatActivity(), SensorEventListener{
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var accelerationThreshold = 15.0f // Adjust this threshold as needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            textView.text = "Accelerometer sensor not available"
        }
    }
    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val acceleration = calculateAcceleration(it)
                textView.text = "Acceleration: $acceleration m/s²"
                if (Math.abs(acceleration) > accelerationThreshold) {
                    handleAccidentDetected()
                }
            }
        }
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
        val accelerationMagnitude = Math.sqrt(
            (linearAcceleration[0] * linearAcceleration[0] +
                    linearAcceleration[1] * linearAcceleration[1] +
                    linearAcceleration[2] * linearAcceleration[2]).toDouble()
        ).toFloat()

        return accelerationMagnitude
    }

    private fun handleAccidentDetected() {
        // Logic to handle accident detected
        textView.text = "Accident Detected!"
        sendSosAlertWithPermissionTimeout()
    }
    private fun sendSosAlertWithPermissionTimeout() {
        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            // If permission is already granted, proceed to send the SOS alert
            for (contact in emergencyContacts) {
                sendSms(contact, sosMessage)
            }
            showToast("SOS alert sent to emergency contacts!")
        } else {
            // Start timer to trigger SOS alert if permission is not granted within 5 minutes
            permissionTimer = object : CountDownTimer(PERMISSION_TIMEOUT_MS, 1000) {
                fun onTick(millisUntilFinished: Long) {
                    // Countdown timer tick
                }

                fun onFinish() {
                    // Timer expired, send SOS alert
                    for (contact in emergencyContacts) {
                        sendSms(contact, sosMessage)
                    }
                    showToast("Permission not granted within 5 minutes. SOS alert sent to emergency contacts!")
                }
            }.start()

            // Request the permission from the user
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS), REQUEST_SEND_SMS_PERMISSION)
        }
    }
    private fun sendSms(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        val sentIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), 0)
        val deliveredIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_DELIVERED"), 0)
        smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SEND_SMS_PERMISSION) {
            // Cancel the permission timeout timer
            permissionTimer?.cancel()

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to send the SOS alert
                for (contact in emergencyContacts) {
                    sendSms(contact, sosMessage)
                }
                showToast("SOS alert sent to emergency contacts!")
            } else {
                // Permission denied
                showToast("Permission denied. SOS alert cannot be sent.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_SEND_SMS_PERMISSION = 1001
    }
}



//<?xml version="1.0" encoding="utf-8"?>
//<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
//xmlns:tools="http://schemas.android.com/tools"
//android:layout_width="match_parent"
//android:layout_height="match_parent"
//android:padding="16dp"
//tools:context=".MainActivity">
//
//<TextView
//android:id="@+id/textView"
//android:layout_width="wrap_content"
//android:layout_height="wrap_content"
//android:text="Acceleration: 0 m/s²"
//android:textSize="18sp"
//android:textStyle="bold"
//android:layout_centerInParent="true"/>
//
//</RelativeLayout>
