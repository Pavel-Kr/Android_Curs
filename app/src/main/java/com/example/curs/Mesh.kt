package com.example.curs

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Mesh(
    private val material: Material
) {
    private var vertexCoords = ArrayList<Float>()
    private var textureCoords = ArrayList<Float>()
    private var normalCoords = ArrayList<Float>()

    private var vertexBuffer: FloatBuffer? = null
    private var normalBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null

    private var vertexCount = 0

    fun addVertex(x: Float, y: Float, z: Float){
        vertexCoords.add(x)
        vertexCoords.add(y)
        vertexCoords.add(z)
        vertexCount++
    }

    fun addTexCoord(x: Float, y: Float){
        textureCoords.add(x)
        textureCoords.add(y)
    }

    fun addNormal(x: Float, y: Float, z: Float){
        normalCoords.add(x)
        normalCoords.add(y)
        normalCoords.add(z)
    }

    fun allocateBuffers(){
        vertexBuffer =
            ByteBuffer.allocateDirect(vertexCoords.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(vertexCoords.toFloatArray())
                    position(0)
                }
            }

        normalBuffer =
            ByteBuffer.allocateDirect(normalCoords.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(normalCoords.toFloatArray())
                    position(0)
                }
            }

        textureBuffer =
            ByteBuffer.allocateDirect(textureCoords.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(textureCoords.toFloatArray())
                    position(0)
                }
            }
    }

    fun draw(MVPMatrix: FloatArray?, normalMatrix: FloatArray, modelMatrix: FloatArray, eyePos: FloatArray, lightPos: FloatArray, shaderProgram: Int){
        if (material.hasTexture){
            val textureHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCord")
            val textureLocation = GLES20.glGetUniformLocation(shaderProgram, "u_Texture")
            val hasTextureLocation = GLES20.glGetUniformLocation(shaderProgram, "u_hasTexture")
            GLES20.glEnableVertexAttribArray(textureHandle)
            GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, material.textureID)
            GLES20.glUniform1i(textureLocation, 0)
            GLES20.glUniform1f(hasTextureLocation, 1f)
        }
        else{
            val textureHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCord")
            val colorLocation = GLES20.glGetUniformLocation(shaderProgram, "u_Color")
            val hasTextureLocation = GLES20.glGetUniformLocation(shaderProgram, "u_hasTexture")
            GLES20.glEnableVertexAttribArray(textureHandle)
            GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
            GLES20.glUniform4fv(colorLocation, 1, material.diffuseColor, 0)
            GLES20.glUniform1f(hasTextureLocation, 0f)
        }

        //Определяем заголовки для последующего связывания переменных с шейдерами
        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "a_Position")
        val normalHandle = GLES20.glGetAttribLocation(shaderProgram, "a_Normal")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "u_MVPMatrix")
        val modelMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "u_ModelMatrix")
        val normalMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "u_NormalMatrix")
        val eyeHandle = GLES20.glGetUniformLocation(shaderProgram, "u_EyePos")
        val lightHandle = GLES20.glGetUniformLocation(shaderProgram, "u_LightPos")

        //РџРµСЂРµРґР°С‘Рј РїРѕР·РёС†РёСЋ РєР°РјРµСЂС‹
        GLES20.glUniform3f(eyeHandle, eyePos[0], eyePos[1], eyePos[2])

        //РџРµСЂРµРґР°С‘Рј РїРѕР·РёС†РёСЋ РёСЃС‚РѕС‡РЅРёРєР° СЃРІРµС‚Р°
        GLES20.glUniform3fv(lightHandle, 1, lightPos, 0)

        //РџРµСЂРµРґР°С‘Рј Р±СѓС„С„РµСЂ РІРµСЂС€РёРЅ
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        //РџРµСЂРµРґР°С‘Рј Р±СѓС„С„РµСЂ РЅРѕСЂРјР°Р»РµР№
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer)

        //РџРµСЂРµРґР°С‘Рј РјР°С‚СЂРёС†Сѓ РїСЂРѕРµРєС†РёРё
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, MVPMatrix, 0)
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(normalMatrixHandle, 1, false, normalMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
    }
}