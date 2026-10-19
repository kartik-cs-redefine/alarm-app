package com.example.shakeup.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.shakeup.domain.ShakeSensitivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shakeup_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val DEFAULT_SNOOZE_MINUTES = intPreferencesKey("default_snooze_minutes")
        val DEFAULT_SHAKE_SENSITIVITY = stringPreferencesKey("default_shake_sensitivity")
        val DEFAULT_RINGTONE_URI = stringPreferencesKey("default_ringtone_uri")
        val DEFAULT_VIBRATE = stringPreferencesKey("default_vibrate")
    }

    val defaultSnoozeMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_SNOOZE_MINUTES] ?: 5
    }

    val defaultShakeSensitivity: Flow<ShakeSensitivity> = context.dataStore.data.map { prefs ->
        val name = prefs[Keys.DEFAULT_SHAKE_SENSITIVITY]
        if (name != null) {
            try { ShakeSensitivity.valueOf(name) } catch (e: Exception) { ShakeSensitivity.MEDIUM }
        } else {
            ShakeSensitivity.MEDIUM
        }
    }

    val defaultRingtoneUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_RINGTONE_URI]
    }

    val defaultVibrate: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_VIBRATE]?.toBoolean() ?: true
    }

    suspend fun setDefaultSnoozeMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_SNOOZE_MINUTES] = minutes
        }
    }

    suspend fun setDefaultShakeSensitivity(sensitivity: ShakeSensitivity) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_SHAKE_SENSITIVITY] = sensitivity.name
        }
    }

    suspend fun setDefaultRingtoneUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri != null) {
                prefs[Keys.DEFAULT_RINGTONE_URI] = uri
            } else {
                prefs.remove(Keys.DEFAULT_RINGTONE_URI)
            }
        }
    }

    suspend fun setDefaultVibrate(vibrate: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_VIBRATE] = vibrate.toString()
        }
    }
}
