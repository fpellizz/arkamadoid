package com.arkamadoid.input

import com.arkamadoid.entities.Paddle

class TouchController(
    var mode: InputMode = InputMode.DRAG_OFFSET,
    var sensitivity: Float = 1f,
) {
    fun update(paddle: Paddle, deltaX: Float, touchX: Float, virtualWidth: Float) {
        when (mode) {
            InputMode.DRAG_OFFSET -> paddle.x =
                (paddle.x + deltaX * sensitivity).coerceIn(0f, virtualWidth - paddle.width)
            InputMode.TOUCH_ABSOLUTE -> paddle.x =
                (touchX - paddle.width / 2f).coerceIn(0f, virtualWidth - paddle.width)
            InputMode.GAMEPAD -> Unit
        }
    }
}
