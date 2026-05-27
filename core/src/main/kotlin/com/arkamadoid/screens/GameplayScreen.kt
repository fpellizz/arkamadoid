package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.audio.MusicTrack
import com.arkamadoid.config.GameConfig
import com.arkamadoid.gameplay.CollisionResolver
import com.arkamadoid.gameplay.CollisionResolver.WallHit
import com.arkamadoid.gameplay.GameState
import com.arkamadoid.gameplay.LevelLoader
import com.arkamadoid.input.TouchController
import com.arkamadoid.render.PixelViewport
import com.arkamadoid.theme.Theme
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport

class GameplayScreen(
    game: ArkamadoidGame,
    private val mode: GameMode,
) : BaseScreen(game) {

    private val state = GameState()
    private val gameViewport = PixelViewport()
    private val uiViewport = FitViewport(UI_W, UI_H)
    private val shapes = ShapeRenderer()
    private val batch = SpriteBatch()
    private val layout = GlyphLayout()
    private val touch = TouchController()

    private val unprojectTmp = Vector3()
    private val tmpUi = Vector3()
    private val damagedTint = Color()
    private val pauseRect = Rectangle(UI_W - 170f, 40f, 130f, 100f)

    private var disposed = false
    private var disposeOnHide = false

    private val playFieldHeight = GameConfig.VIRTUAL_HEIGHT.toFloat()
    private val playFieldWidth = GameConfig.VIRTUAL_WIDTH.toFloat()

    private var accumulator = 0f
    private var lastTouchX = -1f

    private val brickPalette = arrayOf(
        Theme.Palette.SECONDARY_FIXED_DIM,
        Theme.Palette.PRIMARY_CONTAINER,
        Theme.Palette.TERTIARY,
        Theme.Palette.SECONDARY_FIXED,
        Theme.Palette.PRIMARY_FIXED,
        Theme.Palette.ERROR,
    )

    enum class GameMode { ARCADE, ENDLESS, DAILY, PRACTICE }

    override fun show() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        if (state.currentLevel == null) loadLevel(state.levelIndex)
        game.audio.playMusic(MusicTrack.GAMEPLAY_EARLY)
    }

    private fun loadLevel(index: Int) {
        val level = LevelLoader.load(index)
        state.currentLevel = level
        state.balls[0].speed = level.ballSpeed
        positionPaddleAndBall()
    }

    private fun positionPaddleAndBall() {
        val p = state.paddle
        p.x = (playFieldWidth - p.width) / 2f
        p.y = PADDLE_Y
        val b = state.balls[0]
        b.x = p.x + p.width / 2f
        b.y = p.y + p.height + b.radius + 1f
        b.velocity.set(0f, 0f)
        b.stuckToPaddle = true
    }

    override fun render(delta: Float) {
        if (disposed) return
        update(delta.coerceAtMost(1f / 30f))
        if (disposed) return
        draw()
    }

    private fun update(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            game.setScreen(PauseScreen(game, this))
            return
        }
        handleInput()
        if (disposed || state.paused) return
        accumulator += delta
        while (accumulator >= GameConfig.FIXED_STEP) {
            step(GameConfig.FIXED_STEP)
            if (disposed) return
            accumulator -= GameConfig.FIXED_STEP
        }
    }

    private fun handleInput() {
        val touched = Gdx.input.isTouched
        if (!touched) { lastTouchX = -1f; return }

        if (Gdx.input.justTouched()) {
            tmpUi.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            uiViewport.unproject(tmpUi)
            if (pauseRect.contains(tmpUi.x, tmpUi.y)) {
                game.setScreen(PauseScreen(game, this))
                return
            }
        }

        unprojectTmp.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        gameViewport.unproject(unprojectTmp)
        val currX = unprojectTmp.x

        if (lastTouchX < 0f) lastTouchX = currX
        val dx = currX - lastTouchX
        touch.update(state.paddle, dx, currX, playFieldWidth)
        lastTouchX = currX

        val ball = state.balls[0]
        if (ball.stuckToPaddle && Gdx.input.justTouched()) {
            ball.stuckToPaddle = false
            ball.setDirectionDeg(-30f)
        }
    }

    private fun step(dt: Float) {
        val ball = state.balls[0]
        if (ball.stuckToPaddle) {
            ball.x = state.paddle.x + state.paddle.width / 2f
            return
        }

        ball.x += ball.velocity.x * dt
        ball.y += ball.velocity.y * dt

        val wallHit = CollisionResolver.ballVsWalls(ball, playFieldWidth, playFieldHeight)
        if (wallHit == WallHit.BOTTOM) {
            onBallLost()
            return
        }

        CollisionResolver.ballVsPaddle(ball, state.paddle)

        val level = state.currentLevel ?: return
        for (brick in level.bricks) {
            if (!brick.alive) continue
            if (CollisionResolver.ballVsBrick(ball, brick)) {
                if (!brick.alive) state.score += brick.type.score
                break
            }
        }

        if (level.isComplete) onLevelComplete()
    }

    private fun onBallLost() {
        state.lives -= 1
        if (state.lives <= 0) {
            disposeOnHide = true
            game.setScreen(GameOverScreen(game, state.score))
            return
        }
        positionPaddleAndBall()
    }

    private fun onLevelComplete() {
        val next = state.levelIndex + 1
        val handle = Gdx.files.internal("levels/%02d.json".format(next))
        if (!handle.exists()) {
            disposeOnHide = true
            game.setScreen(GameOverScreen(game, state.score))
            return
        }
        state.levelIndex = next
        loadLevel(next)
    }

    private fun draw() {
        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        gameViewport.apply()
        shapes.projectionMatrix = gameViewport.camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)

        state.currentLevel?.bricks?.forEach { brick ->
            if (!brick.alive) return@forEach
            val base = brickPalette[brick.colorIndex % brickPalette.size]
            shapes.color = if (brick.hp < brick.type.hp) {
                damagedTint.set(base).lerp(Color.WHITE, 0.35f)
            } else base
            shapes.rect(brick.x, brick.y, brick.width, brick.height)
        }

        val p = state.paddle
        shapes.color = Theme.Palette.SECONDARY_CONTAINER
        shapes.rect(p.x, p.y, p.width, p.height)

        val ball = state.balls[0]
        shapes.color = Theme.Palette.PRIMARY_CONTAINER
        shapes.circle(ball.x, ball.y, ball.radius, 12)

        shapes.end()

        uiViewport.apply()
        shapes.projectionMatrix = uiViewport.camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color = Theme.Palette.PRIMARY_CONTAINER
        val barW = 16f
        val barH = 56f
        val cx = pauseRect.x + pauseRect.width / 2f
        val cy = pauseRect.y + pauseRect.height / 2f
        shapes.rect(cx - barW - 8f, cy - barH / 2f, barW, barH)
        shapes.rect(cx + 8f, cy - barH / 2f, barW, barH)
        shapes.end()

        batch.projectionMatrix = uiViewport.camera.combined
        batch.begin()

        val font = game.fonts[Theme.FontSize.BODY_MD, true]

        font.color = Theme.Palette.SECONDARY_CONTAINER
        font.draw(batch, "SCORE %07d".format(state.score), 40f, UI_H - 40f)

        font.color = Theme.Palette.TERTIARY
        val sector = "SECTOR %02d".format(state.levelIndex)
        layout.setText(font, sector)
        font.draw(batch, sector, (UI_W - layout.width) / 2f, UI_H - 40f)

        font.color = Theme.Palette.PRIMARY_CONTAINER
        val integrity = "INTEGRITY %d".format(state.lives.coerceAtLeast(0))
        layout.setText(font, integrity)
        font.draw(batch, integrity, UI_W - 40f - layout.width, UI_H - 40f)

        if (ball.stuckToPaddle) {
            font.color = Theme.Palette.PRIMARY_FIXED
            val hint = "TAP TO LAUNCH"
            layout.setText(font, hint)
            font.draw(batch, hint, (UI_W - layout.width) / 2f, 120f)
        }

        batch.end()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiViewport.update(width, height, true)
    }

    override fun hide() {
        if (disposeOnHide) dispose()
    }

    override fun dispose() {
        if (disposed) return
        disposed = true
        shapes.dispose()
        batch.dispose()
    }

    companion object {
        const val UI_W = 720f
        const val UI_H = 1280f
        const val PADDLE_Y = 18f
    }
}
