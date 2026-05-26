package com.arkamadoid.entities

import com.badlogic.gdx.math.Rectangle

class Brick(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: Type,
) {
    var hp: Int = type.hp
    val alive: Boolean get() = hp > 0
    val bounds: Rectangle get() = Rectangle(x, y, width, height)

    enum class Type(val hp: Int, val score: Int) {
        NORMAL(1, 50),
        TOUGH(2, 100),
        VERY_TOUGH(3, 200),
        INDESTRUCTIBLE(Int.MAX_VALUE, 0),
        EXPLOSIVE(1, 150),
        GOLD(1, 500),
    }
}
