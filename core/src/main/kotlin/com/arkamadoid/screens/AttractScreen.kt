package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.theme.Theme
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.sin

class AttractScreen(game: ArkamadoidGame) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()
    private var elapsed = 0f

    override fun render(delta: Float) {
        elapsed += delta

        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        batch.projectionMatrix = viewport.camera.combined
        shapes.projectionMatrix = viewport.camera.combined

        // griglia di sfondo neon (drawn first)
        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = Theme.Palette.PRIMARY_FIXED_DIM.apply { a = 0.08f }
        val gridStep = 40f
        var x = 0f
        while (x < VIRTUAL_W) {
            shapes.line(x, 0f, x, VIRTUAL_H)
            x += gridStep
        }
        var y = 0f
        while (y < VIRTUAL_H) {
            shapes.line(0f, y, VIRTUAL_W, y)
            y += gridStep
        }
        shapes.end()
        Theme.Palette.PRIMARY_FIXED_DIM.a = 1f

        batch.begin()

        // LOGO ARKAMADOID centrale, con pulsazione leggera
        val pulse = 1f + 0.04f * sin(elapsed * 2f)
        val titleFont = game.fonts[Theme.FontSize.DISPLAY, true]
        titleFont.color = Theme.Palette.PRIMARY
        titleFont.data.setScale(pulse)
        layout.setText(titleFont, "ARKAMADOID")
        titleFont.draw(batch, "ARKAMADOID", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f + 200f)
        titleFont.data.setScale(1f)

        // INSERT COIN - blink 1.5 Hz
        val blink = (elapsed * 1.5f).toInt() % 2 == 0
        if (blink) {
            val coinFont = game.fonts[Theme.FontSize.HEADLINE, true]
            coinFont.color = Theme.Palette.PRIMARY_CONTAINER
            layout.setText(coinFont, "INSERT COIN")
            coinFont.draw(batch, "INSERT COIN", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 80f)
        }

        // PRESS START (sempre visibile, leggermente più piccolo)
        val pressFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        pressFont.color = Theme.Palette.SECONDARY_CONTAINER
        layout.setText(pressFont, "TAP TO START")
        pressFont.draw(batch, "TAP TO START", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 160f)

        // High score banner placeholder (in stile mockup)
        val hsLabel = game.fonts[Theme.FontSize.LABEL_SM, true]
        hsLabel.color = Theme.Palette.SECONDARY_FIXED_DIM
        hsLabel.draw(batch, "HI-SCORE  005000", 60f, VIRTUAL_H - 60f)

        val onepLabel = game.fonts[Theme.FontSize.LABEL_SM, true]
        onepLabel.color = Theme.Palette.PRIMARY
        layout.setText(onepLabel, "1P SCORE  000000")
        onepLabel.draw(batch, layout, VIRTUAL_W - 60f - layout.width, VIRTUAL_H - 60f)

        // footer copyright
        val footFont = game.fonts[Theme.FontSize.LABEL_SM]
        footFont.color = Theme.Palette.ON_SURFACE_VARIANT
        layout.setText(footFont, "v0.1.0  -  (C) 2026 ARKAMADOID INDUSTRIES")
        footFont.draw(batch, layout, (VIRTUAL_W - layout.width) / 2f, 40f)

        batch.end()

        com.arkamadoid.render.BezelFrame.draw(shapes, viewport, VIRTUAL_W, VIRTUAL_H)

        if (Gdx.input.justTouched() && elapsed > 0.5f) {
            game.setScreen(MainMenuScreen(game))
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun hide() {
        dispose()
    }

    override fun dispose() {
        batch.dispose()
        shapes.dispose()
    }

    companion object {
        const val VIRTUAL_W = 720f
        const val VIRTUAL_H = 1280f
    }
}
