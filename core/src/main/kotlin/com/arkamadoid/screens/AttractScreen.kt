package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.config.GameConfig
import com.arkamadoid.entities.Ball
import com.arkamadoid.entities.Brick
import com.arkamadoid.gameplay.CollisionResolver
import com.arkamadoid.gameplay.GameState
import com.arkamadoid.gameplay.LevelLoader
import com.arkamadoid.localization.I18n
import com.arkamadoid.render.HudCardSide
import com.arkamadoid.render.PixelViewport
import com.arkamadoid.render.PlayfieldRenderer
import com.arkamadoid.theme.Theme
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.FitViewport

class AttractScreen(game: ArkamadoidGame) : BaseScreen(game) {

    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val viewport = FitViewport(VIRTUAL_W, VIRTUAL_H)
    private val gameViewport = PixelViewport()
    private val layout = GlyphLayout()
    private var elapsed = 0f

    private val demoState = GameState()
    private var demoAccumulator = 0f
    private val playFieldW = GameConfig.VIRTUAL_WIDTH.toFloat()
    private val playFieldH = GameConfig.VIRTUAL_HEIGHT.toFloat()
    private val damagedTint = Color()

    // HUD card layout: identico a GameplayScreen
    private val scoreCardRect = Rectangle(40f, VIRTUAL_H - 200f, 220f, 130f)
    private val sectorCardRect = Rectangle((VIRTUAL_W - 200f) / 2f, VIRTUAL_H - 200f, 200f, 130f)
    private val integrityCardRect = Rectangle(VIRTUAL_W - 260f, VIRTUAL_H - 200f, 220f, 130f)

    private val brickPalette: Array<Color>
        get() = game.prefs.currentPaletteSkin().colors

    init {
        loadDemoLevel()
    }

    private fun loadDemoLevel() {
        demoState.currentLevel = LevelLoader.load(1)
        val p = demoState.paddle
        p.width = GameConfig.PADDLE_BASE_WIDTH.toFloat()
        p.x = (playFieldW - p.width) / 2f
        p.y = 18f
        demoState.balls.clear()
        val b = Ball()
        b.speed = 180f
        b.x = p.x + p.width / 2f
        b.y = p.y + p.height + b.radius + 1f
        b.stuckToPaddle = false
        b.setDirectionDeg(-30f)
        demoState.balls += b
    }

    private fun stepDemo(dt: Float) {
        val level = demoState.currentLevel ?: return
        val p = demoState.paddle

        val target = demoState.balls.firstOrNull()
        if (target != null) {
            val desiredX = target.x - p.width / 2f
            p.x += (desiredX - p.x) * 0.18f
            p.x = p.x.coerceIn(0f, playFieldW - p.width)
        }

        val iter = demoState.balls.iterator()
        while (iter.hasNext()) {
            val ball = iter.next()
            ball.x += ball.velocity.x * dt
            ball.y += ball.velocity.y * dt
            if (!game.prefs.data.reduceMotion) ball.pushTrail()

            val wh = CollisionResolver.ballVsWalls(ball, playFieldW, playFieldH)
            if (wh == CollisionResolver.WallHit.BOTTOM) {
                iter.remove()
                continue
            }
            CollisionResolver.ballVsPaddle(ball, p)
            for (brick in level.bricks) {
                if (!brick.alive) continue
                if (CollisionResolver.ballVsBrick(ball, brick)) break
            }
        }

        if (demoState.balls.isEmpty() || level.isComplete) {
            loadDemoLevel()
        }
    }

    override fun render(delta: Float) {
        elapsed += delta

        demoAccumulator += delta.coerceAtMost(1f / 30f)
        while (demoAccumulator >= GameConfig.FIXED_STEP) {
            stepDemo(GameConfig.FIXED_STEP)
            demoAccumulator -= GameConfig.FIXED_STEP
        }

        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        // === DEMO GAMEPLAY (pixel viewport, identico al gameplay reale) ===
        gameViewport.apply()
        shapes.projectionMatrix = gameViewport.camera.combined

        PlayfieldRenderer.gridBackground(shapes, playFieldW, playFieldH, 16f)

        shapes.begin(ShapeRenderer.ShapeType.Filled)

        demoState.currentLevel?.bricks?.forEach { brick ->
            if (!brick.alive) return@forEach
            if (brick.type == Brick.Type.INDESTRUCTIBLE) {
                PlayfieldRenderer.steelBrick(shapes, brick.x + 0.5f, brick.y + 0.5f, brick.width - 1f, brick.height - 1f, cornerRadius = 1f)
            } else {
                val base = brickPalette[brick.colorIndex % brickPalette.size]
                val maxHp = brick.type.hp.coerceAtLeast(1)
                val damageRatio = if (maxHp > 1) (1f - brick.hp.toFloat() / maxHp) else 0f
                val col = if (damageRatio > 0f)
                    damagedTint.set(base).lerp(Color.WHITE, damageRatio * 0.85f)
                else base
                PlayfieldRenderer.glowRect(
                    shapes,
                    brick.x + 1f, brick.y + 0.5f,
                    brick.width - 2f, brick.height - 1f,
                    col,
                    cornerRadius = 1.5f,
                )
            }
        }

        val p = demoState.paddle
        PlayfieldRenderer.capsule(shapes, p.x, p.y, p.width, p.height, game.prefs.currentPaddleSkin().color)

        val ballSkin = game.prefs.currentBallSkin()
        for (ball in demoState.balls) {
            PlayfieldRenderer.ballTrail(shapes, ball.trailX, ball.trailY, ball.trailHead, ball.trailCount, ball.radius)
            PlayfieldRenderer.glowBall(shapes, ball.x, ball.y, ball.radius, ballSkin.core, ballSkin.halo)
        }

        shapes.end()

        // === HUD card (uguale al gameplay reale) ===
        viewport.apply()
        shapes.projectionMatrix = viewport.camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)

