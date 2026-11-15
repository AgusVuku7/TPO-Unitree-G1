package com.uade.vigia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.uade.vigia.data.Alert
import com.uade.vigia.data.AlertType
import com.uade.vigia.ui.VigiaViewModel
import com.uade.vigia.ui.theme.*

@Composable
fun AlertsScreen(viewModel: VigiaViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val alerts = uiState.telemetry.activeAlerts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        Text("Alertas Activas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin alertas activas.", color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(alerts) { alert ->
                    AlertCard(alert = alert, onAcknowledge = { viewModel.onAcknowledgeAlert(alert.id) })
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: Alert, onAcknowledge: () -> Unit) {
    val alertColor = if (alert.type == AlertType.BATERIA_BAJA) RedStopped else AmberWaiting

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), // Fondo amarillento cálido
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(alertColor, RoundedCornerShape(6.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(alert.type.name.replace("_", " "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(alert.message, style = MaterialTheme.typography.bodyMedium)
            }
            
            Button(
                onClick = onAcknowledge,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceWhite, contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Text("Reconocer")
            }
        }
    }
}
