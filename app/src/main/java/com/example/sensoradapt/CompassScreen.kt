package com.example.sensoradapt

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CompassScreen(sensorViewModel: SensorViewModel = viewModel()) {
    val rotation by sensorViewModel.rotation.observeAsState(0f)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Bússola",
            modifier = Modifier
                .size(200.dp)
                .rotate(rotation)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Direção: ${rotation.toInt()}°")
    }
}