        PlayfieldRenderer.hudCard(shapes, scoreCardRect, Theme.Palette.TERTIARY, HudCardSide.LEFT)
        PlayfieldRenderer.hudCard(shapes, sectorCardRect, Theme.Palette.PRIMARY_CONTAINER, HudCardSide.BOTTOM)
        PlayfieldRenderer.hudCard(shapes, integrityCardRect, Theme.Palette.SECONDARY_FIXED_DIM, HudCardSide.RIGHT)

        shapes.end()

        // === Testi HUD + overlay attract ===
        batch.projectionMatrix = viewport.camera.combined
        batch.begin()

        val labelFont = game.fonts[Theme.FontSize.LABEL_SM, true]
        val valueFont = game.fonts[Theme.FontSize.HEADLINE, true]

        labelFont.color = Theme.Palette.TERTIARY
        labelFont.draw(batch, "DATA_STORE", scoreCardRect.x + 18f, scoreCardRect.y + scoreCardRect.height - 20f)
        valueFont.color = Theme.Palette.TERTIARY
        valueFont.draw(batch, "%07d".format(0), scoreCardRect.x + 18f, scoreCardRect.y + 50f)

        labelFont.color = Theme.Palette.PRIMARY_CONTAINER
        val sectorLabel = "SECTOR"
        layout.setText(labelFont, sectorLabel)
        labelFont.draw(batch, sectorLabel, sectorCardRect.x + (sectorCardRect.width - layout.width) / 2f, sectorCardRect.y + sectorCardRect.height - 20f)
        valueFont.color = Theme.Palette.PRIMARY_CONTAINER
        val sectorVal = "01"
        layout.setText(valueFont, sectorVal)
        valueFont.draw(batch, sectorVal, sectorCardRect.x + (sectorCardRect.width - layout.width) / 2f, sectorCardRect.y + 50f)

        labelFont.color = Theme.Palette.SECONDARY_FIXED_DIM
        val intLabel = "INTEGRITY"
        layout.setText(labelFont, intLabel)
        labelFont.draw(batch, intLabel, integrityCardRect.x + integrityCardRect.width - 18f - layout.width, integrityCardRect.y + integrityCardRect.height - 20f)
        valueFont.color = Theme.Palette.SECONDARY_FIXED_DIM
        val intVal = "03"
        layout.setText(valueFont, intVal)
        valueFont.draw(batch, intVal, integrityCardRect.x + integrityCardRect.width - 18f - layout.width, integrityCardRect.y + 50f)

        // footer hint identico al gameplay
        val hintFont = game.fonts[Theme.FontSize.LABEL_SM, true]
        hintFont.color = Theme.Palette.SECONDARY_FIXED_DIM
        val hint = "SENSORS ACTIVE   SLIDE TO STEER"
        layout.setText(hintFont, hint)
        hintFont.draw(batch, hint, (VIRTUAL_W - layout.width) / 2f, 80f)

        // INSERT COIN blink 1.5 Hz
        val blink = (elapsed * 1.5f).toInt() % 2 == 0
        if (blink) {
            val coinFont = game.fonts[Theme.FontSize.HEADLINE, true]
            coinFont.color = Theme.Palette.PRIMARY_CONTAINER
            val coinTxt = I18n["menu.insertCoin"]
            layout.setText(coinFont, coinTxt)
            coinFont.draw(batch, coinTxt, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 80f)
        }

        // TAP TO START sotto
        val pressFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
        pressFont.color = Theme.Palette.SECONDARY_CONTAINER
        val pressTxt = I18n["attract.tapStart"]
        layout.setText(pressFont, pressTxt)
        pressFont.draw(batch, pressTxt, (VIRTUAL_W - layout.width) / 2f, VIRTUAL_H / 2f - 160f)

        batch.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        com.arkamadoid.render.BezelFrame.draw(shapes, viewport, VIRTUAL_W, VIRTUAL_H)

        if (Gdx.input.justTouched() && elapsed > 0.5f) {
            game.setScreen(MainMenuScreen(game))
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        gameViewport.update(width, height, true)
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
