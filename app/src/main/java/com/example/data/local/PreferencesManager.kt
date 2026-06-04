package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nexus_ucsp_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WELL_LEVEL = "well_level"
        private const val KEY_WATER_BIDONES = "water_bidones"
        private const val KEY_LAST_UPDATE = "last_update"
    }

    var wellLevel: Float
        get() = prefs.getFloat(KEY_WELL_LEVEL, 1.15f)
        set(value) = prefs.edit().putFloat(KEY_WELL_LEVEL, value).apply()

    var waterBidones: Int
        get() = prefs.getInt(KEY_WATER_BIDONES, 37)
        set(value) = prefs.edit().putInt(KEY_WATER_BIDONES, value).apply()

    var lastUpdate: Long
        get() = prefs.getLong(KEY_LAST_UPDATE, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_UPDATE, value).apply()
}
