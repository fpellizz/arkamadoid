package com.arkamadoid.gameplay

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.JsonReader

object LevelLoader {
    fun load(index: Int): Level {
        val file = Gdx.files.internal("levels/%02d.json".format(index))
        val root = JsonReader().parse(file)
        // TODO: parse grid into bricks once asset format is finalized
        return Level(index, root.getString("name", "LEVEL $index"), mutableListOf(), 0f)
    }
}
