package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.input.InputMode
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
        val label: String,
        val getValue: () -> String,
        val onDec: () -> Unit,
        val onInc: () -> Unit,
    )

    private val rows = listOf(
        Row("MUSIC",
            { "%d%%".format((game.prefs.data.musicVolume * 100).toInt()) },
            { setMusicVol(game.prefs.data.musicVolume - 0.1f) },
            { setMusicVol(game.prefs.data.musicVolume + 0.1f) }),
        Row("SFX",
            { "%d%%".format((game.prefs.data.sfxVolume * 100).toInt()) },
            { setSfxVol(game.prefs.data.sfxVolume - 0.1f) },
            { setSfxVol(game.prefs.data.sfxVolume + 0.1f) }),
        Row("INPUT",
            { if (game.prefs.data.inputMode == InputMode.DRAG_OFFSET) "DRAG" else "ABSOLUTE" },
            { toggleInput() },
            { toggleInput() }),
        Row("CRT FX",
            { if (game.prefs.data.crtShader) "ON" else "OFF" },
            { toggleCrt() },
            { toggleCrt() }),
        Row("HAPTICS",
            { if (game.prefs.data.haptics) "ON" else "OFF" },
            { toggleHaptics() },
            { toggleHaptics() }),
    )

    private val decRects: List<Rectangle>
    private val incRects: List<Rectangle>

    init {
        val rowH = 90f
        val rowGap = 20f
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

    private fun toggleCrt() {
        game.prefs.data.crtShader = !game.prefs.data.crtShader
        game.prefs.save()
    }

    private fun toggleHaptics() {
        game.prefs.data.haptics = !game.prefs.data.haptics
        game.prefs.save()
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
        layout.setText(title, "SETTINGS")
        title.draw(batch, "SETTINGS", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 120f)

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

            labelFont.color = Theme.Palette.ON_SURFACE
            layout.setText(labelFont, r.label)
            labelFont.draw(batch, r.label, dec.x + dec.width + 30f, midY + layout.height / 2f)

            valueFont.color = Theme.Palette.TERTIARY
            val v = r.getValue()
            layout.setText(valueFont, v)
            valueFont.draw(batch, v, inc.x - 30f - layout.width, midY + layout.height / 2f)
        }

        val backFont = game.fonts[Theme.FontSize.BODY_MD, true]
        backFont.color = Theme.Palette.ON_SURFACE_VARIANT
        layout.setText(backFont, "< BACK")
        backFont.draw(batch, "< BACK", backRect.x + (backRect.width - layout.width) / 2f, backRect.y + backRect.height / 2f + layout.height / 2f)
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
    }
}
