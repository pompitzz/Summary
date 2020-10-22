package com.example.demo.kotlin.jkid

import java.io.Reader
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.*
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

/** Json에서 제외시키는 애노테이션
 * - **Retention** 자바는 기본이 클래스 파일까지만 유지하는거지만 코틀린은 런타임까지 유지한다.
 * - target이 property인건 자바에서 사용할 수 없음(자바에서 프로퍼티는 큰 범위), 원한다면 FIELD를 쓸 것
 */
@Target(AnnotationTarget.PROPERTY)
annotation class JsonExclude()

annotation class JsonName(val name: String)

interface ValueSerializer<T> {
    fun toJsonValue(value: T): Any?
    fun fromJsonValue(jsonValue: Any?): T
}

/** 커스텀한 직렬화를 사용하고 싶을 때 사용하는 애노테이션
 */
annotation class CustomSerializer(
        // ValueSerializer의 타입 파라미터는 어떤 것이든 올 수 있으므로 스타 프로젝션 사용
        val serializerClass: KClass<out ValueSerializer<*>>
)


//  직렬화 함수
fun serialize(obj: Any): String = buildString { serializeObject(obj) }

//  함수 파라미터로 받을걸 확장 함수의 수신 객체로 바꾸는 방식은 코틀린에서 자주 사용하는 패턴 중 하나이다.
private fun StringBuilder.serializeObject(obj: Any) {
    val kClass = obj.javaClass.kotlin
    kClass.memberProperties
            .filter { it.findAnnotation<JsonExclude>() == null } // findAnn를 활용하면 원하는 Anno를 찾을 수 있다.
            .joinToStringBuilder(this, prefix = "{", postfix = "}") {
                serializeProperty(it, obj)
            }
}

private fun StringBuilder.serializeProperty(prop: KProperty1<Any, *>, obj: Any) {
    val jsonNameAnn = prop.findAnnotation<JsonName>()
    val propName = jsonNameAnn?.name ?: prop.name
    serializeString(propName)
    append(": ")

    val value = prop.get(obj)
    // 확장함수로 구현한 getSerializer를 이용하여 커스텀 직렬화를 사용할지 정한다.
    val jsonValue = prop.getCustomSerializer()?.toJsonValue(value) ?: value
    serializePropertyValue(jsonValue)
}

private fun StringBuilder.serializeString(s: String) {
    append('\"')
    append(s)
    append('\"')
}

private fun StringBuilder.serializePropertyValue(value: Any?) {
    when (value) {
        null -> append("null")
        is String -> serializeString(value)
        is Number, is Boolean -> append(value.toString())
        is List<*> -> serializeList(value)
        else -> serializeObject(value)
    }
}

private fun StringBuilder.serializeList(data: List<Any?>) {
    data.joinToStringBuilder(this, prefix = "[", postfix = "]") { serializePropertyValue(it) }
}

private fun <T> Iterable<T>.joinToStringBuilder(
        stringBuilder: StringBuilder, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "",
        limit: Int = -1, truncated: CharSequence = "...", callback: ((T) -> Unit)? = null): StringBuilder {
    return joinTo(stringBuilder, separator, prefix, postfix, limit, truncated) {
        if (callback == null) return@joinTo it.toString()
        callback(it)
        ""
    }
}

fun KProperty<*>.getCustomSerializer(): ValueSerializer<Any?>? {
    val customSerializerAnn = findAnnotation<CustomSerializer>() ?: return null
    val serializerClass = customSerializerAnn.serializerClass
    // object class(싱글톤)이라면 objectInstance로 가져올 수 있고 그렇지 않으면 인스턴스를 생성해야 한다.
    val valueSerializer = serializerClass.objectInstance ?: serializerClass.createInstance()
    @Suppress("UNCHECKED_CAST")
    return valueSerializer as ValueSerializer<Any?>
}

/** 인터페이스 타입인 프로퍼티를 역직렬화할 때 구현체가 필요하므로 구현체를 직접 정의하는 애노테이션
 *  KClass는 java.lang.Class와 같은 역할을 하는 코틀린 타입
 */
annotation class DeserializeInterface(val targetClass: KClass<out Any>) // out을 안붙이면 하위타입을 정의하질 못할 것이다.

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
    val paramClass = paramType.javaClass

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

fun <T : Any> deserialize(json: Reader, targetClass: KClass<T>): T {
    val seed = ObjectSeed(targetClass, ClassInfoCache())
    Parser(json, seed).parse()
    return seed.spawn()
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
        val seed = createSeedForType(deserializeAs ?: param.type.javaType, isList)
        return seed.apply { seedArguments[param] = this }
    }

    override fun spawn(): T = classInfo.createInstance(arguments)
}
