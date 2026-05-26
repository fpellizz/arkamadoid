package com.arkamadoid.gameplay

import com.arkamadoid.entities.Brick

class Level(
    val index: Int,
    val name: String,
    val bricks: MutableList<Brick>,
    val ballSpeed: Float,
) {
    val isComplete: Boolean
        get() = bricks.none { it.alive && it.type != Brick.Type.INDESTRUCTIBLE }
}
