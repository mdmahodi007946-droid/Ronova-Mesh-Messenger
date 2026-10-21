package com.example.core.locale

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Bengali-First localization helper.
 * Strictly forces and preserves Bengali (bn) locale across all Android versions.
 */
object LocaleHelper {

    const val LOCALE_BANGLA = "bn"

    fun getPersistedLanguage(context: Context): String = LOCALE_BANGLA

    fun getLocalizedContext(context: Context): Context {
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

    fun getLocalizedConfiguration(context: Context): Configuration {
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
