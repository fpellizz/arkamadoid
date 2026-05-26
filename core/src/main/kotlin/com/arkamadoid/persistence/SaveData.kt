package com.arkamadoid.persistence

import com.arkamadoid.input.InputMode

data class SaveData(
    val highScores: MutableList<HighScoreEntry> = mutableListOf(),
    var unlockedLevel: Int = 1,
    var unlockedSkins: MutableSet<String> = mutableSetOf("default"),
    var musicVolume: Float = 0.7f,
    var sfxVolume: Float = 0.9f,
    var inputMode: InputMode = InputMode.DRAG_OFFSET,
    var sensitivity: Float = 1.0f,
    var crtShader: Boolean = true,
    var reduceMotion: Boolean = false,
    var haptics: Boolean = true,
    var language: String = "it",
) {
    data class HighScoreEntry(val initials: String, val score: Int, val level: Int)
}
