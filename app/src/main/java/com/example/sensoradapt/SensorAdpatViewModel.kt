package com.example.sensoradpat

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.Calendar
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class NetworkType {NONE, WIFI, CELLULAR, OTHER}

class SensorAdpatViewModel(
    app: Application
) : AndroidViewModel(app), SensorEventListener {

    private val sensorManager = app.getSystemService(
        Context.SENSOR_SERVICE
    ) as SensorManager

    private val lightSensor = sensorManager.getDefaultSensor(
        Sensor.TYPE_LIGHT
    )

    private val _lux = MutableStateFlow(100)
    val lux: StateFlow<Int> = _lux


    private val _batteryPct = MutableStateFlow(100)
    val batteryPct: StateFlow<Int> = _batteryPct


    private val _hour = MutableStateFlow(
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    )
    val hour : StateFlow<Int> = _hour

    private val _networkType = MutableStateFlow(NetworkType.NONE)
    val networkType: StateFlow<NetworkType> = _networkType

    private val _isUFCQuixada = MutableStateFlow(false)
    val isUFCQuixada: StateFlow<Boolean> = _isUFCQuixada

    init {

    }

    private fun updateBattery(){
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = getApplication<Application>(
        ).registerReceiver(null, intentFilter)
        val level = batteryStatus?.getIntExtra("level", -1) ?: -1
        val scale = batteryStatus?.getIntExtra("scale", -1) ?: -1
        val pct = if(level >= 0 && scale > 0) ((level * 100) / scale) else 100
        _batteryPct.value = pct
    }

    private fun updateHour(){
        val cal = Calendar.getInstance()
        _hour.value = cal.get(Calendar.HOUR_OF_DAY)
    }

    fun updateNetworkType(){
        val cm = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val active = cm.activeNetwork ?: run{
            _networkType.value = NetworkType.NONE
            return
        }

        val caps = cm.getNetworkCapabilities(active) ?: run{
            _networkType.value = NetworkType.NONE
            return
        }


        _networkType.value = when{
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            else -> NetworkType.OTHER
        }

    }

    fun checkIfUFCQuixada() {
        val lm = getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val perm = ContextCompat.checkSelfPermission(getApplication(), android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (perm != PackageManager.PERMISSION_GRANTED) {
            _isUFCQuixada.value = false
            return
        }

        val location: Location? = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        location?.let {
            val LAT = -4.967697
            val LON = -39.016145
            val dist = FloatArray(1)
            Location.distanceBetween(it.latitude, it.longitude, LAT, LON, dist)
            _isUFCQuixada.value = dist[0] < 200.0 // 200 metros de raio
        } ?: run {
            _isUFCQuixada.value = false
        }
    }



    override fun onSensorChanged(event: SensorEvent?) {
        event?.let{ _lux.value = it.values[0].toInt()}
        updateBattery()
        updateHour()
        updateNetworkType()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}