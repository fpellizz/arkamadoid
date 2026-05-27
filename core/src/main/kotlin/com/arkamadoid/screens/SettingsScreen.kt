package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
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
        shapes.end()

        batch.begin()
        val title = game.fonts[Theme.FontSize.DISPLAY, true]
        title.color = Theme.Palette.TERTIARY
        layout.setText(title, "SETTINGS")
        title.draw(batch, "SETTINGS", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 120f)

        val wipFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        wipFont.color = Theme.Palette.ON_SURFACE_VARIANT
        layout.setText(wipFont, "CONFIG PANEL OFFLINE")
        wipFont.draw(batch, "CONFIG PANEL OFFLINE", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f + 40f)

        val sub = game.fonts[Theme.FontSize.BODY_MD]
        sub.color = Theme.Palette.ON_SURFACE_VARIANT
        layout.setText(sub, "audio/input controls coming soon")
        sub.draw(batch, "audio/input controls coming soon", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 20f)

        val backFont = game.fonts[Theme.FontSize.BODY_MD, true]
        backFont.color = Theme.Palette.ON_SURFACE_VARIANT
        layout.setText(backFont, "< BACK")
        backFont.draw(batch, "< BACK", backRect.x + (backRect.width - layout.width) / 2f, backRect.y + backRect.height / 2f + layout.height / 2f)
        batch.end()

        if (Gdx.input.justTouched() && elapsed > 0.15f) {
            tmp.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            viewport.unproject(tmp)
            if (backRect.contains(tmp.x, tmp.y)) {
                game.setScreen(MainMenuScreen(game))
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
