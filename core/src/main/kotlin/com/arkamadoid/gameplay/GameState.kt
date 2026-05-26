package com.arkamadoid.gameplay

import com.arkamadoid.config.GameConfig
import com.arkamadoid.entities.Ball
import com.arkamadoid.entities.Paddle
import com.arkamadoid.entities.PowerUp

class GameState {
    var lives: Int = GameConfig.START_LIVES
    var score: Int = 0
    var levelIndex: Int = 1
    var combo: Int = 0

    val paddle: Paddle = Paddle()
    val balls: MutableList<Ball> = mutableListOf(Ball())
    val activePowerUps: MutableList<PowerUp> = mutableListOf()

    var currentLevel: Level? = null
    var paused: Boolean = false
}
