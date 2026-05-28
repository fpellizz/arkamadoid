package com.arkamadoid.entities

import com.arkamadoid.config.GameConfig
import com.badlogic.gdx.math.Vector2

class Ball(
    var x: Float = 0f,
    var y: Float = 0f,
    val radius: Float = 3f,
) {
    val velocity = Vector2(0f, 0f)
    var speed: Float = GameConfig.BALL_INITIAL_SPEED
    var stuckToPaddle: Boolean = true

    // BLACKBALL: secondi residui di "void mode" (attraversa e distrugge tutto, niente rimbalzi sui brick)
    var blackBallRemaining: Float = 0f
    val isBlackBall: Boolean get() = blackBallRemaining > 0f

    // afterimage trail: 4 posizioni precedenti, ring buffer
    val trailX: FloatArray = FloatArray(TRAIL_SIZE)
    val trailY: FloatArray = FloatArray(TRAIL_SIZE)
    var trailHead: Int = 0
    var trailCount: Int = 0

    fun setDirectionDeg(angleDeg: Float) {
        val rad = Math.toRadians(angleDeg.toDouble())
        velocity.set(Math.sin(rad).toFloat(), Math.cos(rad).toFloat()).nor().scl(speed)
    }

    fun pushTrail() {
        trailX[trailHead] = x
        trailY[trailHead] = y
        trailHead = (trailHead + 1) % TRAIL_SIZE
        if (trailCount < TRAIL_SIZE) trailCount++
    }

    fun clearTrail() {
        trailHead = 0
        trailCount = 0
    }

    companion object {
        const val TRAIL_SIZE = 4
    }
}
