/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package com.onetabb.template.android.core.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.net.NetworkRequest
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean

class NetworkTracker(private val context: Context, private val lifecycle: Lifecycle) : DefaultLifecycleObserver {

    companion object {
        var connected = AtomicBoolean(false)
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var connectivityManager: ConnectivityManager? = null
    private val networkAvailableState: MutableStateFlow<Boolean> = MutableStateFlow(true)

    val isConnectedFlow: StateFlow<Boolean> get() = networkAvailableState

    init {
        lifecycle.addObserver(this)
        connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        networkAvailableState.value = checkInitialNetworkState()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        registerNetworkCallback()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        unregisterNetworkCallback()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        lifecycle.removeObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        val isConnected = checkInitialNetworkState()
        networkAvailableState.tryEmit(isConnected)
    }

    private fun registerNetworkCallback() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            unregisterNetworkCallback()

            networkCallback = createNetworkCallback()

            val networkRequest = NetworkRequest.Builder().addCapability(NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()

            networkCallback?.let { callback ->
                try {
                    connectivityManager?.registerNetworkCallback(networkRequest, callback)
                } catch (e: Exception) {
                    networkAvailableState.tryEmit(checkInitialNetworkState())
                }
            }
        }
    }

    private fun unregisterNetworkCallback() {
        networkCallback?.let { callback ->
            try {
                connectivityManager?.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            networkCallback = null
        }
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            connectivityManager?.getNetworkCapabilities(network)?.let {
                if (it.hasCapability(NET_CAPABILITY_INTERNET)) {
                    connected.set(true)
                    networkAvailableState.tryEmit(true)
                }
            }
        }

        override fun onLost(network: Network) {
            connected.set(false)
            networkAvailableState.tryEmit(false)
        }

        override fun onUnavailable() {
            connected.set(false)
            networkAvailableState.tryEmit(false)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities,
        ) {
            super.onCapabilitiesChanged(network, capabilities)
            if (capabilities.hasCapability(NET_CAPABILITY_VALIDATED)) {
                connected.set(true)
                networkAvailableState.tryEmit(true)
            } else {
                connected.set(false)
                networkAvailableState.tryEmit(false)
            }
        }
    }

    private fun checkInitialNetworkState(): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET) && networkCapabilities.hasCapability(
            NET_CAPABILITY_VALIDATED,
        )
    }
}
