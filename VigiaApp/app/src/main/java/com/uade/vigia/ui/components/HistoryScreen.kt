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
import com.uade.vigia.data.local.MissionWithEvents
import com.uade.vigia.ui.VigiaViewModel
import com.uade.vigia.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: VigiaViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val history = uiState.missionsHistory
    
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        Text("Historial de Misiones", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay misiones registradas aún.", color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(history) { missionWithEvents ->
                    MissionHistoryCard(missionWithEvents, timeFormat)
                }
            }
        }
    }
}

@Composable
fun MissionHistoryCard(data: MissionWithEvents, timeFormat: SimpleDateFormat) {
    val statusColor = when (data.mission.status) {
        "COMPLETADA" -> GreenAvailable
        "CANCELADA" -> AmberWaiting
        "ABORTADA" -> RedStopped
        else -> BlueEscorting
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(data.mission.id, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Badge(containerColor = statusColor) { 
                    Text(data.mission.status, modifier = Modifier.padding(4.dp)) 
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            val startTime = timeFormat.format(Date(data.mission.startTimestamp))
            val durationText = if (data.mission.durationFormatted.isNotEmpty()) " | Duración: ${data.mission.durationFormatted}" else ""
            Text("Inicio: $startTime$durationText | Eventos registrados: ${data.events.size}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            
            if (data.events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(8.dp))
                
                data.events.take(3).forEach { event ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(timeFormat.format(Date(event.timestamp)), style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.width(60.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(event.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (data.events.size > 3) {
                    Text("...y ${data.events.size - 3} más", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}
