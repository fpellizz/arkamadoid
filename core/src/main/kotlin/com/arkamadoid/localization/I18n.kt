package com.arkamadoid.localization

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.I18NBundle
import java.util.Locale

object I18n {
    private var bundle: I18NBundle? = null
    var currentLocale: Locale = Locale.ENGLISH
        private set

    fun load(language: String) {
        val locale = resolveLocale(language)
        currentLocale = locale
        bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/strings"), locale, "UTF-8")
    }

    operator fun get(key: String): String = bundle?.get(key) ?: key
    fun format(key: String, vararg args: Any?): String = bundle?.format(key, *args) ?: key

    /** "auto" → lingua di sistema (it/en supportate, fallback en). Altri valori passano dritti. */
    private fun resolveLocale(language: String): Locale = when (language) {
        "auto" -> {
            val sys = Locale.getDefault().language
            if (sys == "it") Locale.ITALIAN else Locale.ENGLISH
        }
        "it" -> Locale.ITALIAN
        "en" -> Locale.ENGLISH
        else -> Locale.ENGLISH
    }
}
