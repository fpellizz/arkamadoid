package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.audio.MusicTrack
import com.arkamadoid.theme.Theme
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport

class BootScreen(game: ArkamadoidGame) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val layout = GlyphLayout()

    private val lines = listOf(
        "> SYSTEM INITIALIZING...",
        "> CHECKING BIOS INTEGRITY...",
        "> KERNEL LOADED: 0x88FF42",
        "> MOUNTING ASSETS...",
        "> LOADING SPRITE SHEETS...",
        "> COMPILING SHADERS...",
        "> SYNCING CRT REFRESH RATE...",
        "> INITIALIZING PHYSICS ENGINE...",
        "> ALL SYSTEMS GREEN.",
        "> READY TO START.",
    )

    private var elapsed = 0f
    private val totalDuration = 4f
    private var musicStarted = false

    override fun show() {
        Gdx.input.inputProcessor = null
    }

    override fun render(delta: Float) {
        elapsed += delta
        val progress = (elapsed / totalDuration).coerceIn(0f, 1f)

        if (!musicStarted && elapsed > 0.5f) {
            game.audio.playMusic(MusicTrack.MENU, fadeSeconds = 1.5f)
            musicStarted = true
        }

        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f) // surface-container-lowest
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        batch.projectionMatrix = viewport.camera.combined
        shapes.projectionMatrix = viewport.camera.combined

        batch.begin()

        // Header ARKAMADOID
        val titleFont = game.fonts[Theme.FontSize.HEADLINE, true]
        titleFont.color = Theme.Palette.PRIMARY
        layout.setText(titleFont, "ARKAMADOID")
        titleFont.draw(batch, "ARKAMADOID", (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 60f)

        // sottotitolo
        val subFont = game.fonts[Theme.FontSize.LABEL_SM, true]
        subFont.color = Theme.Palette.SECONDARY_FIXED_DIM
        layout.setText(subFont, "RETRO-ENGINE v2.4.88 // CRT-ENABLED")
        subFont.draw(batch, layout, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H - 105f)

        // terminal output
        val termFont = game.fonts[Theme.FontSize.BODY_MD]
        termFont.color = Theme.Palette.SECONDARY_FIXED_DIM
        val visibleLines = (progress * lines.size).toInt()
        val startY = VIRTUAL_H - 180f
        val lineHeight = 28f
        for (i in 0 until visibleLines) {
            val cursor = if (i == visibleLines - 1 && (elapsed * 2).toInt() % 2 == 0) "_" else ""
            termFont.draw(batch, lines[i] + cursor, 60f, startY - i * lineHeight)
        }

        // % label
        val pctFont = game.fonts[Theme.FontSize.LABEL_SM, true]
        pctFont.color = Theme.Palette.PRIMARY_FIXED_DIM
        pctFont.draw(batch, "${(progress * 100).toInt()}%", VIRTUAL_W - 100f, 130f)

        val statusFont = game.fonts[Theme.FontSize.LABEL_SM, true]
        statusFont.color = Theme.Palette.PRIMARY_FIXED_DIM
        statusFont.draw(
            batch,
            when {
                progress < 0.3f -> "INITIALIZING CORE..."
                progress < 0.6f -> "LOADING ASSETS..."
                progress < 0.95f -> "COMPILING SHADERS..."
                else -> "SYSTEM READY"
            },
            60f, 130f,
        )

        batch.end()

        // progress bar
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color = Theme.Palette.SURFACE_CONTAINER_LOWEST
        shapes.rect(60f, 90f, VIRTUAL_W - 120f, 24f)
        shapes.color = Theme.Palette.PRIMARY_CONTAINER
        shapes.rect(60f, 90f, (VIRTUAL_W - 120f) * progress, 24f)
        shapes.end()

        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = Theme.Palette.OUTLINE_VARIANT
        shapes.rect(60f, 90f, VIRTUAL_W - 120f, 24f)
        shapes.end()

        com.arkamadoid.render.BezelFrame.draw(shapes, viewport, VIRTUAL_W, VIRTUAL_H)

        if (elapsed >= totalDuration + 0.5f) {
            game.setScreen(AttractScreen(game))
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
