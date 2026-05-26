package com.arkamadoid

import com.arkamadoid.audio.AudioManager
import com.arkamadoid.persistence.PreferencesStore
import com.arkamadoid.screens.BootScreen
import com.arkamadoid.services.PlatformServices
import com.arkamadoid.theme.FontManager
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx

class ArkamadoidGame(
    val platform: PlatformServices,
) : Game() {

    lateinit var prefs: PreferencesStore
        private set
    lateinit var audio: AudioManager
        private set
    lateinit var fonts: FontManager
        private set

    override fun create() {
        prefs = PreferencesStore()
        audio = AudioManager(prefs)
        fonts = FontManager()
        setScreen(BootScreen(this))
    }

    override fun render() {
        audio.update(Gdx.graphics.deltaTime)
        super.render()
    }

    override fun dispose() {
        screen?.dispose()
        fonts.dispose()
        audio.dispose()
    }
}
