package com.arkamadoid.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable

class CrtRenderer : Disposable {

    private val shader: ShaderProgram
    private val batch: SpriteBatch
    private val projection = Matrix4()
    private var fbo: FrameBuffer? = null

    init {
        ShaderProgram.pedantic = false
        val vert = Gdx.files.internal("shaders/crt.vert").readString()
        val frag = Gdx.files.internal("shaders/crt.frag").readString()
        shader = ShaderProgram(vert, frag)
        check(shader.isCompiled) { "CRT shader compile failed: ${shader.log}" }
        batch = SpriteBatch(1, shader)
    }

    fun resize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val current = fbo
        if (current != null && current.width == width && current.height == height) return
        current?.dispose()
        fbo = FrameBuffer(Pixmap.Format.RGB888, width, height, false).also {
            it.colorBufferTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
        projection.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())
    }

    fun beginCapture() {
        fbo?.begin()
    }

    fun endCaptureAndDraw() {
        val f = fbo ?: return
        f.end()
        val w = f.width
        val h = f.height
        batch.projectionMatrix = projection
        batch.shader = shader
        batch.begin()
        shader.setUniformf("u_resolution", w.toFloat(), h.toFloat())
        batch.draw(f.colorBufferTexture, 0f, 0f, w.toFloat(), h.toFloat(), 0, 0, w, h, false, true)
        batch.end()
    }

    override fun dispose() {
        shader.dispose()
        batch.dispose()
        fbo?.dispose()
        fbo = null
    }
}
