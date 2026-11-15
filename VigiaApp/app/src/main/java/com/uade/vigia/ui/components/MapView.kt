package com.uade.vigia.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.uade.vigia.data.MissionState
import com.uade.vigia.ui.theme.*

@Composable
fun MapView(mission: MissionState?, modifier: Modifier = Modifier) {
    val progress = mission?.progressPercent ?: 0
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Dibujar Zonas
        // Hall (0 a 20%)
        drawRect(color = BackgroundLight, topLeft = Offset(0f, 0f), size = Size(width * 0.2f, height))
        // Pasillo (20% a 80%)
        drawRect(color = Color(0xFFEEEEEE), topLeft = Offset(width * 0.2f, 0f), size = Size(width * 0.6f, height))
        // Oficina (80% a 100%)
        drawRect(color = BackgroundLight, topLeft = Offset(width * 0.8f, 0f), size = Size(width * 0.2f, height))
        
        // Dibujar Cruce de Autoelevadores (Rojo claro en el 50%)
        val crossingCenter = width * 0.5f
        drawRect(color = Color(0xFFFFCDD2), topLeft = Offset(crossingCenter - 20f, 0f), size = Size(40f, height))
        
        // Posición del Robot
        val robotX = width * (progress / 100f)
        val robotY = height / 2f
        
        drawCircle(
            color = BlueEscorting,
            radius = 16f,
            center = Offset(robotX, robotY)
        )
    }
}
