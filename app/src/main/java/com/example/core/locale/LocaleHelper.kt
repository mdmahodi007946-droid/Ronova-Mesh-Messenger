package com.example.core.locale

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Locale Helper for Ronova Mesh PRD v2.1.
 * Supports:
 * - "system": Follows device system language (default).
 * - "en": Explicit English (values/strings.xml).
 * - "bn": Explicit Bengali (values-bn/strings.xml).
 */
object LocaleHelper {

    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_BANGLA = "bn"

    private const val PREFS_NAME = "ronova_preferences"
    private const val KEY_LANGUAGE = "app_language"

    fun getPersistedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }

    fun setPersistedLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun isBangla(context: Context): Boolean {
        val lang = getPersistedLanguage(context)
        return if (lang == LANGUAGE_SYSTEM) {
            val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0)
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
            systemLocale.language.equals("bn", ignoreCase = true)
        } else {
            lang == LANGUAGE_BANGLA
        }
    }

    fun getEffectiveLocale(context: Context): Locale {
        val lang = getPersistedLanguage(context)
        return if (lang == LANGUAGE_SYSTEM) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0)
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
        } else {
            Locale(lang)
        }
    }

    fun getLocalizedContext(context: Context, targetLanguage: String? = null): Context {
        val lang = targetLanguage ?: getPersistedLanguage(context)
        if (lang == LANGUAGE_SYSTEM) {
            return context
        }

        val locale = Locale(lang)
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
        val lang = targetLanguage ?: getPersistedLanguage(context)
        val config = Configuration(context.resources.configuration)
        if (lang != LANGUAGE_SYSTEM) {
            val locale = Locale(lang)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocales(LocaleList(locale))
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
            }
        }
        return config
    }
}
