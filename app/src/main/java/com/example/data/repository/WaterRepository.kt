package com.example.data.repository

import com.example.data.db.WaterTelemetryDao
import com.example.data.model.WaterTelemetry
import kotlinx.coroutines.flow.Flow

class WaterRepository(private val telemetryDao: WaterTelemetryDao) {
    val allTelemetry: Flow<List<WaterTelemetry>> = telemetryDao.getAllTelemetry()

    suspend fun insert(record: WaterTelemetry) {
        telemetryDao.insertTelemetry(record)
    }

    suspend fun deleteById(id: Long) {
        telemetryDao.deleteTelemetryById(id)
    }

    suspend fun clearAll() {
        telemetryDao.clearHistory()
    }
}
