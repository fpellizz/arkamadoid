package com.arkamadoid.theme

import com.badlogic.gdx.graphics.Color

/**
 * Token estratti da assets/_mockups/neon_arcade/DESIGN.md.
 * Singola sorgente di verità per palette, spacing e tipografia.
 */
object Theme {

    object Palette {
        // Brand neon
        val NEON_MAGENTA: Color = Color.valueOf("ff00e5")
        val NEON_CYAN: Color = Color.valueOf("00eefc")
        val NEON_YELLOW: Color = Color.valueOf("dbc900")

        // M3 surface (dark)
        val SURFACE: Color = Color.valueOf("131319")
        val SURFACE_DIM: Color = Color.valueOf("131319")
        val SURFACE_BRIGHT: Color = Color.valueOf("393840")
        val SURFACE_CONTAINER_LOWEST: Color = Color.valueOf("0e0e14")
        val SURFACE_CONTAINER_LOW: Color = Color.valueOf("1b1b22")
        val SURFACE_CONTAINER: Color = Color.valueOf("1f1f26")
        val SURFACE_CONTAINER_HIGH: Color = Color.valueOf("2a2930")
        val SURFACE_CONTAINER_HIGHEST: Color = Color.valueOf("35343b")

        // Primary (magenta family)
        val PRIMARY: Color = Color.valueOf("fface8")
        val ON_PRIMARY: Color = Color.valueOf("5e0053")
        val PRIMARY_CONTAINER: Color = Color.valueOf("ff24e4")
        val ON_PRIMARY_CONTAINER: Color = Color.valueOf("520049")
        val PRIMARY_FIXED: Color = Color.valueOf("ffd7f0")
        val PRIMARY_FIXED_DIM: Color = Color.valueOf("fface8")

        // Secondary (cyan family)
        val SECONDARY: Color = Color.valueOf("d3fbff")
        val ON_SECONDARY: Color = Color.valueOf("00363a")
        val SECONDARY_CONTAINER: Color = Color.valueOf("00eefc")
        val SECONDARY_FIXED: Color = Color.valueOf("7df4ff")
        val SECONDARY_FIXED_DIM: Color = Color.valueOf("00dbe9")

        // Tertiary (yellow family)
        val TERTIARY: Color = Color.valueOf("dbc900")
        val ON_TERTIARY: Color = Color.valueOf("363100")
        val TERTIARY_CONTAINER: Color = Color.valueOf("bdad00")
        val TERTIARY_FIXED: Color = Color.valueOf("fae500")

        // Error
        val ERROR: Color = Color.valueOf("ffb4ab")
        val ERROR_CONTAINER: Color = Color.valueOf("93000a")

        // On/outline
        val ON_SURFACE: Color = Color.valueOf("e4e1ea")
        val ON_SURFACE_VARIANT: Color = Color.valueOf("ddbed1")
        val OUTLINE: Color = Color.valueOf("a5889b")
        val OUTLINE_VARIANT: Color = Color.valueOf("574050")
    }

    /** Spacing in pixel virtuali del canvas 240x320. */
    object Spacing {
        const val UNIT = 1          // ≈ 8px @ schermo full → 1 in canvas 240x320
        const val GUTTER = 2        // ≈ 16px
        const val MARGIN = 2        // mobile margin
        const val BEZEL_MOBILE = 2  // bezel sottile sempre visibile (decisione 2026-05-26)
        const val BEZEL_DESKTOP = 5
    }

    /**
     * Font size in pixel REALI (matchano il DESIGN.md di Stitch).
     * Vengono generati da FreeType e usati nel viewport UI (non nel game viewport 240x320).
     */
    enum class FontSize(val px: Int) {
        LABEL_SM(12),
        BODY_MD(16),
        HEADLINE_MOBILE(24),
        HEADLINE(32),
        DISPLAY(48);

        /** Soglia sotto la quale forziamo nearest-filter + mono per look pixel-art. */
        val isPixelTier: Boolean get() = px <= 16
    }

    /** Durate animazioni cardine (secondi). */
    object Anim {
        const val FLICKER_PERIOD = 0.15f
        const val BLINK_PERIOD = 1.0f
        const val BLINK_EXTREME_PERIOD = 1.5f
        const val CHROMATIC_PERIOD = 2.0f
        const val SCANLINE_SWEEP_PERIOD = 4.0f
        const val PULSE_ACTIVE_PERIOD = 1.5f
    }

    /** Intensità glow (raggi blur multi-layer in pixel virtuali). */
    object Glow {
        val SMALL = intArrayOf(2, 6)
        val MEDIUM = intArrayOf(2, 6, 10)
        val LARGE = intArrayOf(4, 12, 20)
    }
}
