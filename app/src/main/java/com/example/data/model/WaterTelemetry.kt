package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_telemetry")
data class WaterTelemetry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folio: String,
    val dateStr: String,
    val timeStr: String,
    val timestamp: Long,
    val wellLevel: Double,
    val waterBidones: Int
)
