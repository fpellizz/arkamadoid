package com.arkamadoid.render

import com.arkamadoid.theme.Theme
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.Viewport

/**
 * Cornice "cabinato" double-line sempre visibile attorno allo schermo.
 * Si aspetta uno [ShapeRenderer] già allocato; gestisce begin/end internamente
 * sul viewport passato. Va chiamato per ULTIMO nel render delle screen.
 */
object BezelFrame {

    fun draw(shapes: ShapeRenderer, viewport: Viewport, w: Float, h: Float) {
        viewport.apply()
        shapes.projectionMatrix = viewport.camera.combined
        Gdx.gl.glLineWidth(3f)
        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = Theme.Palette.SECONDARY_CONTAINER
        shapes.rect(OUTER_INSET, OUTER_INSET, w - 2f * OUTER_INSET, h - 2f * OUTER_INSET)
        Gdx.gl.glLineWidth(1f)
        shapes.color = Theme.Palette.PRIMARY_CONTAINER
        shapes.rect(INNER_INSET, INNER_INSET, w - 2f * INNER_INSET, h - 2f * INNER_INSET)
        shapes.end()
    }

    private const val OUTER_INSET = 6f
    private const val INNER_INSET = 14f
}
