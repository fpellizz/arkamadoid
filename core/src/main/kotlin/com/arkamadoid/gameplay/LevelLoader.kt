package com.arkamadoid.gameplay

import com.arkamadoid.config.GameConfig
import com.arkamadoid.entities.Boss
import com.arkamadoid.entities.Brick
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.JsonReader

object LevelLoader {
    fun load(index: Int): Level {
        val file = Gdx.files.internal("levels/%02d.json".format(index))
        val root = JsonReader().parse(file)

        val name = root.getString("name", "SECTOR $index")
        val ballSpeed = root.getFloat("ballSpeed", GameConfig.BALL_INITIAL_SPEED)

        val grid = root.get("grid")
        val cellWidth = grid.getFloat("cellWidth")
        val cellHeight = grid.getFloat("cellHeight")
        val originX = grid.getFloat("originX")
        val originY = grid.getFloat("originY")

        val typeByChar = HashMap<Char, Brick.Type>()
        val colorByChar = HashMap<Char, Int>()
        var nextColor = 0
        for (entry in root.get("legend")) {
            val ch = entry.name[0]
            val typeName = entry.asString()
            if (typeName == "EMPTY") continue
            typeByChar[ch] = Brick.Type.valueOf(typeName)
            colorByChar[ch] = nextColor++
        }

        val bricks = mutableListOf<Brick>()
        var rowIndex = 0
        for (row in root.get("rows")) {
            val str = row.asString()
            for ((col, ch) in str.withIndex()) {
                val type = typeByChar[ch] ?: continue
                bricks += Brick(
                    x = originX + col * cellWidth,
                    y = originY - (rowIndex + 1) * cellHeight,
                    width = cellWidth,
                    height = cellHeight,
                    type = type,
                    colorIndex = colorByChar[ch] ?: 0,
                )
            }
            rowIndex++
        }

        val level = Level(index, name, bricks, ballSpeed)
        if (root.has("boss")) {
            val b = root.get("boss")
            val boss = Boss(
                x = b.getFloat("x"),
                y = b.getFloat("y"),
                width = b.getFloat("width"),
                height = b.getFloat("height"),
                maxHp = b.getInt("hp"),
                oscillationSpeed = b.getFloat("oscillationSpeed", 60f),
                oscillationRange = b.getFloat("oscillationRange", 80f),
            )
            boss.anchorX = boss.x
            level.boss = boss
        }
        return level
    }
}
