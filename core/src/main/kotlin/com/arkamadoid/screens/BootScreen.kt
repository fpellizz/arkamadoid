package com.arkamadoid.screens

import com.arkamadoid.ArkamadoidGame

class BootScreen(game: ArkamadoidGame) : BaseScreen(game) {
    override fun show() {
        // TODO: load atlases, fonts, shaders, then transition
        game.screen = AttractScreen(game)
    }
}
