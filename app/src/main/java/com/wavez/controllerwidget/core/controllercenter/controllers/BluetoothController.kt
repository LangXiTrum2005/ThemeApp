package com.wavez.controllerwidget.core.controllercenter.controllers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BluetoothController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val adapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    var onStateChanged: ((Boolean) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                onStateChanged?.invoke(state == BluetoothAdapter.STATE_ON)
            }
        }
    }

    init {
        try {
            context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        } catch (e: Exception) {
            Log.e("ControlCenter", "BluetoothController: init failed", e)
        }
    }

    fun toggleBluetooth() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29+: direct toggle bị restrict, mở panel thay thế
                openBluetoothPanel()
            } else {
                // API 28 only: trực tiếp toggle, cần BLUETOOTH_ADMIN permission
                toggleDirectly()
            }
        } catch (e: Exception) {
            Log.e("ControlCenter", "BluetoothController: toggleBluetooth failed", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun openBluetoothPanel() {
        context.startActivity(
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    @Suppress("DEPRECATION")
    private fun toggleDirectly() {
        val a = adapter ?: return
        if (a.isEnabled) a.disable() else a.enable()
    }

    fun isEnabled(): Boolean = try {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        adapter?.isEnabled == true
    } catch (e: Exception) {
        Log.e("ControlCenter", "BluetoothController: isEnabled failed", e)
        false
    }

    fun release() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.e("ControlCenter", "BluetoothController: release failed", e)
        }
    }
}
