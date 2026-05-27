package com.arkamadoid.entities

import com.badlogic.gdx.math.Rectangle

class LaserBolt(
    var x: Float,
    var y: Float,
) {
    val bounds: Rectangle get() = Rectangle(x - WIDTH / 2f, y, WIDTH, HEIGHT)

    companion object {
        const val WIDTH = 2f
        const val HEIGHT = 8f
        const val SPEED = 300f
    }
}
