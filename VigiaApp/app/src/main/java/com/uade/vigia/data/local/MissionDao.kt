package com.uade.vigia.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Query("UPDATE missions SET status = :status, endTimestamp = :endTimestamp, durationFormatted = :duration, totalDistanceMeters = :distance WHERE id = :id")
    suspend fun updateMission(id: String, status: String, endTimestamp: Long, duration: String, distance: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Transaction
    @Query("SELECT * FROM missions ORDER BY startTimestamp DESC")
    fun getAllMissionsWithEvents(): Flow<List<MissionWithEvents>>

    @Query("SELECT * FROM missions WHERE id = :id LIMIT 1")
    suspend fun getMissionById(id: String): MissionEntity?
}
