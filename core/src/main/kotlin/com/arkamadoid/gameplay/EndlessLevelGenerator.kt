package com.arkamadoid.gameplay

import com.arkamadoid.config.GameConfig
import com.arkamadoid.entities.Boss
import com.arkamadoid.entities.Brick
import kotlin.math.abs
import kotlin.random.Random

/**
 * Generatore deterministico di livelli per la modalità ENDLESS oltre i livelli
 * hand-crafted (`assets/levels/01..MAX_LEVELS.json`). Stesso index → stesso layout,
 * così il giocatore può confrontare run senza che il generatore baddi a caso.
 */
object EndlessLevelGenerator {

    private const val COLS = 13
    private const val CELL_W = 16f
    private const val CELL_H = 8f
    private const val ORIGIN_X = 16f

    fun generate(index: Int): Level {
        // ogni 8 livelli (32, 40, 48, ...): boss room invece di un pattern brick
        if (index % 8 == 0) return generateBossRoom(index)

        val rng = Random(seedFor(index))
        val rows = (6 + (index - GameConfig.MAX_LEVELS) / 6).coerceIn(6, 8)
        val originY = 240f + (rows - 6) * 8f

        val mask = pickPattern(rng).build(COLS, rows, rng)
        val bricks = mutableListOf<Brick>()
        for (r in 0 until rows) {
            for (c in 0 until COLS) {
                if (!mask[r][c]) continue
                val t = pickBrickType(rng, index)
                bricks += Brick(
                    x = ORIGIN_X + c * CELL_W,
                    y = originY - (r + 1) * CELL_H,
                    width = CELL_W,
                    height = CELL_H,
                    type = t,
                    colorIndex = r * 6 / rows.coerceAtLeast(1),
                )
            }
        }
        // assicura che ci sia ALMENO un brick distruggibile, altrimenti il livello è
        // impossibile da completare e Endless si blocca
        if (bricks.none { it.type != Brick.Type.INDESTRUCTIBLE }) {
            val target = bricks.firstOrNull()
            if (target != null) {
                val replaced = Brick(target.x, target.y, target.width, target.height,
                    Brick.Type.TOUGH, target.colorIndex)
                bricks[0] = replaced
            }
        }

        val speed = (260f + (index - GameConfig.MAX_LEVELS) * 4f)
            .coerceIn(GameConfig.BALL_INITIAL_SPEED, GameConfig.BALL_MAX_SPEED)
        return Level(index, "SECTOR %02d".format(index), bricks, speed)
    }

    /**
     * Boss procedurale per i livelli ENDLESS multipli di 8 oltre i 24 hand-crafted.
     * HP cresce linearmente con l'index; dimensione e velocità anch'esse.
     */
    private fun generateBossRoom(index: Int): Level {
        val tier = (index / 8).coerceAtLeast(4) // 32→4, 40→5, 48→6, ...
        val hp = 26 + (tier - 4) * 8
        val width = (80f + (tier - 4) * 4f).coerceAtMost(112f)
        val height = (16f + (tier - 4) * 1f).coerceAtMost(20f)
        val originY = 240f
        val boss = Boss(
            x = (GameConfig.VIRTUAL_WIDTH - width) / 2f,
            y = originY,
            width = width,
            height = height,
            maxHp = hp,
            oscillationSpeed = (70f + (tier - 4) * 8f).coerceAtMost(140f),
            oscillationRange = 96f,
        ).also { it.anchorX = it.x }
        val speed = (300f + (tier - 4) * 4f).coerceIn(GameConfig.BALL_INITIAL_SPEED, GameConfig.BALL_MAX_SPEED)
        return Level(index, "BOSS // GUARDIAN.%02d".format(tier), mutableListOf(), speed, boss = boss)
    }

    private fun seedFor(index: Int): Long {
        var h = index.toLong()
        h = h xor (h ushr 33)
        h *= -0x00aaeaba34f2c2cdL
        h = h xor (h ushr 33)
        h *= -0x3b314601e57a13adL
        h = h xor (h ushr 33)
        return h
    }

