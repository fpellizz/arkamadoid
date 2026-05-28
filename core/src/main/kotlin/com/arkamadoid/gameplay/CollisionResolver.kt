package com.arkamadoid.gameplay

import com.arkamadoid.entities.Ball
import com.arkamadoid.entities.Boss
import com.arkamadoid.entities.Brick
import com.arkamadoid.entities.Paddle
import kotlin.math.sqrt

object CollisionResolver {

    fun ballVsWalls(ball: Ball, width: Float, height: Float): WallHit {
        if (ball.x - ball.radius < 0f) {
            ball.x = ball.radius
            ball.velocity.x = -ball.velocity.x
            return WallHit.LEFT
        }
        if (ball.x + ball.radius > width) {
            ball.x = width - ball.radius
            ball.velocity.x = -ball.velocity.x
            return WallHit.RIGHT
        }
        if (ball.y + ball.radius > height) {
            ball.y = height - ball.radius
            ball.velocity.y = -ball.velocity.y
            return WallHit.TOP
        }
        if (ball.y - ball.radius < 0f) {
            return WallHit.BOTTOM
        }
        return WallHit.NONE
    }

    fun ballVsPaddle(ball: Ball, paddle: Paddle): Boolean {
        val r = paddle.bounds
        val cx = ball.x.coerceIn(r.x, r.x + r.width)
        val cy = ball.y.coerceIn(r.y, r.y + r.height)
        val dx = ball.x - cx
        val dy = ball.y - cy
        if (dx * dx + dy * dy > ball.radius * ball.radius) return false
        if (ball.velocity.y >= 0f) return false

        ball.setDirectionDeg(paddle.bounceAngleFor(ball.x))
        ball.y = r.y + r.height + ball.radius
        return true
    }

    fun ballVsBrick(ball: Ball, brick: Brick): Boolean {
        if (!brick.alive) return false
        val r = brick.bounds
        val cx = ball.x.coerceIn(r.x, r.x + r.width)
        val cy = ball.y.coerceIn(r.y, r.y + r.height)
        val dx = ball.x - cx
        val dy = ball.y - cy
        val dist2 = dx * dx + dy * dy
        if (dist2 > ball.radius * ball.radius) return false

        // BLACKBALL: passa attraverso il brick e lo annichila (anche INDESTRUCTIBLE),
        // niente riflesso né correzione di posizione
        if (ball.isBlackBall) {
            brick.hp = 0
            return true
        }

        val nx: Float
        val ny: Float
        if (dist2 > 1e-6f) {
            val dist = sqrt(dist2)
            nx = dx / dist
            ny = dy / dist
            ball.x = cx + nx * ball.radius
            ball.y = cy + ny * ball.radius
        } else {
            val penLeft = ball.x - r.x
            val penRight = (r.x + r.width) - ball.x
            val penBottom = ball.y - r.y
            val penTop = (r.y + r.height) - ball.y
            val minPen = minOf(penLeft, penRight, penBottom, penTop)
            when (minPen) {
                penLeft -> { nx = -1f; ny = 0f; ball.x = r.x - ball.radius }
                penRight -> { nx = 1f; ny = 0f; ball.x = r.x + r.width + ball.radius }
                penBottom -> { nx = 0f; ny = -1f; ball.y = r.y - ball.radius }
                else -> { nx = 0f; ny = 1f; ball.y = r.y + r.height + ball.radius }
            }
        }

        val dot = ball.velocity.x * nx + ball.velocity.y * ny
        ball.velocity.x -= 2f * dot * nx
        ball.velocity.y -= 2f * dot * ny

        if (brick.type != Brick.Type.INDESTRUCTIBLE) brick.hp -= 1
        return true
    }

    /**
     * Collisione palla-boss: stesso schema circle-vs-AABB di ballVsBrick.
     * - Ritorna true se c'è impatto, decrementando boss.hp di 1.
     * - BLACKBALL: passa attraverso senza riflesso, infligge 1 danno comunque
     *   (no one-shot del boss come accade per i brick).
     */
    fun ballVsBoss(ball: Ball, boss: Boss): Boolean {
        if (!boss.alive) return false
        val r = boss.bounds
        val cx = ball.x.coerceIn(r.x, r.x + r.width)
        val cy = ball.y.coerceIn(r.y, r.y + r.height)
        val dx = ball.x - cx
        val dy = ball.y - cy
        val dist2 = dx * dx + dy * dy
        if (dist2 > ball.radius * ball.radius) return false

        if (ball.isBlackBall) {
            boss.hp -= 1
            return true
        }

        val nx: Float
        val ny: Float
        if (dist2 > 1e-6f) {
            val dist = sqrt(dist2)
            nx = dx / dist
            ny = dy / dist
            ball.x = cx + nx * ball.radius
            ball.y = cy + ny * ball.radius
        } else {
            val penLeft = ball.x - r.x
            val penRight = (r.x + r.width) - ball.x
            val penBottom = ball.y - r.y
            val penTop = (r.y + r.height) - ball.y
            val minPen = minOf(penLeft, penRight, penBottom, penTop)
            when (minPen) {
                penLeft -> { nx = -1f; ny = 0f; ball.x = r.x - ball.radius }
                penRight -> { nx = 1f; ny = 0f; ball.x = r.x + r.width + ball.radius }
                penBottom -> { nx = 0f; ny = -1f; ball.y = r.y - ball.radius }
                else -> { nx = 0f; ny = 1f; ball.y = r.y + r.height + ball.radius }
            }
        }

        val dot = ball.velocity.x * nx + ball.velocity.y * ny
        ball.velocity.x -= 2f * dot * nx
        ball.velocity.y -= 2f * dot * ny

        boss.hp -= 1
        return true
    }

    enum class WallHit { NONE, LEFT, RIGHT, TOP, BOTTOM }
}
