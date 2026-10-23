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

    override fun getColorThemeSync(): String =
        prefs.getString(KEY_COLOR_THEME, "system") ?: "system"

    override suspend fun setColorTheme(theme: String) {
        prefs.edit().putString(KEY_COLOR_THEME, theme).apply()
    }

    override fun getUpstreamDns(): Flow<String> = callbackFlow {
        trySend(getUpstreamDnsSync())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_UPSTREAM_DNS) {
                trySend(getUpstreamDnsSync())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun getUpstreamDnsSync(): String =
        prefs.getString(KEY_UPSTREAM_DNS, "1.1.1.1") ?: "1.1.1.1"

    override suspend fun setUpstreamDns(dns: String) {
        prefs.edit().putString(KEY_UPSTREAM_DNS, dns).apply()
    }

    override fun getDnsProtocol(): Flow<String> = callbackFlow {
        trySend(getDnsProtocolSync())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_DNS_PROTOCOL) {
                trySend(getDnsProtocolSync())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun getDnsProtocolSync(): String =
        prefs.getString(KEY_DNS_PROTOCOL, "UDP") ?: "UDP"

    override suspend fun setDnsProtocol(protocol: String) {
        prefs.edit().putString(KEY_DNS_PROTOCOL, protocol).apply()
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

    override fun getUserAllowedDomains(): Flow<Set<String>> = callbackFlow {
        trySend(getUserAllowedDomainsSync())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_USER_ALLOWED_DOMAINS) {
                trySend(getUserAllowedDomainsSync())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun getUserAllowedDomainsSync(): Set<String> =
        prefs.getStringSet(KEY_USER_ALLOWED_DOMAINS, emptySet())?.toSet() ?: emptySet()

    override suspend fun setUserDomainAllowed(domain: String, allowed: Boolean) {
        val normalized = domain.trim().lowercase()
        val currentAllowed = getUserAllowedDomainsSync().toMutableSet()
        val currentBlocked = getUserBlockedDomainsSync().toMutableSet()
        if (allowed) {
            currentAllowed.add(normalized)
            currentBlocked.remove(normalized)
        } else {
            currentAllowed.remove(normalized)
        }
        prefs.edit()
            .putStringSet(KEY_USER_ALLOWED_DOMAINS, currentAllowed)
            .putStringSet(KEY_USER_BLOCKED_DOMAINS, currentBlocked)
            .apply()
    }

    override fun getUserBlockedDomains(): Flow<Set<String>> = callbackFlow {
        trySend(getUserBlockedDomainsSync())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_USER_BLOCKED_DOMAINS) {
                trySend(getUserBlockedDomainsSync())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun getUserBlockedDomainsSync(): Set<String> =
        prefs.getStringSet(KEY_USER_BLOCKED_DOMAINS, emptySet())?.toSet() ?: emptySet()

    override suspend fun setUserDomainBlocked(domain: String, blocked: Boolean) {
        val normalized = domain.trim().lowercase()
        val currentBlocked = getUserBlockedDomainsSync().toMutableSet()
        val currentAllowed = getUserAllowedDomainsSync().toMutableSet()
        if (blocked) {
            currentBlocked.add(normalized)
            currentAllowed.remove(normalized)
        } else {
            currentBlocked.remove(normalized)
        }
        prefs.edit()
            .putStringSet(KEY_USER_BLOCKED_DOMAINS, currentBlocked)
            .putStringSet(KEY_USER_ALLOWED_DOMAINS, currentAllowed)
            .apply()
    }

    override fun isOnboardingCompleted(): Flow<Boolean> = callbackFlow {
        trySend(isOnboardingCompletedSync())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_ONBOARDING_COMPLETED) {
                trySend(isOnboardingCompletedSync())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun isOnboardingCompletedSync(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    override fun isFeatureGuideCompleted(): Flow<Boolean> = callbackFlow {
        trySend(isFeatureGuideCompletedSync())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_FEATURE_GUIDE_COMPLETED) {
                trySend(isFeatureGuideCompletedSync())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun isFeatureGuideCompletedSync(): Boolean =
        prefs.getBoolean(KEY_FEATURE_GUIDE_COMPLETED, false)

    override suspend fun setFeatureGuideCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_FEATURE_GUIDE_COMPLETED, completed).apply()
    }

    companion object {
        private const val KEY_THEME_MODE   = "theme_mode"
        private const val KEY_COLOR_THEME  = "color_theme"
        private const val KEY_UPSTREAM_DNS = "upstream_dns"
        private const val KEY_DNS_PROTOCOL = "dns_protocol"
        private const val KEY_MAX_AGE_DAYS = "max_age_days"
        private const val KEY_MAX_ROWS     = "max_rows"
        private const val KEY_BLOCKING_ENABLED = "blocking_enabled"
        private const val KEY_USE_SYSTEM_FONT  = "use_system_font"
        private const val KEY_PURE_BLACK       = "pure_black"
        private const val KEY_BYPASSED_APPS    = "bypassed_apps"
        private const val KEY_USER_ALLOWED_DOMAINS = "user_allowed_domains"
        private const val KEY_USER_BLOCKED_DOMAINS = "user_blocked_domains"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_FEATURE_GUIDE_COMPLETED = "feature_guide_completed"
    }
}