    /**
     * Distribuzione dei tipi di brick: cresce con la distanza da MAX_LEVELS.
     * I primi livelli procedurali sono simili agli ultimi handcrafted; oltre +50
     * (cioè dal lvl ~74) la mappa è dominata da VERY_TOUGH con qualche INDESTRUCTIBLE.
     */
    private fun pickBrickType(rng: Random, index: Int): Brick.Type {
        val escalation = ((index - GameConfig.MAX_LEVELS).coerceAtLeast(0)).toFloat() / 50f
        val esc = escalation.coerceAtMost(1f)
        val r = rng.nextFloat()
        return when {
            r < 0.08f * esc -> Brick.Type.INDESTRUCTIBLE
            r < 0.05f + 0.25f * esc -> Brick.Type.VERY_TOUGH
            r < 0.30f + 0.10f * esc -> Brick.Type.TOUGH
            r < 0.40f -> Brick.Type.GOLD
            r < 0.50f -> Brick.Type.EXPLOSIVE
            else -> Brick.Type.NORMAL
        }
    }

    private interface Pattern {
        fun build(cols: Int, rows: Int, rng: Random): Array<BooleanArray>
    }

    private fun pickPattern(rng: Random): Pattern = PATTERNS.random(rng)

    private val PATTERNS: List<Pattern> = listOf(
        FullRowsPattern,
        CheckerboardPattern,
        PyramidPattern,
        ColumnsPattern,
        SparsePattern,
        DiamondPattern,
        FortressPattern,
    )

    private object FullRowsPattern : Pattern {
        override fun build(cols: Int, rows: Int, rng: Random): Array<BooleanArray> {
            val m = Array(rows) { BooleanArray(cols) { true } }
            for (r in 0 until rows) for (c in 0 until cols) {
                if (rng.nextFloat() < 0.08f) m[r][c] = false
            }
            return m
        }
    }

    private object CheckerboardPattern : Pattern {
        override fun build(cols: Int, rows: Int, rng: Random): Array<BooleanArray> {
            return Array(rows) { r -> BooleanArray(cols) { c -> (r + c) % 2 == 0 } }
        }
    }

    private object PyramidPattern : Pattern {
        override fun build(cols: Int, rows: Int, rng: Random): Array<BooleanArray> {
            val m = Array(rows) { BooleanArray(cols) }
            for (r in 0 until rows) {
                val margin = (r * cols / (rows * 2 + 1))
                for (c in margin until cols - margin) m[r][c] = true
            }
            return m
        }
    }

    private object ColumnsPattern : Pattern {
        override fun build(cols: Int, rows: Int, rng: Random): Array<BooleanArray> {
            val m = Array(rows) { BooleanArray(cols) }
            val step = 2 + rng.nextInt(2)  // 2 o 3
            for (c in 0 until cols) for (r in 0 until rows) {
                m[r][c] = c % step == 0
            }
            return m
        }
    }

    private object SparsePattern : Pattern {
        override fun build(cols: Int, rows: Int, rng: Random): Array<BooleanArray> {
            val density = 0.45f + rng.nextFloat() * 0.30f
            return Array(rows) { BooleanArray(cols) { rng.nextFloat() < density } }
        }
    }

    private object DiamondPattern : Pattern {
        override fun build(cols: Int, rows: Int, rng: Random): Array<BooleanArray> {
            val cx = cols / 2
            val cy = rows / 2
            val r0 = maxOf(cx, cy)
            return Array(rows) { r ->
                BooleanArray(cols) { c -> abs(c - cx) + abs(r - cy) <= r0 }
            }
        }
    }

    private object FortressPattern : Pattern {
        override fun build(cols: Int, rows: Int, rng: Random): Array<BooleanArray> {
            val m = Array(rows) { BooleanArray(cols) }
            for (r in 0 until rows) for (c in 0 until cols) {
                val onBorder = r == 0 || c == 0 || c == cols - 1
                val onInner = r in 2 until rows - 1 && c in 2 until cols - 2
                m[r][c] = onBorder || onInner
            }
            return m
        }
    }
}
