package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.WaterTelemetry
import com.example.data.repository.WaterRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ToastType {
    SUCCESS, WARNING, INFO
}

data class ToastState(
    val visible: Boolean = false,
    val message: String = "",
    val type: ToastType = ToastType.INFO
)

class WaterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = WaterRepository(db.telemetryDao())
    private val prefs = PreferencesManager(application)

    // Reactive Flow of history from Room
    val historyRecords: StateFlow<List<WaterTelemetry>> = repository.allTelemetry
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current levels maintained via State and Persisted on adjustment
    var wellLevel by mutableStateOf(prefs.wellLevel)
        private set

    var waterBidones by mutableStateOf(prefs.waterBidones)
        private set

    var isSyncing by mutableStateOf(false)
        private set

    var lastUpdate by mutableStateOf(prefs.lastUpdate)
        private set

    var toastState by mutableStateOf(ToastState())
        private set

    var showReportDialog by mutableStateOf(false)

    init {
        // Fallback sync timing if history has records
        viewModelScope.launch {
            historyRecords.collect { records ->
                if (records.isNotEmpty() && lastUpdate == 0L) {
                    lastUpdate = records.first().timestamp
                    prefs.lastUpdate = lastUpdate
                }
            }
        }
    }

    fun adjustWellLevel(delta: Float) {
        val nextVal = (wellLevel + delta).coerceIn(0f, 2.00f)
        wellLevel = Math.round(nextVal * 100f) / 100f
        prefs.wellLevel = wellLevel
    }

    fun selectWellLevelPreset(preset: Float) {
        wellLevel = preset.coerceIn(0f, 2.00f)
        prefs.wellLevel = wellLevel
    }

    fun adjustWaterBidones(delta: Int) {
        waterBidones = (waterBidones + delta).coerceIn(0, 50)
        prefs.waterBidones = waterBidones
    }

    fun showToast(message: String, type: ToastType = ToastType.INFO) {
        viewModelScope.launch {
            toastState = ToastState(visible = true, message = message, type = type)
            delay(4000)
            if (toastState.message == message) {
                toastState = ToastState()
            }
        }
    }

    fun finalizeDaySync() {
        if (isSyncing) return
        isSyncing = true
        viewModelScope.launch {
            // Replicate 800ms satellite database delay
            delay(800)

            val now = Calendar.getInstance().time
            val dateDigits = SimpleDateFormat("yyyyMMdd", Locale.US).format(now)
            val allowedChars = ('A'..'Z') + ('0'..'9')
            val hashTag = (1..4).map { allowedChars.random() }.joinToString("")
            val folio = "UCSP-$dateDigits-$hashTag"

            // Localized custom Peruvian Spanish dates
            Locale.setDefault(Locale("es", "PE"))
            val dayFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "PE"))
            val rawDateStr = dayFormat.format(now)
            val dateStr = rawDateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "PE")) else it.toString() }
            
            val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(now)

            val record = WaterTelemetry(
                folio = folio,
                dateStr = dateStr,
                timeStr = timeStr,
                timestamp = System.currentTimeMillis(),
                wellLevel = wellLevel.toDouble(),
                waterBidones = waterBidones
            )

            repository.insert(record)
            lastUpdate = record.timestamp
            prefs.lastUpdate = lastUpdate

            isSyncing = false
            showToast("Telemetría sincronizada con la base de datos central", ToastType.SUCCESS)
        }
    }

    fun deleteTelemetry(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
            showToast("Registro eliminado con éxito", ToastType.INFO)
        }
    }
}
