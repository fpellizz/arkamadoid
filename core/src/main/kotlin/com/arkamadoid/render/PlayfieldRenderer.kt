package com.arkamadoid.render

import com.arkamadoid.theme.Theme
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

enum class HudCardSide { LEFT, RIGHT, TOP, BOTTOM }

object PlayfieldRenderer {
    private val tmp = Color()

    fun gridBackground(shapes: ShapeRenderer, width: Float, height: Float, step: Float = 16f) {
        shapes.begin(ShapeRenderer.ShapeType.Line)
        tmp.set(Theme.Palette.PRIMARY).also { it.a = 0.05f }
        shapes.color = tmp
        var gx = 0f
        while (gx <= width) { shapes.line(gx, 0f, gx, height); gx += step }
        var gy = 0f
        while (gy <= height) { shapes.line(0f, gy, width, gy); gy += step }
        shapes.end()
    }

    /**
     * Glow rect: due livelli di halo (esterno alpha 0.10 a 2px outside, medio alpha 0.28 a 1px),
     * body solido con corner radius opzionale.
     * Replica il pattern CSS `box-shadow: 0 0 15px alpha 0.8, 0 0 30px alpha 0.4` proporzionato al canvas 240px.
     */
    fun glowRect(
        shapes: ShapeRenderer,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color: Color,
        cornerRadius: Float = 0f,
    ) {
        // 3 layer di alone, crescente verso il body: simula box-shadow CRT bloom multi-blur
        tmp.set(color).also { it.a = 0.06f }
        shapes.color = tmp
        shapes.rect(x - 2.5f, y - 2.5f, w + 5f, h + 5f)
        tmp.set(color).also { it.a = 0.20f }
        shapes.color = tmp
        shapes.rect(x - 1.5f, y - 1.5f, w + 3f, h + 3f)
        tmp.set(color).also { it.a = 0.45f }
        shapes.color = tmp
        shapes.rect(x - 0.5f, y - 0.5f, w + 1f, h + 1f)
        shapes.color = color
        if (cornerRadius <= 0f) {
            shapes.rect(x, y, w, h)
        } else {
            roundedRect(shapes, x, y, w, h, cornerRadius)
        }
    }

    private fun roundedRect(shapes: ShapeRenderer, x: Float, y: Float, w: Float, h: Float, r: Float) {
        val rr = r.coerceAtMost(minOf(w, h) / 2f)
        shapes.rect(x + rr, y, w - 2f * rr, h)
        shapes.rect(x, y + rr, w, h - 2f * rr)
        shapes.circle(x + rr, y + rr, rr, 10)
        shapes.circle(x + w - rr, y + rr, rr, 10)
        shapes.circle(x + rr, y + h - rr, rr, 10)
        shapes.circle(x + w - rr, y + h - rr, rr, 10)
    }

    /**
     * Glow ball: alone esterno pink chiaro (largo, soffuso) + alone bianco medio + core bianco.
     * Replica `box-shadow: 0 0 20px #fff, 0 0 40px #fface8`.
     */
    fun glowBall(shapes: ShapeRenderer, cx: Float, cy: Float, r: Float) {
        // alone esterno pink soffuso, alone medio bianco, alone interno luminoso, core
        tmp.set(Theme.Palette.PRIMARY).also { it.a = 0.20f }
        shapes.color = tmp
        shapes.circle(cx, cy, r * 2.5f, 18)
        tmp.set(Color.WHITE).also { it.a = 0.30f }
        shapes.color = tmp
        shapes.circle(cx, cy, r * 1.8f, 16)
        tmp.set(Color.WHITE).also { it.a = 0.65f }
        shapes.color = tmp
        shapes.circle(cx, cy, r * 1.2f, 14)
        shapes.color = Color.WHITE
        shapes.circle(cx, cy, r, 14)
    }

    /**
     * Disegna il trail della palla con alpha decrescente.
     * Da chiamare PRIMA di glowBall così il core finisce sopra.
     */
    fun ballTrail(
        shapes: ShapeRenderer,
        trailX: FloatArray,
        trailY: FloatArray,
        head: Int,
        count: Int,
        r: Float,
    ) {
        if (count == 0) return
        val size = trailX.size
        // dal più vecchio (più dietro) al più recente (più vicino)
        for (i in 0 until count) {
            val idx = ((head - count + i) % size + size) % size
            val age = (count - 1 - i)  // 0 = più recente, count-1 = più vecchio
            val ageT = age.toFloat() / count
            val alpha = (1f - ageT) * 0.25f
            if (alpha <= 0.01f) continue
            tmp.set(Color.WHITE).also { it.a = alpha }
            shapes.color = tmp
            shapes.circle(trailX[idx], trailY[idx], r * (1f - ageT * 0.4f), 12)
        }
    }

    /**
     * Capsule (paddle): doppio halo cyan + corpo a capsula + barra di highlight bianca interna.
     * Replica `box-shadow: 0 0 25px alpha 1, 0 0 50px alpha 0.5` + `bg-white/60` highlight.
     */
    fun capsule(shapes: ShapeRenderer, x: Float, y: Float, w: Float, h: Float, color: Color) {
        val rad = h / 2f
        // 3 layer alone esterno cyan: replica `box-shadow: 0 0 25px alpha 1, 0 0 50px alpha 0.5`
        tmp.set(color).also { it.a = 0.08f }
        shapes.color = tmp
        shapes.rect(x - 3f, y - 3f, w + 6f, h + 6f)
        tmp.set(color).also { it.a = 0.22f }
        shapes.color = tmp
        shapes.rect(x - 2f, y - 2f, w + 4f, h + 4f)
        tmp.set(color).also { it.a = 0.50f }
        shapes.color = tmp
        shapes.rect(x - 1f, y - 1f, w + 2f, h + 2f)
        // capsule body
        shapes.color = color
        shapes.circle(x + rad, y + rad, rad, 12)
        shapes.circle(x + w - rad, y + rad, rad, 12)
        shapes.rect(x + rad, y, w - 2f * rad, h)
        // highlight bianca interna
        tmp.set(Color.WHITE).also { it.a = 0.6f }
        shapes.color = tmp
        val inset = w * 0.08f
        shapes.rect(x + inset, y + h * 0.4f, w - 2f * inset, h * 0.25f)
    }

    fun hudCard(shapes: ShapeRenderer, r: Rectangle, borderColor: Color, side: HudCardSide, borderThickness: Float = 5f) {
        tmp.set(Theme.Palette.SURFACE_CONTAINER_LOW).also { it.a = 0.7f }
        shapes.color = tmp
        shapes.rect(r.x, r.y, r.width, r.height)
        shapes.color = borderColor
        val t = borderThickness
        when (side) {
            HudCardSide.LEFT -> shapes.rect(r.x, r.y, t, r.height)
            HudCardSide.RIGHT -> shapes.rect(r.x + r.width - t, r.y, t, r.height)
            HudCardSide.BOTTOM -> shapes.rect(r.x, r.y, r.width, t)
            HudCardSide.TOP -> shapes.rect(r.x, r.y + r.height - t, r.width, t)
        }
    }
}
