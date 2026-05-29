package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.localization.I18n
import com.arkamadoid.render.PlayfieldRenderer
import com.arkamadoid.skins.BallSkin
import com.arkamadoid.skins.PaddleSkin
import com.arkamadoid.skins.PaletteSkin
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

class SkinsScreen(game: ArkamadoidGame) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()
    private val tmp = Vector3()
    private val backRect = Rectangle(40f, 40f, 180f, 90f)
    private var elapsed = 0f

    private enum class Tab { PADDLE, BALL, PALETTE }
    private var selectedTab = Tab.PADDLE

    private val tabs = Tab.values().toList()
    private val tabRects: List<Rectangle> = run {
        val tabW = 200f
        val tabH = 80f
        val gap = 16f
        val totalW = tabs.size * tabW + (tabs.size - 1) * gap
        val startX = (VIRTUAL_W - totalW) / 2f
        tabs.indices.map { i ->
            Rectangle(startX + i * (tabW + gap), VIRTUAL_H - 320f, tabW, tabH)
        }
    }

    private fun rowRects(count: Int): List<Rectangle> {
        val startY = VIRTUAL_H - 440f
        return (0 until count).map { i ->
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

        val rows = currentRowRects()

        // borders e preview swatches (Filled per gli swatch, Line per i border)
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        drawSwatches(rows)
        shapes.end()

        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = Theme.Palette.ON_SURFACE_VARIANT
        shapes.rect(backRect.x, backRect.y, backRect.width, backRect.height)
        // tab outlines
        tabs.forEachIndexed { i, _ ->
            shapes.color = if (i == selectedTab.ordinal) Theme.Palette.PRIMARY_CONTAINER else Theme.Palette.OUTLINE_VARIANT
            val r = tabRects[i]
            shapes.rect(r.x, r.y, r.width, r.height)
        }
        // row borders (verde se selected, grigio dim se locked, altrimenti accent del tab)
        forEachItem(rows) { i, _, isUnlocked, isSelected ->
            val r = rows[i]
            shapes.color = when {
                isSelected -> Theme.Palette.NEON_GREEN
                isUnlocked -> Theme.Palette.PRIMARY_FIXED
                else -> Theme.Palette.OUTLINE_VARIANT
            }
            shapes.rect(r.x, r.y, r.width, r.height)
        }
        shapes.end()

        // testi
        batch.begin()

        val title = game.fonts[Theme.FontSize.DISPLAY, true]
        title.color = Theme.Palette.PRIMARY
        val titleTxt = I18n["skin.screen.title"]
        layout.setText(title, titleTxt)
        title.draw(batch, titleTxt, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 120f)

        // tab labels
        val tabFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        tabs.forEachIndexed { i, t ->
            tabFont.color = if (i == selectedTab.ordinal) Theme.Palette.PRIMARY_CONTAINER else Theme.Palette.ON_SURFACE_VARIANT
            val label = I18n["skin.tab.${t.name.lowercase()}"]
            layout.setText(tabFont, label)
            val r = tabRects[i]
            tabFont.draw(batch, label, r.x + (r.width - layout.width) / 2f, r.y + r.height / 2f + layout.height / 2f)
        }

        // row labels + lock/select hint
        val nameFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        val hintFont = game.fonts[Theme.FontSize.BODY_MD]
        forEachItem(rows) { i, item, isUnlocked, isSelected ->
            val r = rows[i]
            nameFont.color = when {
                isSelected -> Theme.Palette.NEON_GREEN
                isUnlocked -> Theme.Palette.ON_SURFACE
                else -> Theme.Palette.ON_SURFACE_VARIANT
            }
            val name = I18n["skin.${item.id}.name"]
            nameFont.draw(batch, name, r.x + SWATCH_W + 30f, r.y + r.height - 18f)

            hintFont.color = Theme.Palette.ON_SURFACE_VARIANT
            val hint = when {
                isSelected -> "● ${I18n["skin.selected"]}"
                isUnlocked -> ""
                else -> {
                    val ach = item.unlockAchievement
                    if (ach != null) "${I18n["skin.locked.via"]} ${I18n["achievement.${ach.id}.title"]}"
                    else ""
                }
            }
            if (hint.isNotEmpty()) hintFont.draw(batch, hint, r.x + SWATCH_W + 30f, r.y + 18f)
        }

        val backFont = game.fonts[Theme.FontSize.BODY_MD, true]
        backFont.color = Theme.Palette.ON_SURFACE_VARIANT
        val backTxt = "< ${I18n["nav.back"]}"
        layout.setText(backFont, backTxt)
        backFont.draw(batch, backTxt, backRect.x + (backRect.width - layout.width) / 2f, backRect.y + backRect.height / 2f + layout.height / 2f)
        batch.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        com.arkamadoid.render.BezelFrame.draw(shapes, viewport, VIRTUAL_W, VIRTUAL_H)

        handleInput(rows)
    }

    private fun currentRowRects(): List<Rectangle> = when (selectedTab) {
        Tab.PADDLE -> rowRects(PaddleSkin.values().size)
        Tab.BALL -> rowRects(BallSkin.values().size)
        Tab.PALETTE -> rowRects(PaletteSkin.values().size)
    }

    private data class SkinItem(val id: String, val unlockAchievement: com.arkamadoid.achievements.Achievement?)

    /** Itera gli skin del tab corrente passando (index, item, isUnlocked, isSelected). */
    private inline fun forEachItem(
        rows: List<Rectangle>,
        block: (Int, SkinItem, Boolean, Boolean) -> Unit,
    ) {
        val items = when (selectedTab) {
            Tab.PADDLE -> PaddleSkin.values().map { SkinItem(it.id, it.unlockAchievement) }
            Tab.BALL -> BallSkin.values().map { SkinItem(it.id, it.unlockAchievement) }
            Tab.PALETTE -> PaletteSkin.values().map { SkinItem(it.id, it.unlockAchievement) }
        }
        val selectedId = when (selectedTab) {
            Tab.PADDLE -> game.prefs.data.selectedPaddleSkin
            Tab.BALL -> game.prefs.data.selectedBallSkin
            Tab.PALETTE -> game.prefs.data.selectedPaletteSkin
        }
        items.forEachIndexed { i, item ->
            if (i >= rows.size) return@forEachIndexed
            block(i, item, game.prefs.isSkinUnlocked(item.id), item.id == selectedId)
        }
    }

    private fun drawSwatches(rows: List<Rectangle>) {
        when (selectedTab) {
            Tab.PADDLE -> PaddleSkin.values().forEachIndexed { i, s ->
                if (i >= rows.size) return@forEachIndexed
                drawPaddleSwatch(rows[i], s.color, !game.prefs.isSkinUnlocked(s.id))
            }
            Tab.BALL -> BallSkin.values().forEachIndexed { i, s ->
                if (i >= rows.size) return@forEachIndexed
                drawBallSwatch(rows[i], s.core, s.halo, !game.prefs.isSkinUnlocked(s.id))
            }
            Tab.PALETTE -> PaletteSkin.values().forEachIndexed { i, s ->
                if (i >= rows.size) return@forEachIndexed
                drawPaletteSwatch(rows[i], s.colors, !game.prefs.isSkinUnlocked(s.id))
            }
        }
    }

    private fun drawPaddleSwatch(rect: Rectangle, color: Color, locked: Boolean) {
        val sx = rect.x + 20f
        val sy = rect.y + (rect.height - 18f) / 2f
        val sw = SWATCH_W - 30f
        val sh = 18f
        val col = if (locked) dim(color) else color
        PlayfieldRenderer.capsule(shapes, sx, sy, sw, sh, col)
    }

    private fun drawBallSwatch(rect: Rectangle, core: Color, halo: Color, locked: Boolean) {
        val cx = rect.x + SWATCH_W / 2f
        val cy = rect.y + rect.height / 2f
        if (locked) {
            PlayfieldRenderer.glowBall(shapes, cx, cy, 12f, dim(core), dim(halo))
        } else {
            PlayfieldRenderer.glowBall(shapes, cx, cy, 12f, core, halo)
        }
    }

    private fun drawPaletteSwatch(rect: Rectangle, colors: Array<Color>, locked: Boolean) {
        val sx = rect.x + 20f
        val sy = rect.y + (rect.height - 24f) / 2f
        val sw = (SWATCH_W - 30f) / colors.size
        for ((i, c) in colors.withIndex()) {
            shapes.color = if (locked) dim(c) else c
            shapes.rect(sx + i * sw, sy, sw - 2f, 24f)
        }
    }

    /** Color allocato fresh per evitare aliasing su chiamate multiple nello stesso draw. */
    private fun dim(c: Color): Color = Color(c).also { it.a = 0.25f }

    private fun handleInput(rows: List<Rectangle>) {
        if (!Gdx.input.justTouched() || elapsed < 0.15f) return
        tmp.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        viewport.unproject(tmp)
        if (backRect.contains(tmp.x, tmp.y)) {
            game.setScreen(MainMenuScreen(game))
            return
        }
        for (i in tabRects.indices) {
            if (tabRects[i].contains(tmp.x, tmp.y)) {
                selectedTab = tabs[i]
                return
            }
        }
        for (i in rows.indices) {
            if (rows[i].contains(tmp.x, tmp.y)) {
                selectSkinAt(i)
                return
            }
        }
    }

    private fun selectSkinAt(i: Int) {
        when (selectedTab) {
            Tab.PADDLE -> PaddleSkin.values().getOrNull(i)?.let { game.prefs.selectPaddleSkin(it) }
            Tab.BALL -> BallSkin.values().getOrNull(i)?.let { game.prefs.selectBallSkin(it) }
            Tab.PALETTE -> PaletteSkin.values().getOrNull(i)?.let { game.prefs.selectPaletteSkin(it) }
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
        const val ROW_H = 90f
        const val GAP = 14f
        const val SWATCH_W = 180f
    }
}
