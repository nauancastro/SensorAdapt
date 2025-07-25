package com.example.sensoradapt


import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SensorAdaptScreen(viewModel: SensorAdaptViewModel = viewModel()) {
    val lux by viewModel.lux.collectAsState()
    val battery by viewModel.batteryPct.collectAsState()
    val hour by viewModel.hour.collectAsState()
    val networkType by viewModel.networkType.collectAsState()
    val isUFCQuixada by viewModel.isUFCQuixada.collectAsState()
    val context = LocalContext.current

    // Permissão de localização (só pede 1x)
    var askedLocationPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.checkIfUFCQuixada()
        }
    }

    // Solicita permissão ao abrir a tela
    LaunchedEffect(Unit) {
        if (!askedLocationPermission) {
            askedLocationPermission = true
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Atualiza localização sempre que entrar na tela ou rede mudar
    LaunchedEffect(networkType) {
        viewModel.checkIfUFCQuixada()
    }

    // Lógica de tema
    val (isDark, reason) = remember(lux, battery, hour) {
        when {
            battery <= 20 -> true to "Modo escuro ativado para economizar bateria (${battery}%)"
            hour >= 18 || hour < 6 -> {
                if (lux < 30f)
                    true to "Modo escuro por horário (após 18h/${hour}h) e ambiente escuro"
                else
                    false to "Ambiente claro, mesmo após 18h (${hour}h)"
            }
            lux < 15f -> true to "Modo escuro por pouca luz ambiente (${lux.format(1)} lux)"
            else -> false to "Modo claro (luz ambiente boa)"
        }
    }

    MaterialTheme(colorScheme = if (isDark) darkColorScheme() else lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Banner de localização personalizada
                if (isUFCQuixada) {
                    Banner(
                        msg = "Você está na UFC Quixadá! Interface personalizada ativada.",
                        isError = false
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Banner de conexão
                when (networkType) {
                    NetworkType.NONE -> Banner("Sem conexão com a Internet", isError = true)
                    NetworkType.WIFI -> Banner("Conectado ao Wi-Fi", isError = false)
                    NetworkType.CELLULAR -> Banner("Usando dados móveis", isError = false)
                    else -> {}
                }
                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (isDark) "🌙 Modo Escuro Ativo" else "☀️ Modo Claro Ativo",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(24.dp))
                Text("• Luminosidade: ${lux.format(1)} lux")
                Text("• Nível de bateria: $battery%")
                Text("• Hora atual: ${hour}h")
                Spacer(Modifier.height(12.dp))
                Text("• Motivo: $reason", style = MaterialTheme.typography.bodyLarge)

                Spacer(Modifier.height(32.dp))

                // Botão online só se tiver conexão
                if (networkType != NetworkType.NONE) {
                    Button(onClick = { /* ação online */ }) {
                        Text("Botão Online")
                    }
                }
            }
        }
    }
}

// Banner reusável
@Composable
fun Banner(msg: String, isError: Boolean) {
    Surface(
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            msg,
            modifier = Modifier.padding(12.dp),
            color = if (isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

// Extensão para formatar float
fun Float.format(digits: Int) = "%.${digits}f".format(this)
