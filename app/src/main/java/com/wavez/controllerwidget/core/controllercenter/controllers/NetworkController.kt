package com.wavez.controllerwidget.core.controllercenter.controllers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    var onWifiStateChanged: ((Boolean) -> Unit)? = null
    var onAirplaneModeChanged: ((Boolean) -> Unit)? = null
    var onMobileDataStateChanged: ((Boolean) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                    onWifiStateChanged?.invoke(state == WifiManager.WIFI_STATE_ENABLED)
                }
                Intent.ACTION_AIRPLANE_MODE_CHANGED ->
                    onAirplaneModeChanged?.invoke(isAirplaneModeOn())
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network)  {
            Log.d("ControlCenter", "NetworkController: network available")
            onMobileDataStateChanged?.invoke(isMobileDataEnabled())
        }
        override fun onLost(network: Network) {
            Log.d("ControlCenter", "NetworkController: network lost")
            onMobileDataStateChanged?.invoke(isMobileDataEnabled())
        }
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            onMobileDataStateChanged?.invoke(isMobileDataEnabled())
        }
    }

    init {
        try {
            context.registerReceiver(
                receiver,
                IntentFilter().apply {
                    addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                    addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                }
            )
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),
                networkCallback
            )
        } catch (e: Exception) {
            Log.e("ControlCenter", "NetworkController: init failed", e)
        }
    }

    @Suppress("DEPRECATION")
    fun toggleWifi() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
            } else {
                context.startActivity(
                    Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        } catch (e: Exception) {
            Log.e("ControlCenter", "NetworkController: toggleWifi failed", e)
        }
    }

    fun isWifiEnabled(): Boolean = try { wifiManager.isWifiEnabled }
    catch (e: Exception) { Log.e("ControlCenter", "NetworkController: isWifiEnabled failed", e); false }

    fun toggleAirplaneMode(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Log.e("ControlCenter", "NetworkController: toggleAirplaneMode failed", e)
        }
    }

    fun openMobileDataSettings() {
        try {
            context.startActivity(
                Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (inner: Exception) {
                Log.e("ControlCenter", "NetworkController: openMobileDataSettings failed", inner)
            }
        }
    }

    fun isAirplaneModeOn(): Boolean = try {
        Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    } catch (e: Exception) { Log.e("ControlCenter", "NetworkController: isAirplaneModeOn failed", e); false }

    fun isMobileDataEnabled(): Boolean = try {
        val net = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(net) ?: return false
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    } catch (e: Exception) { Log.e("ControlCenter", "NetworkController: isMobileDataEnabled failed", e); false }

    fun release() {
        try {
            context.unregisterReceiver(receiver)
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e("ControlCenter", "NetworkController: release failed", e)
        }
    }
}
