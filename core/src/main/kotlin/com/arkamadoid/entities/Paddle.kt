package com.arkamadoid.entities

import com.arkamadoid.config.GameConfig
import com.badlogic.gdx.math.Rectangle

class Paddle(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = GameConfig.PADDLE_BASE_WIDTH.toFloat(),
    val height: Float = GameConfig.PADDLE_HEIGHT.toFloat(),
) {
    var hasLaser: Boolean = false
    var hasCatch: Boolean = false

    val bounds: Rectangle get() = Rectangle(x, y, width, height)

    fun bounceAngleFor(hitX: Float): Float {
        val rel = ((hitX - x) / width).coerceIn(0f, 1f)
        // arcade: -75°..+75° distribuito su 5 zone
        return -75f + rel * 150f
    }
}
