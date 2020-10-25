package com.example.demo.study.desrialize

interface JsonObject {
    fun setSimpleProperty(propertyName: String, value: Any?)
    fun createJsonObject(propertyName: String): JsonObject
    fun createJsonArray(propertyName: String): JsonObject
}
