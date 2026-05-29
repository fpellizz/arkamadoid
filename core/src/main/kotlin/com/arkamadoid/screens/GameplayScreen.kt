package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.achievements.Achievement
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
import com.arkamadoid.gameplay.EndlessLevelGenerator
import com.arkamadoid.gameplay.GameState
import com.arkamadoid.gameplay.Level
import com.arkamadoid.gameplay.LevelLoader
import com.arkamadoid.input.TouchController
import com.arkamadoid.localization.I18n
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
    private val zeroGravRect = Rectangle(40f, 40f, 130f, 100f)
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

    // achievement popup (separato dal power-up popup, vita più lunga)
    private var achPopupTitle: String? = null
    private var achPopupDesc: String? = null
    private var achPopupRemaining = 0f

    // counter per-level: usati per PIXEL_PERFECT (no ball lost) e NO_POWER (no power-up pickup)
    private var ballsLostThisLevel = 0
    private var powerUpsThisLevel = 0
    private var lastShownComboMultiplier = 1

    private var shakeMagnitude = 0f
    private var shakeRemaining = 0f
    private var hitStopRemaining = 0f

    private var practiceZeroGEnabled = true
    private var zeroGravityRemaining = 0f
    private val isZeroGravityActive: Boolean
        get() = (mode == GameMode.PRACTICE && practiceZeroGEnabled) || zeroGravityRemaining > 0f

    private val playFieldHeight = GameConfig.VIRTUAL_HEIGHT.toFloat()
    private val playFieldWidth = GameConfig.VIRTUAL_WIDTH.toFloat()

    private var accumulator = 0f
    private var lastTouchX = -1f

    private val brickPalette: Array<Color>
        get() = game.prefs.currentPaletteSkin().colors

    private val droppableWeights = listOf(
        PowerUpType.EXPAND to 1f,
        PowerUpType.SLOW to 1f,
        PowerUpType.MULTI to 1f,
        PowerUpType.LIFE to 0.5f,
        PowerUpType.LASER to 1f,
        PowerUpType.CATCH to 1f,
        PowerUpType.BLACKBALL to 0.12f,
        PowerUpType.ZEROGRAV to 0.8f,
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
        val handle = Gdx.files.internal("levels/%02d.json".format(index))
        val level = if (handle.exists()) {
            LevelLoader.load(index)
        } else {
            // oltre i livelli hand-crafted, ENDLESS continua con il generatore procedurale
            EndlessLevelGenerator.generate(index)
        }
        state.currentLevel = level
        ballsLostThisLevel = 0
        powerUpsThisLevel = 0
        lastShownComboMultiplier = 1
        positionPaddleAndBall()
        if (level.boss != null) game.audio.playMusic(MusicTrack.BOSS)
        // ENDLESS_30: appena entri nel sector 30+ in ENDLESS
        if (mode == GameMode.ENDLESS && index >= 30) tryUnlockAchievement(Achievement.ENDLESS_30)
    }

    private fun tryUnlockAchievement(a: Achievement) {
        if (!game.prefs.unlockAchievement(a.id)) return
        game.platform.gpgs.unlockAchievement(a.id)
        achPopupTitle = I18n["achievement.${a.id}.title"]
        achPopupDesc = I18n["achievement.${a.id}.desc"]
        achPopupRemaining = ACHIEVEMENT_POPUP_LIFETIME
        game.audio.playSfx(AudioManager.Sfx.COIN, pitch = 1.4f)
    }

    private fun positionPaddleAndBall() {
        val p = state.paddle
        p.width = GameConfig.PADDLE_BASE_WIDTH.toFloat()
        p.hasLaser = false
        p.hasCatch = false
        p.x = (playFieldWidth - p.width) / 2f
        p.y = PADDLE_Y

        state.balls.clear()
        state.combo = 0
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
        if (achPopupRemaining > 0f) {
            achPopupRemaining -= delta
            if (achPopupRemaining <= 0f) {
                achPopupTitle = null
                achPopupDesc = null
            }
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
            if (mode == GameMode.PRACTICE && zeroGravRect.contains(tmpUi.x, tmpUi.y)) {
                practiceZeroGEnabled = !practiceZeroGEnabled
                game.audio.playSfx(AudioManager.Sfx.POWERUP, pitch = if (practiceZeroGEnabled) 1.2f else 0.8f)
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
            // ZERO-GRAVITY: la palla rallenta nella metà bassa del field.
            // Attivabile via toggle in PRACTICE o via power-up ZEROGRAV nelle altre modalità.
            val ballDt = if (isZeroGravityActive && ball.y < playFieldHeight / 2f)
                dt * ZEROGRAV_SLOW_FACTOR else dt
            ball.x += ball.velocity.x * ballDt
            ball.y += ball.velocity.y * ballDt
            if (!game.prefs.data.reduceMotion) ball.pushTrail()

            val wallHit = CollisionResolver.ballVsWalls(ball, playFieldWidth, playFieldHeight)
            if (wallHit == WallHit.BOTTOM) {
                iter.remove()
                continue
            }
            if (wallHit != WallHit.NONE) game.audio.playSfx(AudioManager.Sfx.BOUNCE, pitch = 1.2f)

            if (CollisionResolver.ballVsPaddle(ball, state.paddle)) {
                game.audio.playSfx(AudioManager.Sfx.BOUNCE)
                // COMBO: il bounce paddle rompe la catena (risk/reward)
                state.combo = 0
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
                    // BLACKBALL: continua, distrugge tutti i brick lungo la traiettoria nello stesso step
                    if (!ball.isBlackBall) break
                }
            }

            // boss hit: dopo il loop brick (così non interferisce con BLACKBALL chain)
            val boss = level.boss
            if (boss != null && boss.alive && CollisionResolver.ballVsBoss(ball, boss)) {
                bumpCombo()
                state.score += BOSS_HIT_SCORE * comboMultiplier(state.combo)
                checkComboAchievements()
                checkScoreAchievements()
                game.audio.playSfx(AudioManager.Sfx.BRICK, pitch = 0.8f)
                if (!boss.alive) onBossDefeated(boss)
            }

            if (ball.blackBallRemaining > 0f) ball.blackBallRemaining -= dt
        }

        level.boss?.takeIf { it.alive }?.update(dt)

        if (zeroGravityRemaining > 0f) zeroGravityRemaining -= dt

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
        bumpCombo()
        state.score += brick.type.score * comboMultiplier(state.combo)
        tryUnlockAchievement(Achievement.FIRST_BRICK)
        checkComboAchievements()
        checkScoreAchievements()
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
                type = pickWeightedPowerUp(),
            )
        }
        if (brick.type == Brick.Type.EXPLOSIVE) explodeAround(brick, level)
    }

    private fun onBossDefeated(boss: com.arkamadoid.entities.Boss) {
        state.score += BOSS_KILL_SCORE
        tryUnlockAchievement(Achievement.BOSS_FIRST)
        if (state.levelIndex == GameConfig.MAX_LEVELS) tryUnlockAchievement(Achievement.BOSS_FINAL)
        checkScoreAchievements()
        triggerImpact(explosive = true)
        // big burst di particelle magenta
        val count = if (game.prefs.data.reduceMotion) 12 else 40
        particles.burstAt(boss.centerX(), boss.centerY(), Theme.Palette.PRIMARY_CONTAINER, count)
        particles.burstAt(boss.centerX(), boss.centerY(), Theme.Palette.TERTIARY, count / 2)
        // shower di power-up dal centro del boss
        val drops = listOf(
            PowerUpType.MULTI,
            PowerUpType.EXPAND,
            PowerUpType.LASER,
            PowerUpType.LIFE,
            PowerUpType.ZEROGRAV,
        )
        drops.forEachIndexed { i, t ->
            droppingPowerUps += PowerUp(
                x = boss.centerX() - 8f + (i - drops.size / 2f) * 18f,
                y = boss.y,
                type = t,
            )
        }
        game.audio.playSfx(AudioManager.Sfx.POWERUP, pitch = 0.7f)
        game.audio.playSfx(AudioManager.Sfx.BRICK, pitch = 0.5f)
        haptic(HAPTIC_LIFE_LOST_MS)
        triggerPickupPopup(PowerUpType.MULTI) // riusa il popup, ma override del testo subito sotto
        popupText = I18n["gameplay.bossCleared"]
        popupColor.set(Theme.Palette.PRIMARY_CONTAINER)
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
            bumpCombo()
            state.score += b.type.score * comboMultiplier(state.combo)
            checkComboAchievements()
            checkScoreAchievements()
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
        powerUpsThisLevel += 1
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
            PowerUpType.LIFE -> {
                state.lives += 1
                game.audio.playSfx(AudioManager.Sfx.COIN)
            }
            PowerUpType.LASER -> state.paddle.hasLaser = true
            PowerUpType.CATCH -> state.paddle.hasCatch = true
            PowerUpType.WARP -> onLevelComplete()
            PowerUpType.BLACKBALL -> {
                for (b in state.balls) b.blackBallRemaining = BLACKBALL_DURATION
            }
            PowerUpType.ZEROGRAV -> zeroGravityRemaining = ZEROGRAV_DURATION
        }
    }

    private fun pickWeightedPowerUp(): PowerUpType {
        val total = droppableWeights.sumOf { it.second.toDouble() }.toFloat()
        var r = Random.nextFloat() * total
        for ((t, w) in droppableWeights) {
            r -= w
            if (r <= 0f) return t
        }
        return droppableWeights.last().first
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
        ballsLostThisLevel += 1
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
            game.setScreen(GameOverScreen(game, state.score, state.levelIndex, mode = mode.name, bestCombo = state.bestComboThisRun))
            return
        }
        positionPaddleAndBall()
    }

    private fun onLevelComplete() {
        // achievement per-level (escluso PRACTICE — è training, non vale)
        if (mode != GameMode.PRACTICE) {
            if (ballsLostThisLevel == 0) tryUnlockAchievement(Achievement.PIXEL_PERFECT)
            if (powerUpsThisLevel == 0) tryUnlockAchievement(Achievement.NO_POWER)
        }
        if (mode == GameMode.DAILY) {
            recordDailyAndExit()
            return
        }
        val next = state.levelIndex + 1
        val handle = Gdx.files.internal("levels/%02d.json".format(next))
        if (!handle.exists() && mode != GameMode.ENDLESS) {
            // modalità che terminano alla fine dei livelli hand-crafted (ARCADE, PRACTICE)
            disposeOnHide = true
            game.setScreen(GameOverScreen(game, state.score, state.levelIndex, mode = mode.name, bestCombo = state.bestComboThisRun))
            return
        }
        state.levelIndex = next
        loadLevel(next)
    }

    private fun recordDailyAndExit() {
        game.prefs.recordDailyScore(DailySeed.dateKey(), state.score)
        val streak = game.prefs.dailyStreak
        if (streak >= 3) tryUnlockAchievement(Achievement.DAILY_3)
        if (streak >= 7) tryUnlockAchievement(Achievement.DAILY_7)
        disposeOnHide = true
        game.setScreen(GameOverScreen(game, state.score, state.levelIndex, daily = true, mode = mode.name, bestCombo = state.bestComboThisRun))
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

    /**
     * Disegna la lettera identificativa del power-up (codice della pill) dentro
     * il pill stesso, in nero, usando un bitmap font 3x5 composto di 1x1 rect.
     * Va chiamato durante il loop power-up dentro shapes.begin(Filled).
     */
    private fun drawPowerUpGlyph(pu: PowerUp) {
        val pattern = POWERUP_GLYPHS[pu.type.code] ?: return
        shapes.color = Color.BLACK
        // pill 16x8, glyph 3x5: centro orizzontale a x+6.5, top at pu.y+6 (bottom at pu.y+1)
        val baseX = pu.x + 6.5f
        val baseTopY = pu.y + 6f
        for (cell in pattern) {
            val col = cell shr 4
            val row = cell and 0xF
            shapes.rect(baseX + col, baseTopY - row, 1f, 1f)
        }
    }

    private fun comboMultiplier(c: Int): Int = when {
        c >= COMBO_TIER_4 -> 4
        c >= COMBO_TIER_3 -> 3
        c >= COMBO_TIER_2 -> 2
        else -> 1
    }

    private fun bumpCombo() {
        state.combo += 1
        if (state.combo > state.bestComboThisRun) state.bestComboThisRun = state.combo
    }

    private fun checkComboAchievements() {
        val mult = comboMultiplier(state.combo)
        if (mult <= lastShownComboMultiplier) return
        lastShownComboMultiplier = mult
        if (mult >= 2) tryUnlockAchievement(Achievement.COMBO_X2)
        if (mult >= 4) tryUnlockAchievement(Achievement.COMBO_X4)
    }

    private fun checkScoreAchievements() {
        if (state.score >= 100_000) tryUnlockAchievement(Achievement.CENTURION)
    }

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
        PowerUpType.BLACKBALL -> "VOID BALL!"
        PowerUpType.ZEROGRAV -> "Zero-Gravity!"
    }

    private fun powerUpColor(type: PowerUpType): Color = when (type) {
        PowerUpType.EXPAND -> Theme.Palette.SECONDARY_FIXED
        PowerUpType.SLOW -> Theme.Palette.SECONDARY_FIXED_DIM
        PowerUpType.LASER -> Theme.Palette.ERROR
        PowerUpType.MULTI -> Theme.Palette.PRIMARY_CONTAINER
        PowerUpType.CATCH -> Theme.Palette.TERTIARY
        PowerUpType.LIFE -> Color.WHITE
        PowerUpType.WARP -> Theme.Palette.PRIMARY_FIXED
        PowerUpType.BLACKBALL -> Theme.Palette.SURFACE_CONTAINER_HIGHEST
        PowerUpType.ZEROGRAV -> Theme.Palette.NEON_GREEN
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
            if (brick.type == Brick.Type.INDESTRUCTIBLE) {
                PlayfieldRenderer.steelBrick(shapes, brick.x + 1f, brick.y + 1f, brick.width - 2f, brick.height - 2f, cornerRadius = 0.5f)
            } else {
                val base = brickColorOf(brick)
                val col = if (brick.hp < brick.type.hp) damagedTint.set(base).lerp(Color.WHITE, 0.35f) else base
                PlayfieldRenderer.glowRect(shapes, brick.x + 2f, brick.y + 1.5f, brick.width - 4f, brick.height - 3f, col, cornerRadius = 1f)
            }
        }

        // boss: corpo magenta + armor plate centrale + 2 occhi + chevron in basso + HP bar
        state.currentLevel?.boss?.let { boss ->
            if (boss.alive) {
                val hpRatio = boss.hp.toFloat() / boss.maxHp.toFloat().coerceAtLeast(1f)
                val damaged = hpRatio < 0.4f
                val bossCol = if (damaged) damagedTint.set(Theme.Palette.PRIMARY_CONTAINER).lerp(Color.WHITE, 0.4f)
                               else Theme.Palette.PRIMARY_CONTAINER

                // corpo principale (capsule magenta con halo)
                PlayfieldRenderer.glowRect(shapes, boss.x, boss.y, boss.width, boss.height, bossCol, cornerRadius = 2f)

                // armor plate centrale (un rettangolo più piccolo, colore PRIMARY più chiaro)
                val plateInset = 4f
                val plateH = boss.height - plateInset * 2f
                val plateW = boss.width * 0.55f
                shapes.color = if (damaged) Theme.Palette.PRIMARY_FIXED else Theme.Palette.NEON_MAGENTA
                shapes.rect(boss.centerX() - plateW / 2f, boss.y + plateInset, plateW, plateH)

                // occhi: 2 piccoli rettangoli orizzontali nel terzo superiore del boss
                val eyeY = boss.y + boss.height * 0.62f
                val eyeH = 3f
                val eyeW = 5f
                val eyeOffset = boss.width * 0.18f
                val eyeColor = if (damaged) Theme.Palette.ERROR else Theme.Palette.NEON_YELLOW
                shapes.color = eyeColor
                shapes.rect(boss.centerX() - eyeOffset - eyeW, eyeY, eyeW, eyeH)
                shapes.rect(boss.centerX() + eyeOffset, eyeY, eyeW, eyeH)

                // chevron centrale in basso: due triangolini con rects sottili
                val chY = boss.y + 1f
                val chW = 3f
                val chH = 2f
                shapes.color = if (damaged) Theme.Palette.SECONDARY_FIXED_DIM else Theme.Palette.SECONDARY_FIXED
                shapes.rect(boss.centerX() - chW * 2f - 1f, chY, chW, chH)
                shapes.rect(boss.centerX() + 1f, chY, chW, chH)

                // HP bar 2px sopra il boss
                val barY = boss.y + boss.height + 3f
                shapes.color = Theme.Palette.SURFACE_CONTAINER_HIGH
                shapes.rect(boss.x, barY, boss.width, 2f)
                shapes.color = if (damaged) Theme.Palette.ERROR else Theme.Palette.NEON_GREEN
                shapes.rect(boss.x, barY, boss.width * hpRatio, 2f)
            }
        }

        // power-up drops (con glow + lettera identificativa)
        for (pu in droppingPowerUps) {
            PlayfieldRenderer.glowRect(shapes, pu.x, pu.y, 16f, 8f, powerUpColor(pu.type))
            drawPowerUpGlyph(pu)
        }

        // particles
        particles.render(shapes)

        // paddle (capsule con colore della skin selezionata)
        val p = state.paddle
        PlayfieldRenderer.capsule(shapes, p.x, p.y, p.width, p.height, game.prefs.currentPaddleSkin().color)

        // cannoni laser sui lati del paddle se hasLaser è attivo
        if (p.hasLaser) {
            PlayfieldRenderer.glowRect(shapes, p.x + 2f, p.y + p.height, 2f, 3f, Theme.Palette.ERROR)
            PlayfieldRenderer.glowRect(shapes, p.x + p.width - 4f, p.y + p.height, 2f, 3f, Theme.Palette.ERROR)
        }

        // laser bolts in volo
        for (bolt in laserBolts) {
            PlayfieldRenderer.glowRect(shapes, bolt.x - LaserBolt.WIDTH / 2f, bolt.y, LaserBolt.WIDTH, LaserBolt.HEIGHT, Theme.Palette.ERROR)
        }

        // balls + trail. BLACKBALL: variante "void". Altrimenti skin selezionata.
        val ballSkin = game.prefs.currentBallSkin()
        for (ball in state.balls) {
            PlayfieldRenderer.ballTrail(shapes, ball.trailX, ball.trailY, ball.trailHead, ball.trailCount, ball.radius)
            if (ball.isBlackBall) PlayfieldRenderer.glowBallVoid(shapes, ball.x, ball.y, ball.radius)
            else PlayfieldRenderer.glowBall(shapes, ball.x, ball.y, ball.radius, ballSkin.core, ballSkin.halo)
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

        // l'INTEGRITY come numero viene disegnato nello stage testi (sotto), niente shape qui

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

        // COMBO overlay (sotto la score card, visibile solo da combo ≥ 2)
        if (state.combo >= COMBO_DISPLAY_MIN) {
            val mult = comboMultiplier(state.combo)
            val comboFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
            comboFont.color = Theme.Palette.PRIMARY_CONTAINER
            val comboTxt = "COMBO %02d ×%d".format(state.combo, mult)
            comboFont.draw(batch, comboTxt, scoreCardRect.x + 18f, scoreCardRect.y - 14f)
        }

        // SECTOR card (label + valore centrati)
        labelFont.color = Theme.Palette.PRIMARY_CONTAINER
        val sectorLabel = "SECTOR"
        layout.setText(labelFont, sectorLabel)
        labelFont.draw(batch, sectorLabel, sectorCardRect.x + (sectorCardRect.width - layout.width) / 2f, sectorCardRect.y + sectorCardRect.height - 20f)
        valueFont.color = Theme.Palette.PRIMARY_CONTAINER
        val sectorVal = "%02d".format(state.levelIndex)
        layout.setText(valueFont, sectorVal)
        valueFont.draw(batch, sectorVal, sectorCardRect.x + (sectorCardRect.width - layout.width) / 2f, sectorCardRect.y + 50f)

        // INTEGRITY card (label allineato a destra) — valore numerico
        labelFont.color = Theme.Palette.SECONDARY_FIXED_DIM
        val intLabel = "INTEGRITY"
        layout.setText(labelFont, intLabel)
        labelFont.draw(batch, intLabel, integrityCardRect.x + integrityCardRect.width - 18f - layout.width, integrityCardRect.y + integrityCardRect.height - 20f)
        valueFont.color = Theme.Palette.SECONDARY_FIXED_DIM
        val intVal = when {
            mode == GameMode.PRACTICE -> "INF"
            state.lives <= 0 -> "00"
            else -> "%02d".format(state.lives)
        }
        layout.setText(valueFont, intVal)
        valueFont.draw(batch, intVal, integrityCardRect.x + integrityCardRect.width - 18f - layout.width, integrityCardRect.y + 50f)

        // 0-G toggle (solo PRACTICE): testo "0-G" centrato nel bottone, verde se attivo
        if (mode == GameMode.PRACTICE) {
            val zgFont = game.fonts[Theme.FontSize.HEADLINE, true]
            zgFont.color = if (practiceZeroGEnabled) Theme.Palette.NEON_GREEN else Theme.Palette.OUTLINE_VARIANT
            val zgLabel = "0-G"
            layout.setText(zgFont, zgLabel)
            zgFont.draw(
                batch,
                zgLabel,
                zeroGravRect.x + (zeroGravRect.width - layout.width) / 2f,
                zeroGravRect.y + (zeroGravRect.height + layout.height) / 2f,
            )
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
            val a = (t * 2f).coerceIn(0.4f, 1f)
            layout.setText(popupFont, popupTextLocal)
            val px = (UI_W - layout.width) / 2f
            val py = UI_H * 0.5f
            // halo bianco offset 8 direzioni per leggibilità su qualunque sfondo/colore testo
            tmpColor.set(Color.WHITE).also { it.a = a * 0.55f }
            popupFont.color = tmpColor
            val o = 3f
            popupFont.draw(batch, popupTextLocal, px - o, py)
            popupFont.draw(batch, popupTextLocal, px + o, py)
            popupFont.draw(batch, popupTextLocal, px, py - o)
            popupFont.draw(batch, popupTextLocal, px, py + o)
            popupFont.draw(batch, popupTextLocal, px - o, py - o)
            popupFont.draw(batch, popupTextLocal, px + o, py - o)
            popupFont.draw(batch, popupTextLocal, px - o, py + o)
            popupFont.draw(batch, popupTextLocal, px + o, py + o)
            // testo principale sopra
            tmpColor.set(popupColor).also { it.a = a }
            popupFont.color = tmpColor
            popupFont.draw(batch, popupTextLocal, px, py)
            popupFont.color = tmpColor.set(Color.WHITE)
        }

        // achievement popup: card in alto centrale con titolo + descrizione
        val achTitle = achPopupTitle
        val achDesc = achPopupDesc
        if (achTitle != null && achDesc != null && achPopupRemaining > 0f) {
            // fade-in nei primi 0.2s, fade-out negli ultimi 0.5s, full opaco nel mezzo
            val a = when {
                achPopupRemaining > ACHIEVEMENT_POPUP_LIFETIME - 0.2f -> (ACHIEVEMENT_POPUP_LIFETIME - achPopupRemaining) / 0.2f
                achPopupRemaining < 0.5f -> achPopupRemaining / 0.5f
                else -> 1f
            }.coerceIn(0f, 1f)

            val labelFontSm = game.fonts[Theme.FontSize.LABEL_SM, true]
            val titleFont = game.fonts[Theme.FontSize.HEADLINE_MOBILE, true]
            val descFont = game.fonts[Theme.FontSize.BODY_MD]

            val headerTxt = "[*] ${I18n["achievement.unlocked"]}"
            val baseY = UI_H * 0.78f
            layout.setText(titleFont, achTitle)
            val titleWidth = layout.width
            layout.setText(descFont, achDesc)
            val descWidth = layout.width
            layout.setText(labelFontSm, headerTxt)
            val headerWidth = layout.width
            val cardWidth = maxOf(titleWidth, descWidth, headerWidth) + 48f
            val cardX = (UI_W - cardWidth) / 2f

            tmpColor.set(Theme.Palette.TERTIARY).also { it.a = a }
            labelFontSm.color = tmpColor
            labelFontSm.draw(batch, headerTxt, cardX + (cardWidth - headerWidth) / 2f, baseY + 76f)

            tmpColor.set(Theme.Palette.PRIMARY).also { it.a = a }
            titleFont.color = tmpColor
            titleFont.draw(batch, achTitle, cardX + (cardWidth - titleWidth) / 2f, baseY + 38f)

            tmpColor.set(Theme.Palette.ON_SURFACE_VARIANT).also { it.a = a }
            descFont.color = tmpColor
            descFont.draw(batch, achDesc, cardX + (cardWidth - descWidth) / 2f, baseY)
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
        const val BLACKBALL_DURATION = 4.5f
        const val ZEROGRAV_DURATION = 12.0f
        const val ZEROGRAV_SLOW_FACTOR = 0.5f
        const val COMBO_TIER_2 = 4
        const val COMBO_TIER_3 = 8
        const val COMBO_TIER_4 = 12
        const val COMBO_DISPLAY_MIN = 2
        const val BOSS_HIT_SCORE = 75
        const val BOSS_KILL_SCORE = 5000
        const val ACHIEVEMENT_POPUP_LIFETIME = 3.0f

        /**
         * Bitmap font 3x5 dei codici power-up. Cell encoding: byte con high nibble
         * = col (0-2), low nibble = row (0-4). Disegnato dentro la pill 16x8.
         */
        private val POWERUP_GLYPHS: Map<Char, IntArray> = mapOf(
            'E' to intArrayOf(0x00, 0x10, 0x20, 0x01, 0x02, 0x12, 0x03, 0x04, 0x14, 0x24),
            'S' to intArrayOf(0x00, 0x10, 0x20, 0x01, 0x02, 0x12, 0x22, 0x23, 0x04, 0x14, 0x24),
            'L' to intArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x14, 0x24),
            'M' to intArrayOf(0x00, 0x20, 0x01, 0x11, 0x21, 0x02, 0x22, 0x03, 0x23, 0x04, 0x24),
            'C' to intArrayOf(0x00, 0x10, 0x20, 0x01, 0x02, 0x03, 0x04, 0x14, 0x24),
            '1' to intArrayOf(0x10, 0x01, 0x11, 0x12, 0x13, 0x04, 0x14, 0x24),
            'W' to intArrayOf(0x00, 0x20, 0x01, 0x21, 0x02, 0x22, 0x03, 0x13, 0x23, 0x14),
            'B' to intArrayOf(0x00, 0x10, 0x01, 0x21, 0x02, 0x12, 0x03, 0x23, 0x04, 0x14),
            'Z' to intArrayOf(0x00, 0x10, 0x20, 0x21, 0x12, 0x03, 0x04, 0x14, 0x24),
        )

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
