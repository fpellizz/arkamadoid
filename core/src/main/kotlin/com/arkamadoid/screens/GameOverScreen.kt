package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.audio.MusicTrack
import com.arkamadoid.theme.Theme
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport

class GameOverScreen(
    game: ArkamadoidGame,
    val finalScore: Int,
) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()
    private var elapsed = 0f

    override fun show() {
        game.audio.playMusic(MusicTrack.GAME_OVER)
    }

    override fun render(delta: Float) {
        elapsed += delta

        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        batch.projectionMatrix = viewport.camera.combined
        batch.begin()

        val title = game.fonts[Theme.FontSize.DISPLAY, true]
        title.color = Theme.Palette.ERROR
        layout.setText(title, "GAME OVER")
        title.draw(batch, "GAME OVER", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f + 120f)

        val scoreFont = game.fonts[Theme.FontSize.HEADLINE, true]
        scoreFont.color = Theme.Palette.TERTIARY
        val scoreLine = "SCORE %07d".format(finalScore)
        layout.setText(scoreFont, scoreLine)
        scoreFont.draw(batch, scoreLine, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 20f)

        val blink = (elapsed * 1.5f).toInt() % 2 == 0
        if (blink && elapsed > 0.8f) {
            val hint = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
            hint.color = Theme.Palette.SECONDARY_CONTAINER
            layout.setText(hint, "TAP TO CONTINUE")
            hint.draw(batch, "TAP TO CONTINUE", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 180f)
        }

        batch.end()

        if (Gdx.input.justTouched() && elapsed > 1.2f) {
            game.setScreen(AttractScreen(game))
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun hide() = dispose()

    override fun dispose() {
        batch.dispose()
    }

    companion object {
        const val VIRTUAL_W = 720f
        const val VIRTUAL_H = 1280f
    }
}
