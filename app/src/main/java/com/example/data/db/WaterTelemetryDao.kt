package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WaterTelemetry
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterTelemetryDao {
    @Query("SELECT * FROM water_telemetry ORDER BY timestamp DESC")
    fun getAllTelemetry(): Flow<List<WaterTelemetry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(record: WaterTelemetry)

    @Query("DELETE FROM water_telemetry WHERE id = :id")
    suspend fun deleteTelemetryById(id: Long)

    @Query("DELETE FROM water_telemetry")
    suspend fun clearHistory()
}
