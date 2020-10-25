package com.example.demo.kotlin.jkid.serialize

import java.lang.reflect.Type

interface ValueSerializer<T> {
    fun toJsonValue(value: T): Any?
    fun fromJsonValue(jsonValue: Any?): T
}

fun serializerForType(type: Type): ValueSerializer<out Any?> = TODO()
