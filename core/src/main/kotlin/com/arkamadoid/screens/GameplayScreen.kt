package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.gameplay.GameState

class GameplayScreen(
    game: ArkamadoidGame,
    private val mode: GameMode,
) : BaseScreen(game) {
    private val state = GameState()

    enum class GameMode { ARCADE, ENDLESS, DAILY, PRACTICE }
}
