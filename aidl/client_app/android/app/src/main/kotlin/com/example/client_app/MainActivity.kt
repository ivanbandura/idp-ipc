package com.example.client_app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import dev.mohancm.serverapp.ICalculatorService

class MainActivity: FlutterActivity() {
    private var calculatorService: ICalculatorService? = null
    private val CHANNEL = "com.example.clientapp/calculator"

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Use the generated Stub class
            calculatorService = ICalculatorService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            calculatorService = null
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "add" -> {
                        try {
                            val a = call.argument<Int>("a") ?: 0
                            val b = call.argument<Int>("b") ?: 0
                            val sum = calculatorService?.add(a, b)
                            result.success(sum)
                        } catch (e: Exception) {
                            result.error("CALC_ERROR", "Failed to perform addition", e.message)
                        }
                    }
                    "subtract" -> {
                        try {
                            val a = call.argument<Int>("a") ?: 0
                            val b = call.argument<Int>("b") ?: 0
                            val difference = calculatorService?.subtract(a, b)
                            result.success(difference)
                        } catch (e: Exception) {
                            result.error("CALC_ERROR", "Failed to perform subtraction", e.message)
                        }
                    }
                    else -> result.notImplemented()
                }
            }

        // Bind to the calculator service
        val intent = Intent("dev.mohancm.serverapp.CALCULATOR_SERVICE")
        intent.setPackage("dev.mohancm.serverapp")
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }
}
