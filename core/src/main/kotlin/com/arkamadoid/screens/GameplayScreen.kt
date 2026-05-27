package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.audio.AudioManager
import com.arkamadoid.audio.MusicTrack
import com.arkamadoid.config.GameConfig
import com.arkamadoid.entities.Ball
import com.arkamadoid.entities.Brick
import com.arkamadoid.entities.PowerUp
import com.arkamadoid.gameplay.CollisionResolver
import com.arkamadoid.gameplay.CollisionResolver.WallHit
import com.arkamadoid.gameplay.GameState
import com.arkamadoid.gameplay.Level
import com.arkamadoid.gameplay.LevelLoader
import com.arkamadoid.input.TouchController
import com.arkamadoid.powerups.PowerUpType
import com.arkamadoid.render.ParticleSystem
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
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.random.Random

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
    private val particles = ParticleSystem()
    private val droppingPowerUps = mutableListOf<PowerUp>()

    private val unprojectTmp = Vector3()
    private val tmpUi = Vector3()
    private val damagedTint = Color()
    private val pauseRect = Rectangle(UI_W - 170f, 40f, 130f, 100f)
    private val skipSectorRect = Rectangle(UI_W / 2f - 140f, UI_H - 100f, 280f, 90f)

    private var disposed = false
    private var disposeOnHide = false
    private var lastSkipTapAt = 0L

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

    private val droppableTypes = arrayOf(
        PowerUpType.EXPAND,
        PowerUpType.SLOW,
        PowerUpType.MULTI,
        PowerUpType.LIFE,
        PowerUpType.LASER,
        PowerUpType.CATCH,
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
        positionPaddleAndBall()
    }

    private fun positionPaddleAndBall() {
        val p = state.paddle
        p.width = GameConfig.PADDLE_BASE_WIDTH.toFloat()
        p.hasLaser = false
        p.hasCatch = false
        p.x = (playFieldWidth - p.width) / 2f
        p.y = PADDLE_Y

        state.balls.clear()
        droppingPowerUps.clear()
        particles.clear()

        val b = Ball()
        b.speed = state.currentLevel?.ballSpeed ?: GameConfig.BALL_INITIAL_SPEED
        b.x = p.x + p.width / 2f
        b.y = p.y + p.height + b.radius + 1f
        b.velocity.set(0f, 0f)
        b.stuckToPaddle = true
        state.balls += b
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
        particles.update(delta)
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
            if (skipSectorRect.contains(tmpUi.x, tmpUi.y)) {
                val now = TimeUtils.nanoTime()
                if (now - lastSkipTapAt < 400_000_000L) {
                    lastSkipTapAt = 0L
                    onLevelComplete()
                } else {
                    lastSkipTapAt = now
                }
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

        if (Gdx.input.justTouched()) {
            for (ball in state.balls) {
                if (ball.stuckToPaddle) {
                    ball.stuckToPaddle = false
                    ball.setDirectionDeg(-30f)
                    break
                }
            }
        }
    }

    private fun step(dt: Float) {
        val level = state.currentLevel ?: return

        val iter = state.balls.iterator()
        while (iter.hasNext()) {
            val ball = iter.next()
            if (ball.stuckToPaddle) {
                ball.x = state.paddle.x + state.paddle.width / 2f
                continue
            }
            ball.x += ball.velocity.x * dt
            ball.y += ball.velocity.y * dt

            val wallHit = CollisionResolver.ballVsWalls(ball, playFieldWidth, playFieldHeight)
            if (wallHit == WallHit.BOTTOM) {
                iter.remove()
                continue
            }
            if (wallHit != WallHit.NONE) game.audio.playSfx(AudioManager.Sfx.BOUNCE, pitch = 1.2f)

            if (CollisionResolver.ballVsPaddle(ball, state.paddle)) {
                game.audio.playSfx(AudioManager.Sfx.BOUNCE)
            }

            for (brick in level.bricks) {
                if (!brick.alive) continue
                if (CollisionResolver.ballVsBrick(ball, brick)) {
                    if (!brick.alive) onBrickDestroyed(brick, level)
                    game.audio.playSfx(AudioManager.Sfx.BRICK)
                    break
                }
            }
        }

        if (state.balls.isEmpty()) {
            onBallLost()
            return
        }

        updatePowerUps(dt)

        if (level.isComplete) onLevelComplete()
    }

    private fun onBrickDestroyed(brick: Brick, level: Level) {
        state.score += brick.type.score
        particles.burstAt(
            brick.x + brick.width / 2f,
            brick.y + brick.height / 2f,
            brickColorOf(brick),
            count = 8,
        )
        if (Random.nextFloat() < GameConfig.POWERUP_DROP_CHANCE) {
            droppingPowerUps += PowerUp(
                x = brick.x + brick.width / 2f - 8f,
                y = brick.y,
                type = droppableTypes.random(),
            )
        }
        if (brick.type == Brick.Type.EXPLOSIVE) explodeAround(brick, level)
    }

    private fun explodeAround(center: Brick, level: Level) {
        val r = Rectangle(
            center.x - center.width,
            center.y - center.height,
            center.width * 3f,
            center.height * 3f,
        )
        val chain = mutableListOf<Brick>()
        for (b in level.bricks) {
            if (b === center || !b.alive) continue
            if (b.type == Brick.Type.INDESTRUCTIBLE) continue
            if (!b.bounds.overlaps(r)) continue
            b.hp = 0
            state.score += b.type.score
            particles.burstAt(
                b.x + b.width / 2f,
                b.y + b.height / 2f,
                brickColorOf(b),
                count = 6,
            )
            if (b.type == Brick.Type.EXPLOSIVE) chain += b
        }
        chain.forEach { explodeAround(it, level) }
    }

    private fun updatePowerUps(dt: Float) {
        val iter = droppingPowerUps.iterator()
        while (iter.hasNext()) {
            val pu = iter.next()
            pu.update(dt)
            if (pu.y + 8f < 0f) { iter.remove(); continue }
            if (pu.bounds.overlaps(state.paddle.bounds)) {
                applyPowerUp(pu.type)
                iter.remove()
            }
        }
    }

    private fun applyPowerUp(type: PowerUpType) {
        game.audio.playSfx(AudioManager.Sfx.POWERUP)
        when (type) {
            PowerUpType.EXPAND -> state.paddle.width = GameConfig.PADDLE_EXPAND_WIDTH.toFloat()
            PowerUpType.SLOW -> {
                for (b in state.balls) {
                    b.speed = (b.speed * 0.75f).coerceAtLeast(120f)
                    if (!b.stuckToPaddle && b.velocity.len2() > 0f) {
                        b.velocity.nor().scl(b.speed)
                    }
                }
            }
            PowerUpType.MULTI -> spawnMultiball()
            PowerUpType.LIFE -> state.lives += 1
            PowerUpType.LASER -> state.paddle.hasLaser = true
            PowerUpType.CATCH -> state.paddle.hasCatch = true
            PowerUpType.WARP -> Unit
        }
    }

    private fun spawnMultiball() {
        val active = state.balls.filter { !it.stuckToPaddle && it.velocity.len2() > 0f }
        if (active.isEmpty()) return
        val template = active.first()
        repeat(2) { i ->
            val b = Ball(template.x, template.y, template.radius)
            b.speed = template.speed
            b.stuckToPaddle = false
            val offset = if (i == 0) -25f else 25f
            val currentAngleRad = kotlin.math.atan2(template.velocity.x, template.velocity.y)
            val newAngleDeg = Math.toDegrees(currentAngleRad.toDouble()).toFloat() + offset
            b.setDirectionDeg(newAngleDeg)
            state.balls += b
        }
    }

    private fun onBallLost() {
        game.audio.playSfx(AudioManager.Sfx.LIFE_LOST)
        if (mode == GameMode.PRACTICE) {
            positionPaddleAndBall()
            return
        }
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
            if (mode == GameMode.ENDLESS) {
                state.levelIndex = 1
                loadLevel(1)
                return
            }
            disposeOnHide = true
            game.setScreen(GameOverScreen(game, state.score))
            return
        }
        state.levelIndex = next
        loadLevel(next)
    }

    private fun brickColorOf(brick: Brick): Color =
        brickPalette[brick.colorIndex % brickPalette.size]

    private fun powerUpColor(type: PowerUpType): Color = when (type) {
        PowerUpType.EXPAND -> Theme.Palette.SECONDARY_FIXED
        PowerUpType.SLOW -> Theme.Palette.SECONDARY_FIXED_DIM
        PowerUpType.LASER -> Theme.Palette.ERROR
        PowerUpType.MULTI -> Theme.Palette.PRIMARY_CONTAINER
        PowerUpType.CATCH -> Theme.Palette.TERTIARY
        PowerUpType.LIFE -> Color.WHITE
        PowerUpType.WARP -> Theme.Palette.PRIMARY_FIXED
    }

    private fun draw() {
        Gdx.gl.glClearColor(0.054f, 0.054f, 0.078f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        gameViewport.apply()
        shapes.projectionMatrix = gameViewport.camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)

        state.currentLevel?.bricks?.forEach { brick ->
            if (!brick.alive) return@forEach
            val base = brickColorOf(brick)
            shapes.color = if (brick.hp < brick.type.hp) {
                damagedTint.set(base).lerp(Color.WHITE, 0.35f)
            } else base
            shapes.rect(brick.x, brick.y, brick.width, brick.height)
        }

        for (pu in droppingPowerUps) {
            shapes.color = powerUpColor(pu.type)
            shapes.rect(pu.x, pu.y, 16f, 8f)
        }

        particles.render(shapes)

        val p = state.paddle
        shapes.color = Theme.Palette.SECONDARY_CONTAINER
        shapes.rect(p.x, p.y, p.width, p.height)

        shapes.color = Theme.Palette.PRIMARY_CONTAINER
        for (ball in state.balls) shapes.circle(ball.x, ball.y, ball.radius, 12)

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
        val integrity = if (mode == GameMode.PRACTICE) "INTEGRITY INF" else "INTEGRITY %d".format(state.lives.coerceAtLeast(0))
        layout.setText(font, integrity)
        font.draw(batch, integrity, UI_W - 40f - layout.width, UI_H - 40f)

        if (state.balls.any { it.stuckToPaddle }) {
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
