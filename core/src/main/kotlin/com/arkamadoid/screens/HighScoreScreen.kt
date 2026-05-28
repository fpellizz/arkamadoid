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

class HighScoreScreen(game: ArkamadoidGame) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()
    private val tmp = Vector3()
    private val backRect = Rectangle(40f, 40f, 180f, 90f)
    private var elapsed = 0f

    // tab di selezione del mode
    private val modes = listOf("ARCADE", "ENDLESS", "DAILY")
    private val modeColor = mapOf(
        "ARCADE" to Theme.Palette.PRIMARY_CONTAINER,
        "ENDLESS" to Theme.Palette.SECONDARY_CONTAINER,
        "DAILY" to Theme.Palette.TERTIARY,
    )
    private var selectedMode = 0

    private val tabRects: List<Rectangle> = run {
        val tabW = 200f
        val tabH = 80f
        val gap = 16f
        val totalW = modes.size * tabW + (modes.size - 1) * gap
        val startX = (VIRTUAL_W - totalW) / 2f
        modes.indices.map { i ->
            Rectangle(startX + i * (tabW + gap), VIRTUAL_H - 320f, tabW, tabH)
        }
    }

    override fun render(delta: Float) {
        elapsed += delta

        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        viewport.apply()
        shapes.projectionMatrix = viewport.camera.combined
        batch.projectionMatrix = viewport.camera.combined

        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = Theme.Palette.ON_SURFACE_VARIANT
        shapes.rect(backRect.x, backRect.y, backRect.width, backRect.height)
        modes.forEachIndexed { i, m ->
            shapes.color = modeColor[m] ?: Theme.Palette.ON_SURFACE
            val r = tabRects[i]
            shapes.rect(r.x, r.y, r.width, r.height)
        }
        shapes.end()

        // sotto-evidenzia il tab selezionato
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        val selR = tabRects[selectedMode]
        shapes.color = modeColor[modes[selectedMode]] ?: Theme.Palette.ON_SURFACE
        shapes.rect(selR.x, selR.y - 4f, selR.width, 4f)
        shapes.end()

        batch.begin()
        val title = game.fonts[Theme.FontSize.DISPLAY, true]
        title.color = Theme.Palette.SECONDARY_CONTAINER
        layout.setText(title, "SCORES")
        title.draw(batch, "SCORES", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 120f)

        val tabFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        modes.forEachIndexed { i, m ->
            tabFont.color = if (i == selectedMode) (modeColor[m] ?: Theme.Palette.ON_SURFACE)
                            else Theme.Palette.ON_SURFACE_VARIANT
            layout.setText(tabFont, m)
            val r = tabRects[i]
            tabFont.draw(batch, m, r.x + (r.width - layout.width) / 2f, r.y + r.height / 2f + layout.height / 2f)
        }

        val scores = game.prefs.highScoresFor(modes[selectedMode])
        if (scores.isEmpty()) {
            val emptyFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
            emptyFont.color = Theme.Palette.ON_SURFACE_VARIANT
            layout.setText(emptyFont, "DATA_STORE EMPTY")
            emptyFont.draw(batch, "DATA_STORE EMPTY", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f + 40f)

            val sub = game.fonts[Theme.FontSize.BODY_MD]
            sub.color = Theme.Palette.ON_SURFACE_VARIANT
            layout.setText(sub, "no runs recorded yet for ${modes[selectedMode]}")
            sub.draw(batch, "no runs recorded yet for ${modes[selectedMode]}", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 20f)
        } else {
            val rowFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
            val labelFont = game.fonts[Theme.FontSize.BODY_MD]
            labelFont.color = Theme.Palette.ON_SURFACE_VARIANT
            val header = "#   NAME    SCORE      SECT"
            layout.setText(labelFont, header)
            labelFont.draw(batch, header, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 440f)

            val rowH = 56f
            val startY = VIRTUAL_H - 480f
            scores.forEachIndexed { i, entry ->
                val rank = i + 1
                rowFont.color = when (rank) {
                    1 -> Theme.Palette.TERTIARY
                    2 -> Theme.Palette.SECONDARY_FIXED
                    3 -> Theme.Palette.PRIMARY_FIXED
                    else -> Theme.Palette.ON_SURFACE
                }
                val line = "%02d  %-3s   %07d   %02d".format(rank, entry.initials, entry.score, entry.level)
                layout.setText(rowFont, line)
                rowFont.draw(batch, line, (VIRTUAL_W - layout.width) / 2f, startY - i * rowH)
            }
        }

        val backFont = game.fonts[Theme.FontSize.BODY_MD, true]
        backFont.color = Theme.Palette.ON_SURFACE_VARIANT
        layout.setText(backFont, "< BACK")
        backFont.draw(batch, "< BACK", backRect.x + (backRect.width - layout.width) / 2f, backRect.y + backRect.height / 2f + layout.height / 2f)
        batch.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        com.arkamadoid.render.BezelFrame.draw(shapes, viewport, VIRTUAL_W, VIRTUAL_H)

        if (Gdx.input.justTouched() && elapsed > 0.15f) {
            tmp.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            viewport.unproject(tmp)
            if (backRect.contains(tmp.x, tmp.y)) {
                game.setScreen(MainMenuScreen(game))
                return
            }
            for (i in tabRects.indices) {
                if (tabRects[i].contains(tmp.x, tmp.y)) {
                    selectedMode = i
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
