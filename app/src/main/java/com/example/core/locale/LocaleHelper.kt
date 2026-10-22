package com.example.core.locale

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Locale Helper for Ronova Mesh.
 * Strictly enforces English-only UI as per PRD v2.3 requirements.
 */
object LocaleHelper {

    const val LANGUAGE_SYSTEM = "en"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_BANGLA = "en"

    fun getPersistedLanguage(context: Context): String = LANGUAGE_ENGLISH

    fun setPersistedLanguage(context: Context, language: String) {
        // Enforce English-only UI
    }

    fun isBangla(context: Context): Boolean = false

    fun getEffectiveLocale(context: Context): Locale = Locale.US

    fun getLocalizedContext(context: Context, targetLanguage: String? = null): Context {
        val locale = Locale.US
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        return context.createConfigurationContext(config)
    }

    fun getLocalizedConfiguration(context: Context, targetLanguage: String? = null): Configuration {
        val config = Configuration(context.resources.configuration)
        val locale = Locale.US
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        return config
    }
}
