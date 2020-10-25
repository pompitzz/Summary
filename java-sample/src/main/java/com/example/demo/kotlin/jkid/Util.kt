package com.example.demo.kotlin.jkid

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass

fun Type.isPrimitiveOrString(): Boolean {
    val clazz = this as? Class<*> ?: return false
    return clazz.kotlin.javaPrimitiveType != null || clazz == String::class.java
}

fun Type.asJavaClass(): Class<Any> = when(this) {
    is Class<*> -> this as Class<Any>
    is ParameterizedType -> rawType as? Class<Any>
            ?: throw UnsupportedOperationException("Unknown type $this")
    else -> throw UnsupportedOperationException("Unknown type $this")
}

internal fun<T : Any> KClass<T>.createInstance(): T {
    val noArgConstructor = constructors.find { it.parameters.isEmpty() }
    return noArgConstructor?.call() ?: throw IllegalArgumentException("Class must have a no-argument constructor")
}
