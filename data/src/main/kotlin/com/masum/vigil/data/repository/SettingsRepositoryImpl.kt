package com.masum.vigil.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.masum.vigil.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("vigil_settings", Context.MODE_PRIVATE)

    override fun getThemeMode(): Flow<String> = callbackFlow {
        trySend(prefs.getString(KEY_THEME_MODE, "system") ?: "system")
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_THEME_MODE) {
                trySend(prefs.getString(KEY_THEME_MODE, "system") ?: "system")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    override fun getUpstreamDns(): Flow<String> = callbackFlow {
        trySend(prefs.getString(KEY_UPSTREAM_DNS, "1.1.1.1") ?: "1.1.1.1")
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_UPSTREAM_DNS) {
                trySend(prefs.getString(KEY_UPSTREAM_DNS, "1.1.1.1") ?: "1.1.1.1")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setUpstreamDns(dns: String) {
        prefs.edit().putString(KEY_UPSTREAM_DNS, dns).apply()
    }

    override fun getMaxAgeDays(): Flow<Int> = callbackFlow {
        trySend(prefs.getInt(KEY_MAX_AGE_DAYS, 7))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_MAX_AGE_DAYS) {
                trySend(prefs.getInt(KEY_MAX_AGE_DAYS, 7))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setMaxAgeDays(days: Int) {
        prefs.edit().putInt(KEY_MAX_AGE_DAYS, days).apply()
    }

    override fun getMaxRows(): Flow<Int> = callbackFlow {
        trySend(prefs.getInt(KEY_MAX_ROWS, 50_000))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_MAX_ROWS) {
                trySend(prefs.getInt(KEY_MAX_ROWS, 50_000))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setMaxRows(rows: Int) {
        prefs.edit().putInt(KEY_MAX_ROWS, rows).apply()
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_UPSTREAM_DNS = "upstream_dns"
        private const val KEY_MAX_AGE_DAYS = "max_age_days"
        private const val KEY_MAX_ROWS = "max_rows"
    }
}
