package com.example.sensoradapt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sensoradapt.ui.theme.SensorAdaptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SensorAdaptTheme {
                CompassScreen()
            }
        }
    }
}