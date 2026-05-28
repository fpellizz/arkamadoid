package com.arkamadoid.entities

import com.badlogic.gdx.math.Rectangle

/**
 * Boss della stanza speciale (livelli 8/16/24 + ogni 8 in ENDLESS).
 * Si comporta come un brick gigante con HP elevati e oscillazione orizzontale.
 */
class Boss(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val maxHp: Int,
    val oscillationSpeed: Float = 60f,
    val oscillationRange: Float = 80f,
) {
    var hp: Int = maxHp
    val alive: Boolean get() = hp > 0
    val bounds: Rectangle get() = Rectangle(x, y, width, height)

    // anchor = punto di partenza (centro del moto sinusoidale ping-pong)
    var anchorX: Float = x
    private var t: Float = 0f
    private var dir: Int = 1

    /** Tick di movimento: oscillazione orizzontale ping-pong attorno ad anchorX. */
    fun update(dt: Float) {
        t += dir * oscillationSpeed * dt
        val limit = oscillationRange / 2f
        if (t > limit) { t = limit; dir = -1 }
        else if (t < -limit) { t = -limit; dir = 1 }
        x = anchorX + t
    }

    fun centerX(): Float = x + width / 2f
    fun centerY(): Float = y + height / 2f
}
