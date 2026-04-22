package com.cat.catpiano.music.core.extensions

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.cat.catpiano.music.core.utils.DataLocal
import com.cat.catpiano.music.core.utils.state.HandleState



fun Context.initNetworkMonitor() {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            DataLocal.isConnectInternet.postValue(true)
        }
        override fun onLost(network: Network) {
            DataLocal.isConnectInternet.postValue(false)
        }
    }
    val request = NetworkRequest.Builder().build()
    connectivityManager.registerNetworkCallback(request, networkCallback)
}

