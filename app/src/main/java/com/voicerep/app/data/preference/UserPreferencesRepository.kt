package com.voicerep.app.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.voicerep.app.audio.AudioConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val TRIGGER_MARGIN_DB = doublePreferencesKey("trigger_margin_db")
        val MIN_REP_INTERVAL_MS = longPreferencesKey("min_rep_interval_ms")
    }

    val triggerMarginDb: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[Keys.TRIGGER_MARGIN_DB] ?: AudioConfig.DEFAULT_TRIGGER_MARGIN_DB
    }

    val minRepIntervalMs: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[Keys.MIN_REP_INTERVAL_MS] ?: AudioConfig.DEFAULT_MIN_REP_INTERVAL_MS
    }

    suspend fun setTriggerMarginDb(marginDb: Double) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TRIGGER_MARGIN_DB] = marginDb
        }
    }

    suspend fun setMinRepIntervalMs(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MIN_REP_INTERVAL_MS] = intervalMs
        }
    }
}
