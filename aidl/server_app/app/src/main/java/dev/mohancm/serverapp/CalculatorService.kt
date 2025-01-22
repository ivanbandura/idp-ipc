package dev.mohancm.serverapp

import android.app.Service
import android.content.Intent
import android.os.IBinder

class CalculatorService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private val binder = object : ICalculatorService.Stub() {
        override fun add(a: Int, b: Int): Int = a + b
        override fun subtract(a: Int, b: Int): Int = a - b
    }

}