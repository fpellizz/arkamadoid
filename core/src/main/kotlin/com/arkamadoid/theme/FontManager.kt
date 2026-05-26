package com.arkamadoid.theme

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.utils.Disposable

class FontManager : Disposable {

    private val regularGenLazy = lazy {
        FreeTypeFontGenerator(Gdx.files.internal("fonts/SpaceMono-Regular.ttf"))
    }
    private val boldGenLazy = lazy {
        FreeTypeFontGenerator(Gdx.files.internal("fonts/SpaceMono-Bold.ttf"))
    }

    private data class Key(val size: Theme.FontSize, val bold: Boolean)

    private val cache = mutableMapOf<Key, BitmapFont>()

    operator fun get(size: Theme.FontSize, bold: Boolean = false): BitmapFont =
        cache.getOrPut(Key(size, bold)) { build(size, bold) }

    private fun build(size: Theme.FontSize, bold: Boolean): BitmapFont {
        val gen = if (bold) boldGenLazy.value else regularGenLazy.value
        val param = FreeTypeFontParameter().apply {
            this.size = size.px
            this.color = Color.WHITE
            this.hinting = FreeTypeFontGenerator.Hinting.None
            this.kerning = false
            this.borderStraight = true
            this.mono = size.isPixelTier
            this.minFilter = if (size.isPixelTier) Texture.TextureFilter.Nearest else Texture.TextureFilter.Linear
            this.magFilter = Texture.TextureFilter.Nearest
            this.characters = ASCII_PRINTABLE + EXTRA_GLYPHS
        }
        val font = gen.generateFont(param)
        font.setUseIntegerPositions(true)
        return font
    }

    override fun dispose() {
        cache.values.forEach { it.dispose() }
        cache.clear()
        if (regularGenLazy.isInitialized()) regularGenLazy.value.dispose()
        if (boldGenLazy.isInitialized()) boldGenLazy.value.dispose()
    }

    companion object {
        private const val ASCII_PRINTABLE =
            " !\"#\$%&'()*+,-./0123456789:;<=>?@" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
        private const val EXTRA_GLYPHS = "àèéìòùÀÈÉÌÒÙ°©"
    }
}
