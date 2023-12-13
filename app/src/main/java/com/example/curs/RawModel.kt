package com.example.curs

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class RawModel(
        context: Context,
        modelFileId: Int,
        materialFileId: Int,
        translate: FloatArray,
        scale: Float,
        var smooth: Boolean) {

    private var indexCount = 0
    private var shaderProgram = 0
    var color = FloatArray(4)
    private val MVPMatrix = FloatArray(16)
    private var rotationMatrix = FloatArray(16)
    private var translationMatrix = FloatArray(16)
    private var scaleMatrix = FloatArray(16)
    private var modelMatrix = FloatArray(16)
    private val shaderHandler = ShaderHandler()
    private val textureHandler = TextureHandler()
    private lateinit var materials: ArrayList<Material>
    private lateinit var meshes: ArrayList<Mesh>

    init {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(translationMatrix, 0)
        Matrix.setIdentityM(rotationMatrix, 0)
        Matrix.setIdentityM(scaleMatrix, 0)
        Matrix.translateM(translationMatrix, 0, translate[0], translate[1], translate[2])
        Matrix.scaleM(scaleMatrix, 0, scale, scale, scale)
        readMTLFile(context, materialFileId)
        readOBJFile(context, modelFileId)
        shaderProgram = shaderHandler.loadShaders(context, R.raw.vertex, R.raw.fragment)
        Log.d("COLOR", color[0].toString())
        Log.d("COLOR", color[1].toString())
        Log.d("COLOR", color[2].toString())
        Log.d("COLOR", color[3].toString())
    }

    fun draw(viewMatrix: FloatArray?, projectionMatrix: FloatArray, eyePos: FloatArray, lightPos: FloatArray) {
        val tmp = FloatArray(16)
        Matrix.multiplyMM(tmp, 0, rotationMatrix, 0, translationMatrix, 0)
        Matrix.multiplyMM(modelMatrix, 0, scaleMatrix, 0, tmp, 0)
        val VPMatrix = FloatArray(16)
        Matrix.multiplyMM(VPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(MVPMatrix, 0, VPMatrix, 0, modelMatrix, 0)

        val normalMatrix = FloatArray(16)
        val inverted = FloatArray(16)
        Matrix.invertM(inverted, 0, modelMatrix, 0)
        Matrix.transposeM(normalMatrix, 0, inverted, 0)

        GLES20.glUseProgram(shaderProgram)
        for(mesh in meshes){
            mesh.draw(MVPMatrix, normalMatrix, modelMatrix, eyePos, lightPos, shaderProgram)
        }
    }

    fun rotate(angle: Float, x: Float, y: Float, z: Float){
        Matrix.rotateM(rotationMatrix, 0, angle, x, y, z)
    }

    private fun readMTLFile(context: Context, mtlFileId: Int) {
        var reader: BufferedReader? = null
        materials = ArrayList()
        var currentMaterial = Material(name = "")
        try {
            reader = BufferedReader(
                InputStreamReader(context.resources.openRawResource(mtlFileId), "UTF-8")
            )
            var mLine: String?
            while (reader.readLine().also { mLine = it } != null) {
                if (mLine?.isEmpty() == true){
                    continue
                }
                val parts = mLine?.split(" ".toRegex())?.dropLastWhile { it.isEmpty() }
                    ?.toTypedArray()
                when (parts?.get(0)?.trim { it <= ' ' }) {
                    "newmtl" -> {
                        currentMaterial = Material(name = parts[1])
                        materials.add(currentMaterial)
                    }

                    "map_Kd" -> {
                        currentMaterial.hasTexture = true
                        val textureParts = parts[1].split(".").dropLastWhile { it.isEmpty() }.toTypedArray()
                        val textureName = textureParts[0]
                        val drawableId = context.resources.getIdentifier(textureName, "drawable", context.packageName)
                        currentMaterial.textureID = textureHandler.loadTexture(context, drawableId)
                    }

                    "Kd" -> {
                        currentMaterial.hasTexture = false
                        var i = 1
                        while (i < parts.size) {
                            val color = java.lang.Float.valueOf(parts[i].trim { it <= ' ' })
                            currentMaterial.diffuseColor[i - 1] = color
                            i++
                        }
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    private fun readOBJFile(context: Context, modelFileId: Int) {
        val verticies = ArrayList<Float>()
        val normals = ArrayList<Float>()
        val textureCords = ArrayList<FloatArray>()
        val indices = ArrayList<Short>()
        val normalIndices = ArrayList<Short>()
        val textureIndices = ArrayList<Short>()
        val materialIndices = ArrayList<Int>()
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(
                InputStreamReader(context.resources.openRawResource(modelFileId), "UTF-8")
            )
            var mLine: String?
            var currentMaterialIndex = 0
            while (reader.readLine().also { mLine = it } != null) {
                val parts = mLine?.split(" ".toRegex())?.dropLastWhile { it.isEmpty() }
                    ?.toTypedArray()
                when (parts?.get(0)?.trim { it <= ' ' }) {
                    "v" -> {
                        var i = 1
                        while (i < parts.size) {
                            val vertex = java.lang.Float.valueOf(parts[i].trim { it <= ' ' })
                            verticies.add(vertex)
                            i++
                        }
                    }

                    "vn" -> {
                        var i = 1
                        while (i < parts.size) {
                            val normal = java.lang.Float.valueOf(parts[i].trim { it <= ' ' })
                            normals.add(normal)
                            i++
                        }
                    }

                    "vt" -> {
                        val texArr = FloatArray(2)
                        texArr[0] = java.lang.Float.valueOf(parts[1].trim { it <= ' ' })
                        texArr[1] = java.lang.Float.valueOf(parts[2].trim { it <= ' ' })
                        textureCords.add(texArr)
                    }

                    "f" -> {
                        //Считывание индексов вершин
                        var faceParts =
                            parts[1].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        var index = faceParts[0].trim { it <= ' ' }.toShort()
                        indices.add((index - 1).toShort())
                        faceParts = parts[2].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        index = faceParts[0].trim { it <= ' ' }.toShort()
                        indices.add((index - 1).toShort())
                        faceParts = parts[3].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        index = faceParts[0].trim { it <= ' ' }.toShort()
                        indices.add((index - 1).toShort())
                        faceParts = parts[1].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        index = faceParts[1].trim { it <= ' ' }.toShort()
                        textureIndices.add((index - 1).toShort())
                        faceParts = parts[2].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        index = faceParts[1].trim { it <= ' ' }.toShort()
                        textureIndices.add((index - 1).toShort())
                        faceParts = parts[3].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        index = faceParts[1].trim { it <= ' ' }.toShort()
                        textureIndices.add((index - 1).toShort())
                        faceParts = parts[1].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        index = faceParts[2].trim { it <= ' ' }.toShort()
                        normalIndices.add((index - 1).toShort())
                        faceParts = parts[2].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        index = faceParts[2].trim { it <= ' ' }.toShort()
                        normalIndices.add((index - 1).toShort())
                        faceParts = parts[3].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        index = faceParts[2].trim { it <= ' ' }.toShort()
                        normalIndices.add((index - 1).toShort())

                        materialIndices.add(currentMaterialIndex)
                        materialIndices.add(currentMaterialIndex)
                        materialIndices.add(currentMaterialIndex)
                    }
                    "usemtl" -> {
                        val name = parts[1]
                        var i = 0
                        while (i < materials.size) {
                            if (materials[i].name == name) {
                                currentMaterialIndex = i
                                break
                            }
                            i++
                        }
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        }
        val indexArray = ShortArray(indices.size)
        val normalValue = FloatArray(verticies.size)
        val normalCount = FloatArray(verticies.size)
        for (i in verticies.indices) {
            normalValue[i] = 0f
            normalCount[i] = 0f
        }

        meshes = ArrayList()
        for (mat in materials){
            val mesh = Mesh(mat)
            meshes.add(mesh)
        }

        //Заполнение массивов значениями из списков
        for (i in indices.indices) {
            val index = indices[i].toInt()
            val textureIndex = textureIndices[i].toInt()
            val normalIndex = normalIndices[i].toInt()
            val materialIndex = materialIndices[i]
            meshes[materialIndex].addVertex(verticies[index * 3], verticies[index * 3 + 1], verticies[index * 3 + 2])
            if (!smooth) {
                meshes[materialIndex].addNormal(normals[normalIndex * 3], normals[normalIndex * 3 + 1], normals[normalIndex * 3 + 2])
            } else {
                normalValue[index * 3] += normals[normalIndex * 3]
                normalValue[index * 3 + 1] += normals[normalIndex * 3 + 1]
                normalValue[index * 3 + 2] += normals[normalIndex * 3 + 2]
                normalCount[index * 3] += 1f
                normalCount[index * 3 + 1] += 1f
                normalCount[index * 3 + 2] += 1f
            }
            meshes[materialIndex].addTexCoord(textureCords[textureIndex][0], textureCords[textureIndex][1])
            indexArray[i] = i.toShort()
        }

        //РЎСЂРµРґРЅРµРµ Р·РЅР°С‡РµРЅРёРµ РЅРѕСЂРјР°Р»Рё (СЃРіР»Р°Р¶РёРІР°РЅРёРµ С‚СЂРµСѓРіРѕР»СЊРЅРёРєРѕРІ)
        if (smooth) {
            for (i in indices.indices) {
                val index = indices[i].toInt()
                val materialIndex = materialIndices[i]
                meshes[materialIndex].addNormal(normalValue[index * 3] / normalCount[index * 3],
                    normalValue[index * 3 + 1] / normalCount[index * 3 + 1], normalValue[index * 3 + 2] / normalCount[index * 3 + 2])
            }
        }

        for (mesh in meshes){
            mesh.allocateBuffers()
        }

        indexCount = indices.size
    }
}