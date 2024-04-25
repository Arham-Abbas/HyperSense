package com.arham.hypersense

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class AccidentSession : Session() {
    override fun onCreateScreen(intent: Intent) : Screen {
        return CarAccident(carContext)
    }
}