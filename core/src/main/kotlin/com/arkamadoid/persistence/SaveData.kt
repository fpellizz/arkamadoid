package com.arkamadoid.persistence

import com.arkamadoid.input.InputMode

data class SaveData(
    val highScores: MutableList<HighScoreEntry> = mutableListOf(),
    var unlockedLevel: Int = 1,
    var unlockedSkins: MutableSet<String> = mutableSetOf(),
    var selectedPaddleSkin: String = "paddle_default",
    var selectedBallSkin: String = "ball_default",
    var selectedPaletteSkin: String = "palette_default",
    var unlockedAchievements: MutableSet<String> = mutableSetOf(),
    var musicVolume: Float = 0.7f,
    var sfxVolume: Float = 0.9f,
    var inputMode: InputMode = InputMode.DRAG_OFFSET,
    var sensitivity: Float = 1.0f,
    var crtShader: Boolean = true,
    var reduceMotion: Boolean = false,
    var haptics: Boolean = true,
    var language: String = "auto",
    var dailyDate: String = "",
    var dailyBestScore: Int = 0,
    var dailyStreak: Int = 0,
    var dailyStreakDate: String = "",
) {
    data class HighScoreEntry(
        val initials: String = "AAA",
        val score: Int = 0,
        val level: Int = 1,
        val mode: String = "ARCADE",
    )
}
