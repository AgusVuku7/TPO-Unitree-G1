package com.uade.vigia.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.util.UUID
import kotlin.math.min

interface IRobotRepository {
    val telemetryFlow: StateFlow<Telemetry>
    fun sendCommand(command: Command): CommandResult
}

enum class CommandResult { PENDIENTE, CONFIRMADA, RECHAZADA, SIN_RESPUESTA }

class SimulationRepositoryImpl(
    private val coroutineScope: CoroutineScope
) : IRobotRepository {

    private val _telemetryFlow = MutableStateFlow(Telemetry())
    override val telemetryFlow: StateFlow<Telemetry> = _telemetryFlow.asStateFlow()

    private var simulationJob: Job? = null
    
    // Variables internas del simulador de movimiento (0.5 m/s)
    private var currentDistance = 0.0f
    private val distanceToP1 = 5.0f
    private val distanceToP3 = 20.0f
    private val distanceToP4 = 35.0f

    init {
        startSimulation()
    }

    private fun startSimulation() {
        simulationJob = coroutineScope.launch {
            while (isActive) {
                delay(1000)
                tick()
            }
        }
    }

    private fun tick() {
        _telemetryFlow.update { current ->
            // 1. Lógica de Batería y Recarga
            val isMoving = current.robotState == RobotState.CAMINANDO
            val isCharging = current.robotState == RobotState.EN_BASE || current.mission?.currentPoint == "SAFE"
            
            val batteryDrop = if (isMoving) 2 else 1
            var newBatteryLevel = current.battery.level
            
            if (isCharging) {
                newBatteryLevel = min(100, current.battery.level + 2) // Carga 2% por tick
            } else if (current.battery.level > 0) {
                newBatteryLevel = maxOf(0, current.battery.level - batteryDrop)
            }

            // Generar Alertas Automáticas (Batería Baja)
            val activeAlerts = current.activeAlerts.toMutableList()
            if (newBatteryLevel <= 15 && activeAlerts.none { it.type == AlertType.BATERIA_BAJA }) {
                activeAlerts.add(
                    Alert(UUID.randomUUID().toString(), "Batería baja - $newBatteryLevel%", AlertType.BATERIA_BAJA)
                )
            }

            // 2. Lógica de Progresión de Misión
            var newState = current.robotState
            var newPoint = current.mission?.currentPoint ?: "BASE"
            var newProgress = current.mission?.progressPercent ?: 0
            var newLed = current.serviceVariables.ledColor

            if (current.mission?.status == MissionStatus.EN_CURSO && isMoving) {
                currentDistance += 0.5f // Avanza 0.5 metros por segundo
                
                // Cálculos de puntos del recorrido
                if (currentDistance >= distanceToP4) {
                    newPoint = "P4"
                    newState = RobotState.EN_DESTINO
                    newLed = LedColor.VERDE
                    // Llegó al final (ida)
                } else if (currentDistance >= distanceToP3 && current.mission.currentPoint != "P3_CRUZADO") {
                    newPoint = "P3"
                    newState = RobotState.ESPERANDO_EN_CRUCE
                    newLed = LedColor.AMBAR // Detención forzosa antes del cruce
                } else if (currentDistance >= distanceToP1) {
                    newPoint = "P2" // Pasillo
                } else {
                    newPoint = "P1" // Hall
                }

                newProgress = min(100, ((currentDistance / distanceToP4) * 100).toInt())
            }

            // Actualizar variables de servicio basadas en movimiento
            val newServiceVars = current.serviceVariables.copy(
                ledColor = newLed,
                visitorDistanceM = if (isMoving) 1.2f else current.serviceVariables.visitorDistanceM
            )

            current.copy(
                timestamp = Instant.now(),
                battery = current.battery.copy(level = newBatteryLevel, isCharging = isCharging),
                robotState = newState,
                mission = current.mission?.copy(
                    currentPoint = newPoint,
                    progressPercent = newProgress
                ),
                serviceVariables = newServiceVars,
                activeAlerts = activeAlerts
            )
        }
    }

    override fun sendCommand(command: Command): CommandResult {
        val currentState = _telemetryFlow.value

        if (currentState.connection.status == ConnectionStatus.SIN_CONEXION) {
            return CommandResult.SIN_RESPUESTA
        }

        when (command) {
            is Command.IniciarEscolta -> {
                // Solo si está en BASE y con batería > 30%
                if (currentState.robotState == RobotState.EN_BASE && currentState.battery.level > 30) {
                    currentDistance = 0.0f // Reseteamos la distancia del simulador
                    _telemetryFlow.update { it.copy(
                        robotState = RobotState.CAMINANDO,
                        battery = it.battery.copy(isCharging = false),
                        mission = MissionState(
                            id = "ESC-${(100..999).random()}",
                            status = MissionStatus.EN_CURSO,
                            currentPoint = "P1"
                        ),
                        serviceVariables = it.serviceVariables.copy(ledColor = LedColor.AZUL)
                    )}
                    return CommandResult.CONFIRMADA
                }
                return CommandResult.RECHAZADA
            }
            is Command.Pausar -> {
                if (currentState.mission?.status == MissionStatus.EN_CURSO) {
                    _telemetryFlow.update { it.copy(
                        robotState = RobotState.DETENIDO,
                        mission = it.mission?.copy(status = MissionStatus.EN_PAUSA),
                        serviceVariables = it.serviceVariables.copy(ledColor = LedColor.AMBAR)
                    )}
                    return CommandResult.CONFIRMADA
                }
                return CommandResult.RECHAZADA
            }
            is Command.Reanudar -> {
                if (currentState.mission?.status == MissionStatus.EN_PAUSA) {
                    _telemetryFlow.update { it.copy(
                        robotState = RobotState.CAMINANDO,
                        mission = it.mission?.copy(status = MissionStatus.EN_CURSO),
                        serviceVariables = it.serviceVariables.copy(ledColor = LedColor.AZUL)
                    )}
                    return CommandResult.CONFIRMADA
                }
                return CommandResult.RECHAZADA
            }
            is Command.Cancelar -> {
                _telemetryFlow.update { it.copy(
                    robotState = RobotState.EN_BASE,
                    mission = it.mission?.copy(status = MissionStatus.CANCELADA, currentPoint = "BASE"),
                    serviceVariables = it.serviceVariables.copy(ledColor = LedColor.VERDE)
                )}
                return CommandResult.CONFIRMADA
            }
            is Command.IrPuntoSeguro -> {
                _telemetryFlow.update { it.copy(
                    robotState = RobotState.DETENIDO,
                    mission = it.mission?.copy(status = MissionStatus.ABORTADA, currentPoint = "SAFE"),
                    serviceVariables = it.serviceVariables.copy(ledColor = LedColor.AMBAR)
                )}
                return CommandResult.CONFIRMADA
            }
            is Command.Detener -> {
                _telemetryFlow.update { it.copy(
                    robotState = RobotState.DETENIDO,
                    mission = it.mission?.copy(status = MissionStatus.EN_PAUSA),
                    serviceVariables = it.serviceVariables.copy(ledColor = LedColor.ROJO)
                )}
                return CommandResult.CONFIRMADA
            }
            is Command.ConfirmarCruce -> {
                // Solo se acepta si el robot estaba esperando en P3
                if (currentState.robotState == RobotState.ESPERANDO_EN_CRUCE && command.isClear) {
                    _telemetryFlow.update { it.copy(
                        robotState = RobotState.CAMINANDO,
                        mission = it.mission?.copy(currentPoint = "P3_CRUZADO"),
                        serviceVariables = it.serviceVariables.copy(ledColor = LedColor.AZUL)
                    )}
                    return CommandResult.CONFIRMADA
                }
                return CommandResult.RECHAZADA
            }
            is Command.ReconocerAlerta -> {
                _telemetryFlow.update { state ->
                    val updatedAlerts = state.activeAlerts.map { alert ->
                        if (alert.id == command.alertId) alert.copy(isAcknowledged = true) else alert
                    }.filterNot { it.isAcknowledged } // Removemos las reconocidas de la UI
                    
                    state.copy(activeAlerts = updatedAlerts)
                }
                return CommandResult.CONFIRMADA
            }
        }
    }
}
