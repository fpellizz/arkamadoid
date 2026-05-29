package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.achievements.Achievement
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

class AchievementsScreen(game: ArkamadoidGame) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()
    private val tmp = Vector3()
    private val backRect = Rectangle(40f, 40f, 180f, 90f)
    private var elapsed = 0f

    private val items = Achievement.values().toList()

    private val itemRects: List<Rectangle> = run {
        val startY = VIRTUAL_H - 260f
        items.indices.map { i ->
            Rectangle(40f, startY - i * (ROW_H + GAP) - ROW_H, VIRTUAL_W - 80f, ROW_H)
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

        val unlocked = game.prefs.data.unlockedAchievements

        // borders dei card e back button
        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = Theme.Palette.ON_SURFACE_VARIANT
        shapes.rect(backRect.x, backRect.y, backRect.width, backRect.height)
        for (i in items.indices) {
            val r = itemRects[i]
            val isUnlocked = items[i].id in unlocked
            shapes.color = if (isUnlocked) Theme.Palette.NEON_GREEN else Theme.Palette.OUTLINE_VARIANT
            shapes.rect(r.x, r.y, r.width, r.height)
        }
        shapes.end()

        // text overlay
        batch.begin()
        val title = game.fonts[Theme.FontSize.DISPLAY, true]
        title.color = Theme.Palette.PRIMARY
        val titleTxt = I18n["achievement.screen.title"]
        layout.setText(title, titleTxt)
        title.draw(batch, titleTxt, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 120f)

        // progress counter X/9 sotto il titolo
        val progressFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        progressFont.color = Theme.Palette.TERTIARY
        val unlockedCount = items.count { it.id in unlocked }
        val progressTxt = I18n.format("achievement.progress", unlockedCount, items.size)
        layout.setText(progressFont, progressTxt)
        progressFont.draw(batch, progressTxt, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 200f)

        val iconFont = game.fonts[Theme.FontSize.HEADLINE, true]
        val titleFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        val descFont = game.fonts[Theme.FontSize.BODY_MD]

        for (i in items.indices) {
            val ach = items[i]
            val r = itemRects[i]
            val isUnlocked = ach.id in unlocked
            val showAsLocked = !isUnlocked && ach.hidden

            // checkbox unlocked / locked, sinistra
            iconFont.color = if (isUnlocked) Theme.Palette.NEON_GREEN else Theme.Palette.OUTLINE_VARIANT
            val icon = if (isUnlocked) "[X]" else "[ ]"
            layout.setText(iconFont, icon)
            iconFont.draw(batch, icon, r.x + 20f, r.y + r.height / 2f + layout.height / 2f)

            // title centrato verticalmente, parte alta del card
            titleFont.color = when {
                isUnlocked -> Theme.Palette.ON_SURFACE
                else -> Theme.Palette.ON_SURFACE_VARIANT
            }
            val titleStr = if (showAsLocked) I18n["achievement.locked.title"]
                           else I18n["achievement.${ach.id}.title"]
            titleFont.draw(batch, titleStr, r.x + 110f, r.y + r.height - 18f)

            // desc sotto al title
            descFont.color = Theme.Palette.ON_SURFACE_VARIANT
            val descStr = if (showAsLocked) I18n["achievement.locked.desc"]
                          else I18n["achievement.${ach.id}.desc"]
            descFont.draw(batch, descStr, r.x + 110f, r.y + 18f)
        }

        val backFont = game.fonts[Theme.FontSize.BODY_MD, true]
        backFont.color = Theme.Palette.ON_SURFACE_VARIANT
        val backTxt = "< ${I18n["nav.back"]}"
        layout.setText(backFont, backTxt)
        backFont.draw(batch, backTxt, backRect.x + (backRect.width - layout.width) / 2f, backRect.y + backRect.height / 2f + layout.height / 2f)
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
        const val ROW_H = 70f
        const val GAP = 8f
    }
}
