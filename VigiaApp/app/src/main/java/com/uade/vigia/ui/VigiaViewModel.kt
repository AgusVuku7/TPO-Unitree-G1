package com.uade.vigia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uade.vigia.data.*
import com.uade.vigia.data.local.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

// Estado consolidado para la UI
data class UiState(
    val telemetry: Telemetry = Telemetry(),
    val connectionStatus: UiConnectionStatus = UiConnectionStatus.EN_LINEA,
    val lastUpdateTime: String = "Actualizado recién",
    val missionsHistory: List<MissionWithEvents> = emptyList()
)

enum class UiConnectionStatus { EN_LINEA, DATO_ANTIGUO, SIN_CONEXION }

class VigiaViewModel(
    private val repository: IRobotRepository,
    private val missionDao: MissionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeTelemetry()
        observeHistory()
    }

    private fun observeTelemetry() {
        viewModelScope.launch {
            repository.telemetryFlow.collect { newTelemetry ->
                val now = Instant.now()
                val age = Duration.between(newTelemetry.timestamp, now).seconds
                
                // Lógica de "Dato Antiguo" y "Desconexión" (Requerimiento de consigna)
                val uiConnection = when {
                    age > 10 || newTelemetry.connection.status == ConnectionStatus.SIN_CONEXION -> UiConnectionStatus.SIN_CONEXION
                    age > 3 -> UiConnectionStatus.DATO_ANTIGUO
                    else -> UiConnectionStatus.EN_LINEA
                }

                val updateText = if (uiConnection == UiConnectionStatus.EN_LINEA) {
                    "Actualizado hace ${maxOf(1, age)} s"
                } else {
                    "Dato antiguo - $age s"
                }

                _uiState.update { it.copy(
                    telemetry = newTelemetry,
                    connectionStatus = uiConnection,
                    lastUpdateTime = updateText
                ) }
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            missionDao.getAllMissionsWithEvents().collect { history ->
                _uiState.update { it.copy(missionsHistory = history) }
            }
        }
    }

    // --- FUNCIONES DE COMANDO PARA LA UI ---

    fun onStartMission(visitorName: String, destination: String) {
        val result = repository.sendCommand(Command.IniciarEscolta(visitorName, destination))
        if (result == CommandResult.CONFIRMADA) {
            val missionId = _uiState.value.telemetry.mission?.id ?: return
            
            viewModelScope.launch {
                missionDao.insertMission(
                    MissionEntity(
                        id = missionId,
                        status = MissionStatus.EN_CURSO.name,
                        startTimestamp = System.currentTimeMillis()
                    )
                )
                logEvent(missionId, "Iniciar escolta - confirmada por el robot")
            }
        }
    }

    fun onPauseMission() {
        val result = repository.sendCommand(Command.Pausar)
        if (result == CommandResult.CONFIRMADA) {
            val missionId = _uiState.value.telemetry.mission?.id ?: return
            logEvent(missionId, "Misión pausada")
        }
    }

    fun onResumeMission() {
        val result = repository.sendCommand(Command.Reanudar)
        if (result == CommandResult.CONFIRMADA) {
            val missionId = _uiState.value.telemetry.mission?.id ?: return
            logEvent(missionId, "Misión reanudada")
        }
    }

    fun onCancelMission() {
        val missionId = _uiState.value.telemetry.mission?.id ?: return
        val result = repository.sendCommand(Command.Cancelar)
        if (result == CommandResult.CONFIRMADA) {
            viewModelScope.launch {
                val endTs = System.currentTimeMillis()
                val durationStr = calculateDuration(missionId, endTs)
                missionDao.updateMission(
                    id = missionId,
                    status = MissionStatus.CANCELADA.name,
                    endTimestamp = endTs,
                    duration = durationStr,
                    distance = 0 // Simulado
                )
                logEvent(missionId, "Misión cancelada")
            }
        }
    }

    fun onConfirmCrossing() {
        val result = repository.sendCommand(Command.ConfirmarCruce(isClear = true))
        if (result == CommandResult.CONFIRMADA) {
            val missionId = _uiState.value.telemetry.mission?.id ?: return
            logEvent(missionId, "Cruce libre - confirmado por guardia")
        }
    }

    fun onAcknowledgeAlert(alertId: String) {
        val result = repository.sendCommand(Command.ReconocerAlerta(alertId))
        if (result == CommandResult.CONFIRMADA) {
            val missionId = _uiState.value.telemetry.mission?.id
            if (missionId != null) {
                logEvent(missionId, "Alerta reconocida ($alertId)")
            }
        }
    }
    
    fun onEmergencyStop() {
        val result = repository.sendCommand(Command.Detener)
        if (result == CommandResult.CONFIRMADA) {
            val missionId = _uiState.value.telemetry.mission?.id ?: return
            logEvent(missionId, "Detención operativa accionada")
        }
    }

    private fun logEvent(missionId: String, description: String) {
        viewModelScope.launch {
            missionDao.insertEvent(
                EventEntity(
                    missionId = missionId,
                    timestamp = System.currentTimeMillis(),
                    description = description
                )
            )
        }
    }

    suspend fun calculateDuration(missionId: String, endTimestamp: Long = System.currentTimeMillis()): String {
        val mission = missionDao.getMissionById(missionId)
        val start = mission?.startTimestamp
            ?: _uiState.value.missionsHistory.find { it.mission.id == missionId }?.mission?.startTimestamp
            ?: endTimestamp
        val diffMillis = maxOf(0L, endTimestamp - start)
        val totalSeconds = diffMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) {
            "$minutes min $seconds s"
        } else {
            "$seconds s"
        }
    }
}
