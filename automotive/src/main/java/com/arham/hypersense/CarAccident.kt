package com.arham.hypersense

import android.content.pm.PackageManager
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.Manifest
import android.telephony.SmsManager
import android.app.PendingIntent
import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import kotlin.math.sqrt


class CarAccident(carContext: CarContext) : Screen(carContext) {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var accelerationThreshold = 15.0f // Adjust this threshold as needed
    private var permissionTimer: CountDownTimer? = null

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

    private fun handleAccidentDetected() {
        // Logic to handle accident detected
        showCarToast("Accident Detected!")
        sendSosAlertWithPermissionTimeout()
    }
    private fun sendSosAlertWithPermissionTimeout() {
        // If permission is already granted, proceed to send the SOS alert
        val emergencyContacts = listOf("100", "102")
        for (contact in emergencyContacts) {
            val sosMessage = "Help!I am in an emergency"
            sendSms(contact, sosMessage)
            showCarToast("SOS alert sent to emergency contacts!")
        }
    }
    private fun sendSms(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        val sentIntent = PendingIntent.getBroadcast(carContext, 0, Intent("SMS_SENT"), 0)
        val deliveredIntent = PendingIntent.getBroadcast(carContext, 0, Intent("SMS_DELIVERED"), 0)
        smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent)
    }

    private fun showCarToast(message: String) {
        CarToast.makeText(carContext, message, CarToast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_SEND_SMS_PERMISSION = 1001
    }

    override fun onGetTemplate(): Template {
        handleAccidentDetected()
        val row = Row.Builder()
            .setTitle("Accident Detected")
            .build()

        val pane = Pane.Builder()
            .addRow(row)
            .build()

        invalidate()
        return PaneTemplate.Builder(pane)
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}

