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

    fun setDirectionDeg(angleDeg: Float) {
        val rad = Math.toRadians(angleDeg.toDouble())
        velocity.set(Math.sin(rad).toFloat(), Math.cos(rad).toFloat()).nor().scl(speed)
    }
}
