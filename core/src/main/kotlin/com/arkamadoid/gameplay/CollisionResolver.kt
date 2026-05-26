package com.arkamadoid.gameplay

import com.arkamadoid.entities.Ball
import com.arkamadoid.entities.Brick
import com.arkamadoid.entities.Paddle

object CollisionResolver {
    fun ballVsPaddle(ball: Ball, paddle: Paddle): Boolean {
        // TODO: swept-circle vs AABB; on hit set ball direction from paddle.bounceAngleFor
        return false
    }

    fun ballVsBrick(ball: Ball, brick: Brick): Boolean {
        // TODO: circle vs AABB with normal resolution and HP decrement
        return false
    }

    fun ballVsWalls(ball: Ball, width: Float, height: Float): WallHit {
        // TODO: clamp + reflect
        return WallHit.NONE
    }

    enum class WallHit { NONE, LEFT, RIGHT, TOP, BOTTOM }
}
