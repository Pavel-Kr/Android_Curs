package com.example.curs

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private val vpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private lateinit var table: RawModel

    private val eyePos = floatArrayOf(0f, 5f, -10f)
    private val lightPos = floatArrayOf(0f, 10f, -20f, 1f)

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        table = RawModel(context, R.raw.stol, R.raw.stol_mat, floatArrayOf(0f, 0f, 0f), 1f, false)
        //table.rotate(90f, 0f, 1f, 0f)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f)
        Matrix.setLookAtM(viewMatrix, 0, eyePos[0], eyePos[1], eyePos[2], 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

        val t = 1000 * 5
        val time = (SystemClock.uptimeMillis() % t).toFloat() / t
        val angle: Double = time * 2 * 3.1415926

        /*eyePos[0] = (Math.cos(angle) * 15f).toFloat()
        eyePos[1] = 5f
        eyePos[2] = (Math.sin(angle) * 15f).toFloat()*/

        Matrix.setLookAtM(viewMatrix, 0, eyePos[0], eyePos[1], eyePos[2], 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        table.rotate(0.5f, 0f, 1f, 0f)
        table.draw(viewMatrix, projectionMatrix, eyePos, lightPos)
        //sphere.draw(vpMatrix)
    }
}