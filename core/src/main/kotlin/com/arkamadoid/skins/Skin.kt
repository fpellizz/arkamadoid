package com.arkamadoid.skins

import com.arkamadoid.achievements.Achievement
import com.arkamadoid.theme.Theme
import com.badlogic.gdx.graphics.Color

/**
 * Skin del paddle (capsule color). DEFAULT è sempre disponibile, gli altri
 * si sbloccano via [unlockAchievement] (auto-aggiunti a unlockedSkins in
 * PreferencesStore.unlockAchievement).
 */
enum class PaddleSkin(
    val id: String,
    val color: Color,
    val unlockAchievement: Achievement?,
) {
    DEFAULT("paddle_default", Theme.Palette.SECONDARY_CONTAINER, null),
    MAGENTA("paddle_magenta", Theme.Palette.PRIMARY_CONTAINER, Achievement.FIRST_BRICK),
    YELLOW("paddle_yellow", Theme.Palette.TERTIARY, Achievement.BOSS_FIRST),
    GREEN("paddle_green", Theme.Palette.NEON_GREEN, Achievement.CENTURION),
}

/**
 * Skin della palla (core + alone color).
 */
enum class BallSkin(
    val id: String,
    val core: Color,
    val halo: Color,
    val unlockAchievement: Achievement?,
) {
    DEFAULT("ball_default", Color.WHITE, Theme.Palette.PRIMARY, null),
    YELLOW("ball_yellow", Theme.Palette.NEON_YELLOW, Theme.Palette.PRIMARY, Achievement.COMBO_X4),
    GREEN("ball_green", Theme.Palette.NEON_GREEN, Theme.Palette.NEON_CYAN, Achievement.ENDLESS_30),
}

/**
 * Palette dei brick (6 colori indicizzati per riga del livello).
 */
enum class PaletteSkin(
    val id: String,
    val colors: Array<Color>,
    val unlockAchievement: Achievement?,
) {
    DEFAULT(
        "palette_default",
        arrayOf(
            Theme.Palette.SECONDARY_FIXED_DIM,
            Theme.Palette.PRIMARY_CONTAINER,
            Theme.Palette.TERTIARY,
            Theme.Palette.SECONDARY_FIXED,
            Theme.Palette.PRIMARY_FIXED,
            Theme.Palette.ERROR,
        ),
        null,
    ),
    HOT(
        "palette_hot",
        arrayOf(
            Color.valueOf("ffb04a"),
            Color.valueOf("ff7800"),
            Theme.Palette.NEON_YELLOW,
            Color.valueOf("ff5500"),
            Color.valueOf("ff2400"),
            Color.valueOf("bd1f00"),
        ),
        Achievement.BOSS_FINAL,
    ),
}
