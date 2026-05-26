package com.arkamadoid.config

object GameConfig {
    const val VIRTUAL_WIDTH = 240
    const val VIRTUAL_HEIGHT = 320

    const val TARGET_FPS = 60
    const val FIXED_STEP = 1f / 120f

    const val START_LIVES = 3
    const val EXTRA_LIFE_EVERY = 20_000

    const val BALL_INITIAL_SPEED = 90f
    const val BALL_SPEED_INCREMENT_PER_LEVEL = 8f
    const val BALL_MAX_SPEED = 220f

    const val PADDLE_BASE_WIDTH = 36
    const val PADDLE_EXPAND_WIDTH = 52
    const val PADDLE_HEIGHT = 6

    const val POWERUP_DROP_CHANCE = 0.18f
    const val POWERUP_FALL_SPEED = 50f

    const val ATTRACT_TIMEOUT_SECONDS = 15f
    const val CONTINUE_COUNTDOWN_SECONDS = 9
}
