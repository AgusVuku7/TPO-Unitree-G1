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
import com.uade.vigia.data.RobotState
import com.uade.vigia.ui.VigiaViewModel
import com.uade.vigia.ui.theme.*

@Composable
fun ConnectionScreen(viewModel: VigiaViewModel, onConnectClicked: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val telemetry = uiState.telemetry
    
    val isAvailable = telemetry.connection.status == ConnectionStatus.EN_LINEA && 
                      telemetry.robotState == RobotState.EN_BASE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("Vigía", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text("Consola de operación · G1", color = TextSecondary)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Tarjeta de Selección de Robot
        Card(
            modifier = Modifier.width(600.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Elegí el robot a operar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                // Item de Robot (Simulador Interno)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("G1 · GO-01", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Control de acceso · Centro logístico", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Badge(containerColor = if (telemetry.connection.status == ConnectionStatus.EN_LINEA) GreenAvailable else RedStopped) { 
                                Text(telemetry.connection.status.name.replace("_", " "), modifier = Modifier.padding(4.dp)) 
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = GreenAvailable) { 
                                Text("Batería ${telemetry.battery.level}%", modifier = Modifier.padding(4.dp)) 
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = Color(0xFFE0E0E0)) { 
                                Text(telemetry.robotState.name.replace("_", " "), color = TextPrimary, modifier = Modifier.padding(4.dp)) 
                            }
                        }
                    }
                    
                    Button(
                        onClick = onConnectClicked,
                        enabled = isAvailable,
                        modifier = Modifier.width(140.dp)
                    ) {
                        Text(if (isAvailable) "Conectar" else "No disponible")
                    }
                }
            }
        }
    }
}
