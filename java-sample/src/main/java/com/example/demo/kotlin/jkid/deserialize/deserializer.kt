package com.example.demo.kotlin.jkid.deserialize

import com.example.demo.kotlin.jkid.asJavaClass
import com.example.demo.kotlin.jkid.isPrimitiveOrString
import com.example.demo.kotlin.jkid.serialize.serializerForType
import java.io.Reader
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

fun <T : Any> deserialize(json: Reader, targetClass: KClass<T>): T {
    val seed = ObjectSeed(targetClass, ClassInfoCache())
//    Parser(json, seed).parse()
    return seed.spawn()
}

interface JsonObject {
    fun setSimpleProperty(propertyName: String, value: Any?)

    fun createObject(propertyName: String): JsonObject

    fun createArray(propertyName: String): JsonObject
}

interface Seed : JsonObject {
    val classInfoCache: ClassInfoCache

    fun spawn(): Any?

    fun createCompositeProperty(propertyName: String, isList: Boolean): JsonObject

    override fun createObject(propertyName: String) = createCompositeProperty(propertyName, false)

    override fun createArray(propertyName: String) = createCompositeProperty(propertyName, true)
}

fun Seed.createSeedForType(paramType: Type, isList: Boolean): Seed {
    val paramClass = paramType.asJavaClass()

    if (List::class.java.isAssignableFrom(paramClass)) {
        if (!isList) throw JKidException("An array expected, not a composite object")
        val parameterizedType = paramType as? ParameterizedType
                ?: throw UnsupportedOperationException("Unsupported parameter type $this")
        val elementType: Type = parameterizedType.actualTypeArguments.single()
        if (elementType.isPrimitiveOrString()) {
            return ValueListSeed(elementType, classInfoCache)
        }
        return ObjectListSeed(elementType, classInfoCache)
    }

    if (isList) throw JKidException("Object of the type ${paramType.typeName} expected, not an array")
    return ObjectSeed(paramClass.kotlin, classInfoCache)
}

class ObjectSeed<out T : Any>(
        targetClass: KClass<T>, override val classInfoCache: ClassInfoCache
) : Seed {

    private val classInfo: ClassInfo<T> = classInfoCache[targetClass]
    private val valueArguments = mutableMapOf<KParameter, Any?>() // 간단한 값 프로퍼티 저장
    private val seedArguments = mutableMapOf<KParameter, Seed>() // 복합 프로퍼티 저장
    private val arguments: Map<KParameter, Any?>
        get() = valueArguments + seedArguments.mapValues { it.value.spawn() }

    // 간단한 타입의 경우 그 값을 기록하기
    override fun setSimpleProperty(propertyName: String, value: Any?) {
        val param = classInfo.getConstructorParameter(propertyName)
        valueArguments[param] = classInfo.deserializeConstructorArgument(param, value)
    }

    override fun createCompositeProperty(propertyName: String, isList: Boolean): JsonObject {
        val param = classInfo.getConstructorParameter(propertyName)
        // 프로퍼티에 대한 역직렬화 인터페이스가 있으면 그 값을 가져옴
        val deserializeAs = classInfo.getDeserializeClass(propertyName)
        val paramType = deserializeAs ?: param.type.javaType
        val seed = createSeedForType(paramType, isList)
        return seed.apply { seedArguments[param] = this }
    }

    override fun spawn(): T = classInfo.createInstance(arguments)
}

class ObjectListSeed(
        private val elementType: Type, override val classInfoCache: ClassInfoCache
) : Seed {
    private val element = mutableListOf<Seed>()

    override fun createCompositeProperty(propertyName: String, isList: Boolean) =
            createSeedForType(elementType, isList).apply { element.add(this) }

    override fun setSimpleProperty(propertyName: String, value: Any?) {
        throw JKidException("Can't set simple property in ObjectListSeed")
    }

    override fun spawn() = element.map { it.spawn() }

}

class ValueListSeed(
        elementType: Type, override val classInfoCache: ClassInfoCache
) : Seed {
    private val elements = mutableListOf<Any?>()
    private val serializerForType = serializerForType(elementType)

    override fun setSimpleProperty(propertyName: String, value: Any?) {
        elements.add(serializerForType.fromJsonValue(value))
    }

    override fun createCompositeProperty(propertyName: String, isList: Boolean): JsonObject {
        throw JKidException("Can't create composite property in ValueListSeed")
    }

    override fun spawn() = elements
}
