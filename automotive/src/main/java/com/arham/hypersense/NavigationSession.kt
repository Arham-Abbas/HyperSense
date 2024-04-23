package com.arham.hypersense

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.arham.hypersense.screen.MainScreen

class NavigationSession : Session() {
    override fun onCreateScreen(intent: Intent) : Screen {
        return MainScreen(carContext)
    }
}