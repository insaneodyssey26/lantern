package com.masum.lantern.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.masum.lantern.domain.repository.SettingsRepository
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

    override fun getThemeModeSync(): String =
        prefs.getString(KEY_THEME_MODE, "system") ?: "system"

    override suspend fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    override fun getColorTheme(): Flow<String> = callbackFlow {
        trySend(prefs.getString(KEY_COLOR_THEME, "system") ?: "system")
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_COLOR_THEME) {
                trySend(prefs.getString(KEY_COLOR_THEME, "system") ?: "system")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setColorTheme(theme: String) {
        prefs.edit().putString(KEY_COLOR_THEME, theme).apply()
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

    override fun isBlockingEnabled(): Flow<Boolean> = callbackFlow {
        trySend(prefs.getBoolean(KEY_BLOCKING_ENABLED, true))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_BLOCKING_ENABLED) {
                trySend(prefs.getBoolean(KEY_BLOCKING_ENABLED, true))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setBlockingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCKING_ENABLED, enabled).apply()
    }

    override fun isPureBlack(): Flow<Boolean> = callbackFlow {
        trySend(prefs.getBoolean(KEY_PURE_BLACK, false))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_PURE_BLACK) {
                trySend(prefs.getBoolean(KEY_PURE_BLACK, false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun isPureBlackSync(): Boolean =
        prefs.getBoolean(KEY_PURE_BLACK, false)

    override suspend fun setPureBlack(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PURE_BLACK, enabled).apply()
    }

    override fun isUseSystemFont(): Flow<Boolean> = callbackFlow {
        trySend(prefs.getBoolean(KEY_USE_SYSTEM_FONT, false))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_USE_SYSTEM_FONT) {
                trySend(prefs.getBoolean(KEY_USE_SYSTEM_FONT, false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun isUseSystemFontSync(): Boolean =
        prefs.getBoolean(KEY_USE_SYSTEM_FONT, false)

    override suspend fun setUseSystemFont(useSystem: Boolean) {
        prefs.edit().putBoolean(KEY_USE_SYSTEM_FONT, useSystem).apply()
    }

    override fun getBypassedApps(): Flow<Set<String>> = callbackFlow {
        trySend(prefs.getStringSet(KEY_BYPASSED_APPS, emptySet())?.toSet() ?: emptySet())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_BYPASSED_APPS) {
                trySend(prefs.getStringSet(KEY_BYPASSED_APPS, emptySet())?.toSet() ?: emptySet())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setAppBypassed(packageName: String, bypassed: Boolean) {
        val current = prefs.getStringSet(KEY_BYPASSED_APPS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (bypassed) {
            current.add(packageName)
        } else {
            current.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_BYPASSED_APPS, current).apply()
    }

    companion object {
        private const val KEY_THEME_MODE   = "theme_mode"
        private const val KEY_COLOR_THEME  = "color_theme"
        private const val KEY_UPSTREAM_DNS = "upstream_dns"
        private const val KEY_MAX_AGE_DAYS = "max_age_days"
        private const val KEY_MAX_ROWS     = "max_rows"
        private const val KEY_BLOCKING_ENABLED = "blocking_enabled"
        private const val KEY_USE_SYSTEM_FONT  = "use_system_font"
        private const val KEY_PURE_BLACK       = "pure_black"
        private const val KEY_BYPASSED_APPS    = "bypassed_apps"
    }
}
