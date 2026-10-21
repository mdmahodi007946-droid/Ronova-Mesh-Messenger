package com.example.core.locale

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Localization helper supporting Bengali (bn) and English (en).
 */
object LocaleHelper {

    const val LOCALE_BANGLA = "bn"
    const val LANGUAGE_BANGLA = "bn"
    const val LANGUAGE_ENGLISH = "en"

    private const val PREFS_NAME = "ronova_locale_prefs"
    private const val KEY_LANG = "selected_language"

    fun getPersistedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, LOCALE_BANGLA) ?: LOCALE_BANGLA
    }

    fun persistLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, language).apply()
    }

    fun setLanguage(context: Context, language: String) {
        persistLanguage(context, language)
    }

    fun getLocalizedContext(context: Context, language: String = getPersistedLanguage(context)): Context {
        val locale = Locale(language)
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

    fun getLocalizedConfiguration(context: Context, language: String = getPersistedLanguage(context)): Configuration {
        val locale = Locale(language)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        return config
    }
}
