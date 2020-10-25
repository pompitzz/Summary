package com.example.demo.kotlin.jkid.serialize

import com.example.demo.kotlin.jkid.CustomSerializer
import com.example.demo.kotlin.jkid.JsonExclude
import com.example.demo.kotlin.jkid.JsonName
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

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
