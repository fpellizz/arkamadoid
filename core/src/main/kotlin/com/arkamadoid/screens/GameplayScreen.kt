package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.audio.AudioManager
import com.arkamadoid.audio.MusicTrack
import com.arkamadoid.config.GameConfig
import com.arkamadoid.entities.Ball
import com.arkamadoid.entities.Brick
import com.arkamadoid.entities.LaserBolt
import com.arkamadoid.entities.PowerUp
import com.arkamadoid.gameplay.CollisionResolver
import com.arkamadoid.gameplay.CollisionResolver.WallHit
import com.arkamadoid.gameplay.DailySeed
import com.arkamadoid.gameplay.GameState
import com.arkamadoid.gameplay.Level
import com.arkamadoid.gameplay.LevelLoader
import com.arkamadoid.input.TouchController
import com.arkamadoid.powerups.PowerUpType
import com.arkamadoid.render.HudCardSide
import com.arkamadoid.render.ParticleSystem
import com.arkamadoid.render.PixelViewport
import com.arkamadoid.render.PlayfieldRenderer
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
    private val laserBolts = mutableListOf<LaserBolt>()
    private var laserCooldown = 0f

    private val unprojectTmp = Vector3()
    private val tmpUi = Vector3()
    private val damagedTint = Color()
    private val tmpColor = Color()
    private val pauseRect = Rectangle(UI_W - 170f, 40f, 130f, 100f)
    private val scoreCardRect = Rectangle(40f, UI_H - 200f, 220f, 130f)
    private val sectorCardRect = Rectangle((UI_W - 200f) / 2f, UI_H - 200f, 200f, 130f)
    private val integrityCardRect = Rectangle(UI_W - 260f, UI_H - 200f, 220f, 130f)
    private val skipSectorRect = Rectangle(sectorCardRect.x, sectorCardRect.y, sectorCardRect.width, sectorCardRect.height)

    private var disposed = false
    private var disposeOnHide = false
    private var lastSkipTapAt = 0L

    private var popupText: String? = null
    private val popupColor = Color()
    private var popupRemaining = 0f

    private var shakeMagnitude = 0f
    private var shakeRemaining = 0f
    private var hitStopRemaining = 0f

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
        touch.mode = game.prefs.data.inputMode
        touch.sensitivity = game.prefs.data.sensitivity
        if (mode == GameMode.DAILY) {
            state.lives = 1
            state.levelIndex = DailySeed.levelIndex(GameConfig.MAX_LEVELS)
            state.currentLevel = null
        }
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
        laserBolts.clear()
        laserCooldown = 0f
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
        if (shakeRemaining > 0f) shakeRemaining -= delta
        if (hitStopRemaining > 0f) {
            hitStopRemaining -= delta
            return
        }
        accumulator += delta
        while (accumulator >= GameConfig.FIXED_STEP) {
            step(GameConfig.FIXED_STEP)
            if (disposed) return
            accumulator -= GameConfig.FIXED_STEP
        }
        particles.update(delta)
        expirePopupIfDone(delta)
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

        fireLaserIfReady()
    }

    private fun fireLaserIfReady() {
        if (!state.paddle.hasLaser) return
        if (laserCooldown > 0f) return
        if (state.balls.any { it.stuckToPaddle }) return
        val p = state.paddle
        val topY = p.y + p.height + 1f
        laserBolts += LaserBolt(p.x + 3f, topY)
        laserBolts += LaserBolt(p.x + p.width - 3f, topY)
        laserCooldown = LASER_COOLDOWN
        game.audio.playSfx(AudioManager.Sfx.BOUNCE, pitch = 2.0f)
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
            if (!game.prefs.data.reduceMotion) ball.pushTrail()

            val wallHit = CollisionResolver.ballVsWalls(ball, playFieldWidth, playFieldHeight)
            if (wallHit == WallHit.BOTTOM) {
                iter.remove()
                continue
            }
            if (wallHit != WallHit.NONE) game.audio.playSfx(AudioManager.Sfx.BOUNCE, pitch = 1.2f)

            if (CollisionResolver.ballVsPaddle(ball, state.paddle)) {
                game.audio.playSfx(AudioManager.Sfx.BOUNCE)
                if (state.paddle.hasCatch) {
                    ball.velocity.set(0f, 0f)
                    ball.stuckToPaddle = true
                    ball.y = state.paddle.y + state.paddle.height + ball.radius + 1f
                }
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
        updateLaserBolts(dt)

        if (level.isComplete) onLevelComplete()
    }

    private fun updateLaserBolts(dt: Float) {
        if (laserCooldown > 0f) laserCooldown -= dt
        val level = state.currentLevel ?: return
        val it = laserBolts.iterator()
        while (it.hasNext()) {
            val bolt = it.next()
            bolt.y += LaserBolt.SPEED * dt
            if (bolt.y > playFieldHeight) { it.remove(); continue }
            val r = bolt.bounds
            var hit = false
            for (brick in level.bricks) {
                if (!brick.alive) continue
                if (!brick.bounds.overlaps(r)) continue
                if (brick.type != Brick.Type.INDESTRUCTIBLE) {
                    brick.hp -= 1
                    if (!brick.alive) onBrickDestroyed(brick, level)
                    game.audio.playSfx(AudioManager.Sfx.BRICK, pitch = 1.4f)
                }
                hit = true
                break
            }
            if (hit) it.remove()
        }
    }

    private fun onBrickDestroyed(brick: Brick, level: Level) {
        state.score += brick.type.score
        particles.burstAt(
            brick.x + brick.width / 2f,
            brick.y + brick.height / 2f,
            brickColorOf(brick),
            count = if (game.prefs.data.reduceMotion) 3 else 8,
        )
        triggerImpact(brick.type == Brick.Type.EXPLOSIVE)
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
                count = if (game.prefs.data.reduceMotion) 2 else 6,
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
        triggerPickupPopup(type)
        haptic(HAPTIC_POWERUP_MS)
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
            PowerUpType.WARP -> onLevelComplete()
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
        haptic(HAPTIC_LIFE_LOST_MS)
        if (mode == GameMode.PRACTICE) {
            positionPaddleAndBall()
            return
        }
        state.lives -= 1
        if (state.lives <= 0) {
            if (mode == GameMode.DAILY) {
                recordDailyAndExit()
                return
            }
            disposeOnHide = true
            game.setScreen(GameOverScreen(game, state.score, state.levelIndex))
            return
        }
        positionPaddleAndBall()
    }

    private fun onLevelComplete() {
        if (mode == GameMode.DAILY) {
            recordDailyAndExit()
            return
        }
        val next = state.levelIndex + 1
        val handle = Gdx.files.internal("levels/%02d.json".format(next))
        if (!handle.exists()) {
            if (mode == GameMode.ENDLESS) {
                state.levelIndex = 1
                loadLevel(1)
                return
            }
            disposeOnHide = true
            game.setScreen(GameOverScreen(game, state.score, state.levelIndex))
            return
        }
        state.levelIndex = next
        loadLevel(next)
    }

    private fun recordDailyAndExit() {
        game.prefs.recordDailyScore(DailySeed.dateKey(), state.score)
        disposeOnHide = true
        game.setScreen(GameOverScreen(game, state.score, state.levelIndex, daily = true))
    }

    private fun applyShakeOffset() {
        val cam = gameViewport.camera
        val baseX = playFieldWidth / 2f
        val baseY = playFieldHeight / 2f
        if (shakeRemaining > 0f && shakeMagnitude > 0f) {
            val t = (shakeRemaining / SHAKE_DURATION_HEAVY).coerceIn(0f, 1f)
            val intensity = shakeMagnitude * t
            val offX = (Random.nextFloat() * 2f - 1f) * intensity
            val offY = (Random.nextFloat() * 2f - 1f) * intensity
            cam.position.set(baseX + offX, baseY + offY, 0f)
        } else {
            cam.position.set(baseX, baseY, 0f)
        }
        cam.update()
    }

    private fun triggerImpact(explosive: Boolean) {
        val reduceMotion = game.prefs.data.reduceMotion
        if (explosive) {
            if (!reduceMotion) {
                shakeMagnitude = SHAKE_INTENSITY_HEAVY
                shakeRemaining = SHAKE_DURATION_HEAVY
                hitStopRemaining = HIT_STOP_HEAVY
            }
            haptic(HAPTIC_HEAVY_MS)
        } else {
            if (!reduceMotion) {
                shakeMagnitude = SHAKE_INTENSITY_LIGHT
                shakeRemaining = SHAKE_DURATION_LIGHT
                hitStopRemaining = HIT_STOP_LIGHT
            }
            haptic(HAPTIC_LIGHT_MS)
        }
    }

    private fun haptic(ms: Int) {
        if (!game.prefs.data.haptics) return
        game.platform.vibrate(ms)
    }

    private fun brickColorOf(brick: Brick): Color =
        brickPalette[brick.colorIndex % brickPalette.size]

    private fun triggerPickupPopup(type: PowerUpType) {
        popupText = pickupLabel(type)
        popupColor.set(powerUpColor(type))
        popupRemaining = POPUP_LIFETIME
    }

    private fun expirePopupIfDone(delta: Float) {
        if (popupRemaining <= 0f) return
        popupRemaining -= delta
        if (popupRemaining > 0f) return
        val p = state.paddle
        particles.burstAt(
            p.x + p.width / 2f,
            p.y + p.height + 8f,
            popupColor,
            count = if (game.prefs.data.reduceMotion) 6 else 22,
        )
        popupText = null
        popupRemaining = 0f
    }

    private fun pickupLabel(type: PowerUpType): String = when (type) {
        PowerUpType.EXPAND -> "EXPAND!"
        PowerUpType.SLOW -> "SLOW BALL!"
        PowerUpType.LASER -> "LASER!"
        PowerUpType.MULTI -> "MULTIBALL!"
        PowerUpType.CATCH -> "CATCH!"
        PowerUpType.LIFE -> "EXTRA LIFE!"
        PowerUpType.WARP -> "WARP!"
    }

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

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        // === GAME VIEWPORT ===
        gameViewport.apply()
        applyShakeOffset()
        shapes.projectionMatrix = gameViewport.camera.combined

        PlayfieldRenderer.gridBackground(shapes, playFieldWidth, playFieldHeight, 16f)

        shapes.begin(ShapeRenderer.ShapeType.Filled)

        // bricks (con glow, 1px gap per separazione visiva)
        state.currentLevel?.bricks?.forEach { brick ->
            if (!brick.alive) return@forEach
            val base = brickColorOf(brick)
            val col = if (brick.hp < brick.type.hp) damagedTint.set(base).lerp(Color.WHITE, 0.35f) else base
            PlayfieldRenderer.glowRect(shapes, brick.x + 2f, brick.y + 1.5f, brick.width - 4f, brick.height - 3f, col, cornerRadius = 1f)
        }

        // power-up drops (con glow)
        for (pu in droppingPowerUps) {
            PlayfieldRenderer.glowRect(shapes, pu.x, pu.y, 16f, 8f, powerUpColor(pu.type))
        }

        // particles
        particles.render(shapes)

        // paddle (capsule cyan con inner highlight)
        val p = state.paddle
        PlayfieldRenderer.capsule(shapes, p.x, p.y, p.width, p.height, Theme.Palette.SECONDARY_CONTAINER)

        // cannoni laser sui lati del paddle se hasLaser è attivo
        if (p.hasLaser) {
            PlayfieldRenderer.glowRect(shapes, p.x + 2f, p.y + p.height, 2f, 3f, Theme.Palette.ERROR)
            PlayfieldRenderer.glowRect(shapes, p.x + p.width - 4f, p.y + p.height, 2f, 3f, Theme.Palette.ERROR)
        }

        // laser bolts in volo
        for (bolt in laserBolts) {
            PlayfieldRenderer.glowRect(shapes, bolt.x - LaserBolt.WIDTH / 2f, bolt.y, LaserBolt.WIDTH, LaserBolt.HEIGHT, Theme.Palette.ERROR)
        }

        // balls (bianche con alone magenta) + trail
        for (ball in state.balls) {
            PlayfieldRenderer.ballTrail(shapes, ball.trailX, ball.trailY, ball.trailHead, ball.trailCount, ball.radius)
            PlayfieldRenderer.glowBall(shapes, ball.x, ball.y, ball.radius)
        }

        shapes.end()

        // === UI VIEWPORT ===
        uiViewport.apply()
        shapes.projectionMatrix = uiViewport.camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)

        // HUD cards
        PlayfieldRenderer.hudCard(shapes,scoreCardRect, Theme.Palette.TERTIARY, HudCardSide.LEFT)
        PlayfieldRenderer.hudCard(shapes,sectorCardRect, Theme.Palette.PRIMARY_CONTAINER, HudCardSide.BOTTOM)
        PlayfieldRenderer.hudCard(shapes,integrityCardRect, Theme.Palette.SECONDARY_FIXED_DIM, HudCardSide.RIGHT)

        // vite (quadrati cyan con glow), o niente se PRACTICE (mostro INF dopo come testo)
        if (mode != GameMode.PRACTICE) {
            val livesShown = state.lives.coerceIn(0, 5)
            val sizeL = 22f
            val gap = 10f
            val totalW = livesShown * sizeL + (livesShown - 1).coerceAtLeast(0) * gap
            val startX = integrityCardRect.x + integrityCardRect.width - 20f - totalW
            val ly = integrityCardRect.y + 32f
            for (i in 0 until livesShown) {
                PlayfieldRenderer.glowRect(shapes, startX + i * (sizeL + gap), ly, sizeL, sizeL, Theme.Palette.SECONDARY_FIXED_DIM)
            }
        }

        // pause button bars
        shapes.color = Theme.Palette.PRIMARY_CONTAINER
        val barW = 16f
        val barH = 56f
        val cx = pauseRect.x + pauseRect.width / 2f
        val cy = pauseRect.y + pauseRect.height / 2f
        shapes.rect(cx - barW - 8f, cy - barH / 2f, barW, barH)
        shapes.rect(cx + 8f, cy - barH / 2f, barW, barH)

        shapes.end()

        // text overlay
        batch.projectionMatrix = uiViewport.camera.combined
        batch.begin()

        val labelFont = game.fonts[Theme.FontSize.LABEL_SM, true]
        val valueFont = game.fonts[Theme.FontSize.HEADLINE, true]

        // SCORE card
        labelFont.color = Theme.Palette.TERTIARY
        labelFont.draw(batch, "DATA_STORE", scoreCardRect.x + 18f, scoreCardRect.y + scoreCardRect.height - 20f)
        valueFont.color = Theme.Palette.TERTIARY
        valueFont.draw(batch, "%07d".format(state.score), scoreCardRect.x + 18f, scoreCardRect.y + 50f)

        // SECTOR card (label + valore centrati)
        labelFont.color = Theme.Palette.PRIMARY_CONTAINER
        val sectorLabel = "SECTOR"
        layout.setText(labelFont, sectorLabel)
        labelFont.draw(batch, sectorLabel, sectorCardRect.x + (sectorCardRect.width - layout.width) / 2f, sectorCardRect.y + sectorCardRect.height - 20f)
        valueFont.color = Theme.Palette.PRIMARY_CONTAINER
        val sectorVal = "%02d".format(state.levelIndex)
        layout.setText(valueFont, sectorVal)
        valueFont.draw(batch, sectorVal, sectorCardRect.x + (sectorCardRect.width - layout.width) / 2f, sectorCardRect.y + 50f)

        // INTEGRITY card (label allineato a destra)
        labelFont.color = Theme.Palette.SECONDARY_FIXED_DIM
        val intLabel = "INTEGRITY"
        layout.setText(labelFont, intLabel)
        labelFont.draw(batch, intLabel, integrityCardRect.x + integrityCardRect.width - 18f - layout.width, integrityCardRect.y + integrityCardRect.height - 20f)
        if (mode == GameMode.PRACTICE) {
            valueFont.color = Theme.Palette.SECONDARY_FIXED_DIM
            val infStr = "INF"
            layout.setText(valueFont, infStr)
            valueFont.draw(batch, infStr, integrityCardRect.x + integrityCardRect.width - 18f - layout.width, integrityCardRect.y + 50f)
        }

        // hint footer "SENSORS ACTIVE   SLIDE TO STEER"
        val hintFont = game.fonts[Theme.FontSize.LABEL_SM, true]
        hintFont.color = Theme.Palette.SECONDARY_FIXED_DIM
        val hint = "SENSORS ACTIVE   SLIDE TO STEER"
        layout.setText(hintFont, hint)
        hintFont.draw(batch, hint, (UI_W - layout.width) / 2f, 80f)

        // marker DAILY (sostituisce il SECTOR number nel HUD-top? no: aggiungo sopra il SECTOR card)
        if (mode == GameMode.DAILY) {
            val dailyFont = game.fonts[Theme.FontSize.LABEL_SM, true]
            dailyFont.color = Theme.Palette.TERTIARY
            val dailyTxt = "DAILY CHALLENGE   ${DailySeed.dateKey()}"
            layout.setText(dailyFont, dailyTxt)
            dailyFont.draw(batch, dailyTxt, (UI_W - layout.width) / 2f, UI_H - 40f)
        }

        // TAP TO LAUNCH if at least one ball is stuck
        if (state.balls.any { it.stuckToPaddle }) {
            val launchFont = game.fonts[Theme.FontSize.BODY_MD, true]
            launchFont.color = Theme.Palette.PRIMARY_FIXED
            val launchHint = "TAP TO LAUNCH"
            layout.setText(launchFont, launchHint)
            launchFont.draw(batch, launchHint, (UI_W - layout.width) / 2f, 140f)
        }

        // power-up pickup popup (durata breve, esplode alla fine)
        val popupTextLocal = popupText
        if (popupTextLocal != null && popupRemaining > 0f) {
            val popupFont = game.fonts[Theme.FontSize.DISPLAY, true]
            val t = (popupRemaining / POPUP_LIFETIME).coerceIn(0f, 1f)
            tmpColor.set(popupColor).also { it.a = (t * 2f).coerceIn(0.4f, 1f) }
            popupFont.color = tmpColor
            layout.setText(popupFont, popupTextLocal)
            popupFont.draw(batch, popupTextLocal, (UI_W - layout.width) / 2f, UI_H * 0.5f)
            popupFont.color = tmpColor.set(Color.WHITE)
        }

        batch.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        com.arkamadoid.render.BezelFrame.draw(shapes, uiViewport, UI_W, UI_H)
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
        const val POPUP_LIFETIME = 0.45f
        const val LASER_COOLDOWN = 0.20f

        const val SHAKE_INTENSITY_LIGHT = 1.5f
        const val SHAKE_INTENSITY_HEAVY = 4f
        const val SHAKE_DURATION_LIGHT = 0.12f
        const val SHAKE_DURATION_HEAVY = 0.22f
        const val HIT_STOP_LIGHT = 0.025f
        const val HIT_STOP_HEAVY = 0.06f
        const val HAPTIC_LIGHT_MS = 12
        const val HAPTIC_HEAVY_MS = 40
        const val HAPTIC_LIFE_LOST_MS = 90
        const val HAPTIC_POWERUP_MS = 18
    }
}
