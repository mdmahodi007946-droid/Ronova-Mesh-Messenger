package com.example.core.locale

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Bengali-First Locale Helper for Ronova Mesh v2.0.
 * Enforces Bengali ("bn") across all contexts and configurations.
 * Per PRD v2.0 Section 4: All language-switching code and stored language preferences
 * are removed, enforcing Bengali as the default and only UI locale.
 */
object LocaleHelper {

    const val LOCALE_BANGLA = "bn"
    const val LANGUAGE_BANGLA = "bn"

    fun getPersistedLanguage(@Suppress("UNUSED_PARAMETER") context: Context): String {
        return LOCALE_BANGLA
    }

    fun getLocalizedContext(context: Context, @Suppress("UNUSED_PARAMETER") language: String = LOCALE_BANGLA): Context {
        val locale = Locale(LOCALE_BANGLA)
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

    fun getLocalizedConfiguration(context: Context, @Suppress("UNUSED_PARAMETER") language: String = LOCALE_BANGLA): Configuration {
        val locale = Locale(LOCALE_BANGLA)
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
