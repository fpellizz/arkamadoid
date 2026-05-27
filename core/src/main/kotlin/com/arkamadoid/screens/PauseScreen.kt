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

class PauseScreen(
    game: ArkamadoidGame,
    private val gameplay: GameplayScreen,
) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()
    private val tmpVec = Vector3()

    private val resumeRect = Rectangle((VIRTUAL_W - BTN_W) / 2f, 580f, BTN_W, BTN_H)
    private val quitRect = Rectangle((VIRTUAL_W - BTN_W) / 2f, 420f, BTN_W, BTN_H)

    private var elapsed = 0f

    override fun render(delta: Float) {
        elapsed += delta

        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        shapes.projectionMatrix = viewport.camera.combined
        batch.projectionMatrix = viewport.camera.combined

        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = Theme.Palette.SECONDARY_CONTAINER
        shapes.rect(resumeRect.x, resumeRect.y, resumeRect.width, resumeRect.height)
        shapes.color = Theme.Palette.ERROR
        shapes.rect(quitRect.x, quitRect.y, quitRect.width, quitRect.height)
        shapes.end()

        batch.begin()
        val title = game.fonts[Theme.FontSize.DISPLAY, true]
        title.color = Theme.Palette.PRIMARY_CONTAINER
        layout.setText(title, "PAUSED")
        title.draw(batch, "PAUSED", (VIRTUAL_W - layout.width) / 2f, 880f)

        val btnFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        btnFont.color = Theme.Palette.SECONDARY_CONTAINER
        layout.setText(btnFont, "RESUME")
        btnFont.draw(batch, "RESUME", resumeRect.x + (resumeRect.width - layout.width) / 2f, resumeRect.y + resumeRect.height / 2f + layout.height / 2f)

        btnFont.color = Theme.Palette.ERROR
        layout.setText(btnFont, "QUIT")
        btnFont.draw(batch, "QUIT", quitRect.x + (quitRect.width - layout.width) / 2f, quitRect.y + quitRect.height / 2f + layout.height / 2f)
        batch.end()

        if (Gdx.input.justTouched() && elapsed > 0.2f) {
            tmpVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            viewport.unproject(tmpVec)
            when {
                resumeRect.contains(tmpVec.x, tmpVec.y) -> {
                    game.setScreen(gameplay)
                }
                quitRect.contains(tmpVec.x, tmpVec.y) -> {
                    gameplay.dispose()
                    game.setScreen(AttractScreen(game))
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
        const val BTN_W = 400f
        const val BTN_H = 120f
    }
}
