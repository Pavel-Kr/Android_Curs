package com.example.curs

import android.content.Context
import android.opengl.GLES20
import java.io.BufferedReader

class ShaderHandler {
    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0
    private var shaderProgram: Int = 0

    fun loadShaders(context: Context, vertexSrcId: Int, fragmentSrcId: Int): Int {
        println("Vertex shader")
        val vertexSrc = BufferedReader(context.resources.openRawResource(vertexSrcId).reader()).readText()
        vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSrc)
        println("Fragment shader")
        val fragmentSrc = BufferedReader(context.resources.openRawResource(fragmentSrcId).reader()).readText()
        fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSrc)
        shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)
        println(GLES20.glGetProgramInfoLog(shaderProgram))
        return shaderProgram
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        println(GLES20.glGetShaderInfoLog(shader))
        return shader
    }
}