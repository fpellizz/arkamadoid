package com.arkamadoid.render

import com.arkamadoid.config.GameConfig
import com.badlogic.gdx.utils.viewport.FitViewport

class PixelViewport : FitViewport(
    GameConfig.VIRTUAL_WIDTH.toFloat(),
    GameConfig.VIRTUAL_HEIGHT.toFloat(),
)
