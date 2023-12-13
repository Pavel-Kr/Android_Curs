package com.example.curs

class Material(
    var name: String,
    var hasTexture: Boolean = false,
    var textureID: Int = 0,
    var diffuseColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)
)