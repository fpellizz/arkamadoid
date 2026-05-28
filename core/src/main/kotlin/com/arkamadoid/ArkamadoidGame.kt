package com.arkamadoid

import com.arkamadoid.audio.AudioManager
import com.arkamadoid.localization.I18n
import com.arkamadoid.persistence.PreferencesStore
import com.arkamadoid.render.CrtRenderer
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
    lateinit var crt: CrtRenderer
        private set

    override fun create() {
        prefs = PreferencesStore()
        I18n.load(prefs.data.language)
        audio = AudioManager(prefs)
        fonts = FontManager()
        crt = CrtRenderer()
        crt.resize(Gdx.graphics.width, Gdx.graphics.height)
        platform.registerAudioFocusCallbacks(
            onLoss = { audio.pauseForFocus() },
            onGain = { audio.resumeFromFocus() },
        )
        // silent best-effort sign-in: se GPGS è configurato proviamo, altrimenti no-op
        platform.gpgs.signIn()
        setScreen(BootScreen(this))
    }

    override fun render() {
        audio.update(Gdx.graphics.deltaTime)
        if (prefs.data.crtShader) {
            crt.beginCapture()
            super.render()
            crt.endCaptureAndDraw()
        } else {
            super.render()
        }
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        crt.resize(width, height)
    }

    /**
     * Defensive save su perdita focus (RNF-R-02): la maggior parte degli stati è già
     * salvata eagerly quando cambia (settings, achievement, score), ma se la run
     * sblocca qualcosa proprio prima che l'OS killi il processo, vogliamo flushare.
     */
    override fun pause() {
        super.pause()
        if (::prefs.isInitialized) prefs.save()
    }

    override fun dispose() {
        screen?.dispose()
        fonts.dispose()
        audio.dispose()
        crt.dispose()
    }
}
