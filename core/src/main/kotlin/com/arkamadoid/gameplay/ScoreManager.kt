package com.arkamadoid.gameplay

import com.arkamadoid.config.GameConfig

class ScoreManager {
    private var lastExtraLifeAt = 0

    fun award(state: GameState, points: Int) {
        state.score += points
        while (state.score - lastExtraLifeAt >= GameConfig.EXTRA_LIFE_EVERY) {
            state.lives += 1
            lastExtraLifeAt += GameConfig.EXTRA_LIFE_EVERY
        }
    }

    fun reset() {
        lastExtraLifeAt = 0
    }
}
