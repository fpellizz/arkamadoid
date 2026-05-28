package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.audio.MusicTrack
import com.arkamadoid.config.GameConfig
import com.arkamadoid.theme.Theme
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport

class MainMenuScreen(game: ArkamadoidGame) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()
    private val tmp = Vector3()
    private var elapsed = 0f
    private var idleTime = 0f

    private val items = listOf(
        Item("PLAY", Theme.Palette.PRIMARY_CONTAINER) { game.setScreen(ModeSelectScreen(game)) },
        Item("SCORES", Theme.Palette.SECONDARY_CONTAINER) { game.setScreen(HighScoreScreen(game)) },
        Item("SETTINGS", Theme.Palette.TERTIARY) { game.setScreen(SettingsScreen(game)) },
        Item("HOME", Theme.Palette.PRIMARY_FIXED) { game.setScreen(AttractScreen(game)) },
        Item("EXIT", Theme.Palette.ERROR) { game.platform.exitApp() },
    )

    private val itemRects: List<Rectangle> = run {
        val totalH = items.size * BTN_H + (items.size - 1) * GAP
        val startY = (VIRTUAL_H - totalH) / 2f - 80f
        items.mapIndexed { i, _ ->
            Rectangle((VIRTUAL_W - BTN_W) / 2f, startY + (items.size - 1 - i) * (BTN_H + GAP), BTN_W, BTN_H)
        }
    }

    override fun show() {
        game.audio.playMusic(MusicTrack.MENU)
    }

    override fun render(delta: Float) {
        elapsed += delta
        idleTime += delta
        if (idleTime > GameConfig.ATTRACT_TIMEOUT_SECONDS) {
            game.setScreen(AttractScreen(game))
            return
        }

        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        shapes.projectionMatrix = viewport.camera.combined
        batch.projectionMatrix = viewport.camera.combined

        shapes.begin(ShapeRenderer.ShapeType.Line)
        items.forEachIndexed { i, item ->
            shapes.color = item.color
            val r = itemRects[i]
            shapes.rect(r.x, r.y, r.width, r.height)
        }
        shapes.end()

        batch.begin()
        val title = game.fonts[Theme.FontSize.DISPLAY, true]
        title.color = Theme.Palette.PRIMARY
        layout.setText(title, "ARKAMADOID")
        title.draw(batch, "ARKAMADOID", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 100f)

        val btnFont = game.fonts[Theme.FontSize.HEADLINE, true]
        items.forEachIndexed { i, item ->
            btnFont.color = item.color
            layout.setText(btnFont, item.label)
            val r = itemRects[i]
            btnFont.draw(batch, item.label, r.x + (r.width - layout.width) / 2f, r.y + r.height / 2f + layout.height / 2f)
        }

        val foot = game.fonts[Theme.FontSize.LABEL_SM]
        foot.color = Theme.Palette.ON_SURFACE_VARIANT
        layout.setText(foot, "v0.1.0  -  (C) 2026 ARKAMADOID INDUSTRIES")
        foot.draw(batch, layout, (VIRTUAL_W - layout.width) / 2f, 40f)
        batch.end()

        com.arkamadoid.render.BezelFrame.draw(shapes, viewport, VIRTUAL_W, VIRTUAL_H)

        if (Gdx.input.justTouched() && elapsed > 0.15f) {
            idleTime = 0f
            tmp.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            viewport.unproject(tmp)
            for (i in items.indices) {
                if (itemRects[i].contains(tmp.x, tmp.y)) {
                    items[i].onClick()
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

    private data class Item(val label: String, val color: Color, val onClick: () -> Unit)

    companion object {
        const val VIRTUAL_W = 720f
        const val VIRTUAL_H = 1280f
        const val BTN_W = 480f
        const val BTN_H = 120f
        const val GAP = 30f
    }
}
