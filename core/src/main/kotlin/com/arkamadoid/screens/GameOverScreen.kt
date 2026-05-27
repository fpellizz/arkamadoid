package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.audio.MusicTrack
import com.arkamadoid.theme.Theme
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport

class GameOverScreen(
    game: ArkamadoidGame,
    val finalScore: Int,
    val finalLevel: Int = 1,
    val daily: Boolean = false,
) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()
    private val tmp = Vector3()
    private var elapsed = 0f
    private var rank = 0

    private var enteringInitials = false
    private val initials = charArrayOf('A', 'A', 'A')
    private var cursorIndex = 0

    private val letterCenters = floatArrayOf(VIRTUAL_W / 2f - 140f, VIRTUAL_W / 2f, VIRTUAL_W / 2f + 140f)
    private val letterRects = Array(3) { Rectangle(letterCenters[it] - 60f, VIRTUAL_H / 2f - 40f, 120f, 140f) }
    private val upArrowRect = Rectangle(VIRTUAL_W / 2f - 60f, VIRTUAL_H / 2f + 140f, 120f, 80f)
    private val downArrowRect = Rectangle(VIRTUAL_W / 2f - 60f, VIRTUAL_H / 2f - 140f, 120f, 80f)
    private val confirmRect = Rectangle((VIRTUAL_W - 360f) / 2f, 180f, 360f, 110f)

    override fun show() {
        if (finalScore > 0) {
            rank = game.prefs.submitScore("___", finalScore, finalLevel)
            enteringInitials = rank in 1..10
        }
        game.audio.playMusic(MusicTrack.GAME_OVER)
    }

    override fun render(delta: Float) {
        elapsed += delta

        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        shapes.projectionMatrix = viewport.camera.combined
        batch.projectionMatrix = viewport.camera.combined

        if (enteringInitials) drawInitialsPrompt() else drawGameOver()

        com.arkamadoid.render.BezelFrame.draw(shapes, viewport, VIRTUAL_W, VIRTUAL_H)

        handleInput()
    }

    private fun drawGameOver() {
        batch.begin()
        val title = game.fonts[Theme.FontSize.DISPLAY, true]
        title.color = Theme.Palette.ERROR
        layout.setText(title, "GAME OVER")
        title.draw(batch, "GAME OVER", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f + 160f)

        if (daily) {
            val dailyFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
            dailyFont.color = Theme.Palette.TERTIARY
            layout.setText(dailyFont, "DAILY CHALLENGE")
            dailyFont.draw(batch, "DAILY CHALLENGE", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f + 95f)
        }

        val scoreFont = game.fonts[Theme.FontSize.HEADLINE, true]
        scoreFont.color = Theme.Palette.TERTIARY
        val scoreLine = "SCORE %07d".format(finalScore)
        layout.setText(scoreFont, scoreLine)
        scoreFont.draw(batch, scoreLine, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f + 20f)

        if (daily) {
            val bestFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
            bestFont.color = Theme.Palette.PRIMARY
            val best = game.prefs.data.dailyBestScore
            val line = "TODAY BEST  %07d".format(best)
            layout.setText(bestFont, line)
            bestFont.draw(batch, line, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 30f)
        }

        if (rank in 1..10) {
            val rankFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
            rankFont.color = Theme.Palette.PRIMARY_CONTAINER
            val rankLine = "NEW HIGH SCORE  #%02d  AS  %s".format(rank, String(initials))
            layout.setText(rankFont, rankLine)
            rankFont.draw(batch, rankLine, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 60f)
        }

        val blink = (elapsed * 1.5f).toInt() % 2 == 0
        if (blink && elapsed > 0.8f) {
            val hint = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
            hint.color = Theme.Palette.SECONDARY_CONTAINER
            layout.setText(hint, "TAP TO CONTINUE")
            hint.draw(batch, "TAP TO CONTINUE", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 200f)
        }
        batch.end()
    }

    private fun drawInitialsPrompt() {
        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = Theme.Palette.PRIMARY_CONTAINER
        shapes.rect(confirmRect.x, confirmRect.y, confirmRect.width, confirmRect.height)
        shapes.color = Theme.Palette.SECONDARY_CONTAINER
        shapes.rect(upArrowRect.x, upArrowRect.y, upArrowRect.width, upArrowRect.height)
        shapes.rect(downArrowRect.x, downArrowRect.y, downArrowRect.width, downArrowRect.height)
        val cur = letterRects[cursorIndex]
        shapes.color = Theme.Palette.TERTIARY
        shapes.rect(cur.x, cur.y, cur.width, cur.height)
        shapes.end()

        batch.begin()
        val title = game.fonts[Theme.FontSize.HEADLINE, true]
        title.color = Theme.Palette.PRIMARY_CONTAINER
        val titleLine = "NEW HIGH SCORE  #%02d".format(rank)
        layout.setText(title, titleLine)
        title.draw(batch, titleLine, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 180f)

        val sub = game.fonts[Theme.FontSize.HEADLINE_MOBILE]
        sub.color = Theme.Palette.ON_SURFACE_VARIANT
        layout.setText(sub, "ENTER YOUR NAME")
        sub.draw(batch, "ENTER YOUR NAME", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 280f)

        val letterFont = game.fonts[Theme.FontSize.DISPLAY, true]
        for (i in 0..2) {
            val r = letterRects[i]
            letterFont.color = if (i == cursorIndex) Theme.Palette.TERTIARY else Theme.Palette.ON_SURFACE
            val ch = initials[i].toString()
            layout.setText(letterFont, ch)
            letterFont.draw(batch, ch, r.x + (r.width - layout.width) / 2f, r.y + r.height / 2f + layout.height / 2f)
        }

        val arrowFont = game.fonts[Theme.FontSize.HEADLINE, true]
        arrowFont.color = Theme.Palette.SECONDARY_CONTAINER
        layout.setText(arrowFont, "+")
        arrowFont.draw(batch, "+", upArrowRect.x + (upArrowRect.width - layout.width) / 2f, upArrowRect.y + upArrowRect.height / 2f + layout.height / 2f)
        layout.setText(arrowFont, "-")
        arrowFont.draw(batch, "-", downArrowRect.x + (downArrowRect.width - layout.width) / 2f, downArrowRect.y + downArrowRect.height / 2f + layout.height / 2f)

        val confirmFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        confirmFont.color = Theme.Palette.PRIMARY_CONTAINER
        layout.setText(confirmFont, "CONFIRM")
        confirmFont.draw(batch, "CONFIRM", confirmRect.x + (confirmRect.width - layout.width) / 2f, confirmRect.y + confirmRect.height / 2f + layout.height / 2f)
        batch.end()
    }

    private fun handleInput() {
        if (!Gdx.input.justTouched() || elapsed < 0.2f) return
        tmp.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        viewport.unproject(tmp)

        if (enteringInitials) {
            if (confirmRect.contains(tmp.x, tmp.y)) {
                confirmInitials()
                return
            }
            if (upArrowRect.contains(tmp.x, tmp.y)) {
                shiftLetter(+1)
                return
            }
            if (downArrowRect.contains(tmp.x, tmp.y)) {
                shiftLetter(-1)
                return
            }
            for (i in 0..2) {
                if (letterRects[i].contains(tmp.x, tmp.y)) {
                    cursorIndex = i
                    return
                }
            }
        } else if (elapsed > 1.0f) {
            game.setScreen(MainMenuScreen(game))
        }
    }

    private fun shiftLetter(delta: Int) {
        val current = initials[cursorIndex]
        val next = (((current - 'A') + delta + 26) % 26)
        initials[cursorIndex] = ('A' + next)
    }

    private fun confirmInitials() {
        val name = String(initials)
        if (rank in 1..10) {
            val list = game.prefs.data.highScores
            list[rank - 1] = list[rank - 1].copy(initials = name)
            game.prefs.save()
        }
        enteringInitials = false
        elapsed = 0f
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
