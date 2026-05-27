package com.arkamadoid.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ParticleSystem {

    private val particles = ArrayList<Particle>()

    fun burstAt(x: Float, y: Float, color: Color, count: Int = 8) {
        repeat(count) {
            val baseAngle = (it.toDouble() / count) * 2.0 * Math.PI
            val angle = baseAngle + Random.nextDouble(-0.4, 0.4)
            val speed = Random.nextFloat() * 30f + 15f
            val life = Random.nextFloat() * 0.3f + 0.3f
            particles += Particle(
                x = x,
                y = y,
                vx = cos(angle).toFloat() * speed,
                vy = sin(angle).toFloat() * speed,
                color = color,
                life = life,
                maxLife = life,
            )
        }
    }

    fun update(delta: Float) {
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.x += p.vx * delta
            p.y += p.vy * delta
            p.vy -= 60f * delta
            p.life -= delta
            if (p.life <= 0f) it.remove()
        }
    }

    fun render(shapes: ShapeRenderer) {
        for (p in particles) {
            val t = (p.life / p.maxLife).coerceIn(0f, 1f)
            val size = 1f + 1.5f * t
            shapes.color = p.color
            shapes.rect(p.x - size / 2f, p.y - size / 2f, size, size)
        }
    }

    fun clear() = particles.clear()

    private class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val color: Color,
        var life: Float,
        val maxLife: Float,
    )
}
