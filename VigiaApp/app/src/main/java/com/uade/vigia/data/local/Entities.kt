package com.uade.vigia.data.local

import androidx.room.*

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val id: String,
    val status: String, // EN_CURSO, COMPLETADA, CANCELADA, ABORTADA
    val startTimestamp: Long,
    val endTimestamp: Long? = null,
    val durationFormatted: String = "",
    val totalDistanceMeters: Int = 0
)

@Entity(
    tableName = "mission_events",
    foreignKeys = [ForeignKey(
        entity = MissionEntity::class,
        parentColumns = ["id"],
        childColumns = ["missionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("missionId")]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val missionId: String,
    val timestamp: Long,
    val description: String
)

data class MissionWithEvents(
    @Embedded val mission: MissionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "missionId"
    )
    val events: List<EventEntity>
)
