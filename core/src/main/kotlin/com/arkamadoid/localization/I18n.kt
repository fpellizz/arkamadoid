package com.arkamadoid.localization

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.I18NBundle
import java.util.Locale

object I18n {
    private var bundle: I18NBundle? = null

    fun load(language: String) {
        val locale = Locale(language)
        bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/strings"), locale)
    }

    operator fun get(key: String): String = bundle?.get(key) ?: key
    fun format(key: String, vararg args: Any?): String = bundle?.format(key, *args) ?: key
}
