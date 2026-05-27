package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
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

class ModeSelectScreen(game: ArkamadoidGame) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()
    private val tmp = Vector3()
    private var elapsed = 0f

    private val modes = listOf(
        Mode("ARCADE", "STORY-DRIVEN, 24 SECTORS", Theme.Palette.PRIMARY_CONTAINER, GameplayScreen.GameMode.ARCADE),
        Mode("ENDLESS", "NO END, JUST SURVIVE", Theme.Palette.SECONDARY_CONTAINER, GameplayScreen.GameMode.ENDLESS),
        Mode("DAILY", "ONE SHARED LEVEL PER DAY", Theme.Palette.TERTIARY, GameplayScreen.GameMode.DAILY),
        Mode("PRACTICE", "NO DEATH, NO SCORE", Theme.Palette.PRIMARY_FIXED, GameplayScreen.GameMode.PRACTICE),
    )

    private val modeRects: List<Rectangle> = run {
        val totalH = modes.size * CARD_H + (modes.size - 1) * GAP
        val startY = (VIRTUAL_H - totalH) / 2f - 60f
        modes.mapIndexed { i, _ ->
            Rectangle((VIRTUAL_W - CARD_W) / 2f, startY + (modes.size - 1 - i) * (CARD_H + GAP), CARD_W, CARD_H)
        }
    }

    private val backRect = Rectangle(40f, 40f, 180f, 90f)

    override fun render(delta: Float) {
        elapsed += delta

        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        shapes.projectionMatrix = viewport.camera.combined
        batch.projectionMatrix = viewport.camera.combined

        shapes.begin(ShapeRenderer.ShapeType.Line)
        modes.forEachIndexed { i, m ->
            shapes.color = m.color
            val r = modeRects[i]
            shapes.rect(r.x, r.y, r.width, r.height)
        }
        shapes.color = Theme.Palette.ON_SURFACE_VARIANT
        shapes.rect(backRect.x, backRect.y, backRect.width, backRect.height)
        shapes.end()

        batch.begin()
        val title = game.fonts[Theme.FontSize.HEADLINE, true]
        title.color = Theme.Palette.PRIMARY
        layout.setText(title, "SELECT MODE")
        title.draw(batch, "SELECT MODE", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 100f)

        val nameFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        val descFont = game.fonts[Theme.FontSize.BODY_MD]
        modes.forEachIndexed { i, m ->
            val r = modeRects[i]
            nameFont.color = m.color
            layout.setText(nameFont, m.name)
            nameFont.draw(batch, m.name, r.x + (r.width - layout.width) / 2f, r.y + r.height / 2f + 30f)

            descFont.color = Theme.Palette.ON_SURFACE_VARIANT
            layout.setText(descFont, m.desc)
            descFont.draw(batch, m.desc, r.x + (r.width - layout.width) / 2f, r.y + r.height / 2f - 18f)
        }

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
                return
            }
            for (i in modes.indices) {
                if (modeRects[i].contains(tmp.x, tmp.y)) {
                    game.setScreen(GameplayScreen(game, modes[i].mode))
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

    private data class Mode(val name: String, val desc: String, val color: Color, val mode: GameplayScreen.GameMode)

    companion object {
        const val VIRTUAL_W = 720f
        const val VIRTUAL_H = 1280f
        const val CARD_W = 540f
        const val CARD_H = 140f
        const val GAP = 30f
    }
}
