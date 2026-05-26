package com.arkamadoid.entities

import com.arkamadoid.config.GameConfig
import com.arkamadoid.powerups.PowerUpType
import com.badlogic.gdx.math.Rectangle

class PowerUp(
    var x: Float,
    var y: Float,
    val type: PowerUpType,
) {
    var fallSpeed: Float = GameConfig.POWERUP_FALL_SPEED
    val bounds: Rectangle get() = Rectangle(x, y, 16f, 8f)

    fun update(delta: Float) {
        y -= fallSpeed * delta
    }
}
