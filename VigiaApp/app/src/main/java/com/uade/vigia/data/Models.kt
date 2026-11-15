package com.uade.vigia.data

import java.time.Instant

// --- TELEMETRÍA ---
data class Telemetry(
    val timestamp: Instant = Instant.now(),
    val connection: ConnectionState = ConnectionState(),
    val battery: BatteryState = BatteryState(),
    val robotState: RobotState = RobotState.EN_BASE,
    val mission: MissionState? = null,
    val serviceVariables: ServiceVariables = ServiceVariables(),
    val activeAlerts: List<Alert> = emptyList()
)

data class ConnectionState(
    val status: ConnectionStatus = ConnectionStatus.EN_LINEA,
    val latencyMs: Int = 42
)

enum class ConnectionStatus { EN_LINEA, SIN_CONEXION }

data class BatteryState(
    val level: Int = 100,
    val isCharging: Boolean = true
)

enum class RobotState {
    EN_BASE, CAMINANDO, ESPERANDO_EN_CRUCE, EN_DESTINO, DETENIDO
}

// --- VARIABLES DE SERVICIO (HRI) ---
data class ServiceVariables(
    val visitorInRange: Boolean = true,
    val visitorDistanceM: Float = 0.0f,
    val ledColor: LedColor = LedColor.VERDE,
    val voiceMessagesDelivered: Int = 0
)

enum class LedColor { VERDE, AZUL, AMBAR, ROJO }

// --- MISIÓN Y ALERTAS ---
data class MissionState(
    val id: String,
    val status: MissionStatus,
    val currentPoint: String = "BASE",
    val progressPercent: Int = 0
)

enum class MissionStatus { EN_CURSO, EN_PAUSA, COMPLETADA, CANCELADA, ABORTADA }

data class Alert(
    val id: String,
    val message: String,
    val type: AlertType,
    val isAcknowledged: Boolean = false
)

enum class AlertType { BATERIA_BAJA, VISITANTE_REZAGADO, OBSTACULO, INCLINACION }

// --- COMANDOS ---
sealed class Command {
    data class IniciarEscolta(val visitorName: String, val destination: String) : Command()
    object Pausar : Command()
    object Reanudar : Command()
    object Cancelar : Command()
    object IrPuntoSeguro : Command()
    object Detener : Command()
    data class ConfirmarCruce(val isClear: Boolean) : Command()
    data class ReconocerAlerta(val alertId: String) : Command()
}
