package com.arkamadoid.achievements

/**
 * Achievement set v1. Gli `id` sono stabili e usati anche come chiave GPGS
 * (mapparli sui veri achievement_id del Play Games Console quando configurato).
 * Titolo/descrizione visibili sono via I18n con chiave "achievement.{id}.title|desc".
 */
enum class Achievement(val id: String, val hidden: Boolean = false) {
    FIRST_BRICK("first_brick"),
    COMBO_X2("combo_x2"),
    COMBO_X4("combo_x4"),
    BOSS_FIRST("boss_first"),
    BOSS_FINAL("boss_final", hidden = true),
    PIXEL_PERFECT("pixel_perfect"),
    NO_POWER("no_power"),
    ENDLESS_30("endless_30"),
    CENTURION("centurion"),
    DAILY_3("daily_3"),
    DAILY_7("daily_7"),
}
