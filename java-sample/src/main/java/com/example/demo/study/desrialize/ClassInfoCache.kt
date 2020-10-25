package com.example.demo.study.desrialize

import com.example.demo.kotlin.jkid.serialize.ValueSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class ClassInfoCache {
    private val classInfoCache = mutableMapOf<KClass<*>, ClassInfo<*>>()

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(clazz: KClass<T>): ClassInfo<T> {
        return classInfoCache.getOrPut(clazz) { ClassInfo(clazz) } as ClassInfo<T>
    }
}

class ClassInfo<T : Any>(clazz: KClass<T>) {
    private val name = clazz.qualifiedName
    private val constructor = clazz.primaryConstructor ?:
            throw IllegalArgumentException()
    private val propertyNameToParamMap = mutableMapOf<String, KParameter>()
    private val paramToSerializerMap = mutableMapOf<KParameter, ValueSerializer<*>>()
}
