package com.uade.vigia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uade.vigia.data.ConnectionStatus
import com.uade.vigia.ui.UiConnectionStatus
import com.uade.vigia.ui.VigiaViewModel
import com.uade.vigia.ui.theme.*

@Composable
fun DashboardScreen(viewModel: VigiaViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val telemetry = uiState.telemetry
    
    // Si hay desconexión, toda la UI pasa a una escala de grises visual
    val isDisconnected = uiState.connectionStatus == UiConnectionStatus.SIN_CONEXION
    val tintColor = if (isDisconnected) GrayDisconnected else SurfaceWhite

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        // Cabecera
        HeaderSection(
            connectionStatus = uiState.connectionStatus,
            lastUpdateTime = uiState.lastUpdateTime,
            batteryLevel = telemetry.battery.level
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Contenido Principal (Mapa + Órdenes)
        Row(modifier = Modifier.weight(1f)) {
            // Columna Izquierda: Mapa de Recorrido
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                    .background(tintColor, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Recorrido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Componente interactivo MapView
                    MapView(mission = telemetry.mission, modifier = Modifier.fillMaxSize())
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            // Columna Derecha: Panel de Órdenes
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .background(tintColor, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                CommandsPanel(
                    viewModel = viewModel,
                    isDisconnected = isDisconnected
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Panel Inferior: Telemetría Rápida (Ej: Escolta E-0142)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(tintColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            val mission = telemetry.mission
            if (mission != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Misión: ${mission.id} · Estado: ${mission.status}", fontWeight = FontWeight.Bold)
                        Text("Punto actual: ${mission.currentPoint} · Distancia visitante: ${telemetry.serviceVariables.visitorDistanceM} m", color = TextSecondary)
                    }
                    Text("Progreso: ${mission.progressPercent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BlueEscorting)
                }
            } else {
                Text("Detalles de la Misión (Sin misión activa)", color = TextSecondary)
            }
        }
    }
}

@Composable
fun HeaderSection(connectionStatus: UiConnectionStatus, lastUpdateTime: String, batteryLevel: Int) {
    val statusColor = when(connectionStatus) {
        UiConnectionStatus.EN_LINEA -> GreenAvailable
        UiConnectionStatus.DATO_ANTIGUO -> AmberWaiting
        UiConnectionStatus.SIN_CONEXION -> RedStopped
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("G1 · GO-01", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Control de acceso · Centro logístico", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Badge(containerColor = statusColor) { 
                Text(connectionStatus.name.replace("_", " "), modifier = Modifier.padding(4.dp)) 
            }
            Spacer(modifier = Modifier.width(8.dp))
            Badge(containerColor = if (batteryLevel > 15) GreenAvailable else RedStopped) { 
                Text("Batería $batteryLevel%", modifier = Modifier.padding(4.dp)) 
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(lastUpdateTime, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
fun CommandsPanel(viewModel: VigiaViewModel, isDisconnected: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Órdenes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.onStartMission("Visitante", "Oficina Central") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDisconnected
        ) {
            Text("Iniciar Escolta")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.onPauseMission() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDisconnected
        ) {
            Text("Pausar")
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { viewModel.onConfirmCrossing() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDisconnected
        ) {
            Text("Cruce libre · continuar")
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { viewModel.onCancelMission() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDisconnected
        ) {
            Text("Cancelar escolta")
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Botón de Detención de Emergencia
        Button(
            onClick = { viewModel.onEmergencyStop() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RedStopped),
            enabled = !isDisconnected
        ) {
            Text("Detener · mantener 2 s", color = Color.White)
        }
    }
}
