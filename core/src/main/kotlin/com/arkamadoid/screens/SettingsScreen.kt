package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.input.InputMode
import com.arkamadoid.localization.I18n
import com.arkamadoid.theme.Theme
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport

class SettingsScreen(game: ArkamadoidGame) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()
    private val tmp = Vector3()
    private val backRect = Rectangle(40f, 40f, 180f, 90f)
    private var elapsed = 0f

    private data class Row(
        val label: () -> String,
        val getValue: () -> String,
        val onDec: () -> Unit,
        val onInc: () -> Unit,
    )

    private fun onOff(b: Boolean) = if (b) I18n["settings.on"] else I18n["settings.off"]

    private val rows = listOf(
        Row({ I18n["settings.music"] },
            { "%d%%".format((game.prefs.data.musicVolume * 100).toInt()) },
            { setMusicVol(game.prefs.data.musicVolume - 0.1f) },
            { setMusicVol(game.prefs.data.musicVolume + 0.1f) }),
        Row({ I18n["settings.sfx"] },
            { "%d%%".format((game.prefs.data.sfxVolume * 100).toInt()) },
            { setSfxVol(game.prefs.data.sfxVolume - 0.1f) },
            { setSfxVol(game.prefs.data.sfxVolume + 0.1f) }),
        Row({ I18n["settings.input"] },
            { if (game.prefs.data.inputMode == InputMode.DRAG_OFFSET) I18n["settings.input.drag"] else I18n["settings.input.absolute"] },
            { toggleInput() },
            { toggleInput() }),
        Row({ I18n["settings.sensitivity"] },
            { "%.1fx".format(game.prefs.data.sensitivity) },
            { setSensitivity(game.prefs.data.sensitivity - 0.1f) },
            { setSensitivity(game.prefs.data.sensitivity + 0.1f) }),
        Row({ I18n["settings.crt"] },
            { onOff(game.prefs.data.crtShader) },
            { toggleCrt() },
            { toggleCrt() }),
        Row({ I18n["settings.haptics"] },
            { onOff(game.prefs.data.haptics) },
            { toggleHaptics() },
            { toggleHaptics() }),
        Row({ I18n["settings.reduceMotion"] },
            { onOff(game.prefs.data.reduceMotion) },
            { toggleReduceMotion() },
            { toggleReduceMotion() }),
        Row({ I18n["settings.language"] },
            { I18n["settings.lang.${game.prefs.data.language}"] },
            { cycleLanguage(-1) },
            { cycleLanguage(1) }),
        Row({ I18n["settings.privacy"] },
            { ">" },
            { openPrivacy() },
            { openPrivacy() }),
    )

    private val decRects: List<Rectangle>
    private val incRects: List<Rectangle>

    init {
        val rowH = 75f
        val rowGap = 15f
        val startY = 1000f
        decRects = rows.indices.map { Rectangle(40f, startY - it * (rowH + rowGap), 80f, rowH) }
        incRects = rows.indices.map { Rectangle(VIRTUAL_W - 120f, startY - it * (rowH + rowGap), 80f, rowH) }
    }

    private fun setMusicVol(v: Float) {
        game.prefs.data.musicVolume = ((v * 10).toInt() / 10f).coerceIn(0f, 1f)
        game.prefs.save()
        game.audio.applyVolume()
    }

    private fun setSfxVol(v: Float) {
        game.prefs.data.sfxVolume = ((v * 10).toInt() / 10f).coerceIn(0f, 1f)
        game.prefs.save()
    }

    private fun toggleInput() {
        val d = game.prefs.data
        d.inputMode = if (d.inputMode == InputMode.DRAG_OFFSET) InputMode.TOUCH_ABSOLUTE else InputMode.DRAG_OFFSET
        game.prefs.save()
    }

    private fun setSensitivity(v: Float) {
        game.prefs.data.sensitivity = ((v * 10).toInt() / 10f).coerceIn(0.5f, 2.0f)
        game.prefs.save()
    }

    private fun toggleCrt() {
        game.prefs.data.crtShader = !game.prefs.data.crtShader
        game.prefs.save()
    }

    private fun toggleHaptics() {
        game.prefs.data.haptics = !game.prefs.data.haptics
        game.prefs.save()
    }

    private fun toggleReduceMotion() {
        game.prefs.data.reduceMotion = !game.prefs.data.reduceMotion
        game.prefs.save()
    }

    private fun cycleLanguage(direction: Int) {
        val order = listOf("auto", "it", "en")
        val cur = order.indexOf(game.prefs.data.language).let { if (it < 0) 0 else it }
        val next = ((cur + direction) % order.size + order.size) % order.size
        game.prefs.data.language = order[next]
        game.prefs.save()
        I18n.load(game.prefs.data.language)
    }

    private fun openPrivacy() {
        game.platform.openUrl(PRIVACY_URL)
    }

    override fun render(delta: Float) {
        elapsed += delta

        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        shapes.projectionMatrix = viewport.camera.combined
        batch.projectionMatrix = viewport.camera.combined

        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = Theme.Palette.ON_SURFACE_VARIANT
        shapes.rect(backRect.x, backRect.y, backRect.width, backRect.height)
        for (i in rows.indices) {
            shapes.color = Theme.Palette.SECONDARY_CONTAINER
            shapes.rect(decRects[i].x, decRects[i].y, decRects[i].width, decRects[i].height)
            shapes.color = Theme.Palette.PRIMARY_CONTAINER
            shapes.rect(incRects[i].x, incRects[i].y, incRects[i].width, incRects[i].height)
        }
        shapes.end()

        batch.begin()
        val title = game.fonts[Theme.FontSize.DISPLAY, true]
        title.color = Theme.Palette.TERTIARY
        val titleTxt = I18n["settings.title"]
        layout.setText(title, titleTxt)
        title.draw(batch, titleTxt, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 120f)

        val labelFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        val valueFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        val arrowFont = game.fonts[Theme.FontSize.HEADLINE, true]

        for (i in rows.indices) {
            val r = rows[i]
            val dec = decRects[i]
            val inc = incRects[i]
            val midY = dec.y + dec.height / 2f

            arrowFont.color = Theme.Palette.SECONDARY_CONTAINER
            layout.setText(arrowFont, "<")
            arrowFont.draw(batch, "<", dec.x + (dec.width - layout.width) / 2f, midY + layout.height / 2f)

            arrowFont.color = Theme.Palette.PRIMARY_CONTAINER
            layout.setText(arrowFont, ">")
            arrowFont.draw(batch, ">", inc.x + (inc.width - layout.width) / 2f, midY + layout.height / 2f)

            val labelTxt = r.label()
            labelFont.color = Theme.Palette.ON_SURFACE
            layout.setText(labelFont, labelTxt)
            labelFont.draw(batch, labelTxt, dec.x + dec.width + 30f, midY + layout.height / 2f)

            valueFont.color = Theme.Palette.TERTIARY
            val v = r.getValue()
            layout.setText(valueFont, v)
            valueFont.draw(batch, v, inc.x - 30f - layout.width, midY + layout.height / 2f)
        }

        val backFont = game.fonts[Theme.FontSize.BODY_MD, true]
        backFont.color = Theme.Palette.ON_SURFACE_VARIANT
        val backTxt = "< ${I18n["nav.back"]}"
        layout.setText(backFont, backTxt)
        backFont.draw(batch, backTxt, backRect.x + (backRect.width - layout.width) / 2f, backRect.y + backRect.height / 2f + layout.height / 2f)
        batch.end()

        com.arkamadoid.render.BezelFrame.draw(shapes, viewport, VIRTUAL_W, VIRTUAL_H)

        if (Gdx.input.justTouched() && elapsed > 0.15f) {
            tmp.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            viewport.unproject(tmp)
            if (backRect.contains(tmp.x, tmp.y)) {
                game.setScreen(MainMenuScreen(game))
                return
            }
            for (i in rows.indices) {
                if (decRects[i].contains(tmp.x, tmp.y)) {
                    rows[i].onDec()
                    return
                }
                if (incRects[i].contains(tmp.x, tmp.y)) {
                    rows[i].onInc()
                    return
                }
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun hide() = dispose()

    override fun dispose() {
        batch.dispose()
        shapes.dispose()
    }

    companion object {
        const val VIRTUAL_W = 720f
        const val VIRTUAL_H = 1280f
        const val PRIVACY_URL = "https://github.com/fpellizz/arkamadoid/blob/main/PRIVACY.md"
    }
}
