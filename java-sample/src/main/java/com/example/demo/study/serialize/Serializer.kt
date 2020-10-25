package com.example.demo.study.serialize

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

data class Temp(
        val name: String,
        val temp: Temp? = null,
)

fun main() {
    val json = serialize(Temp("1", Temp("2", Temp("3"))))
    println(json)
}

fun serialize(obj: Any): String = buildString { serializeObject(obj) }

private fun StringBuilder.serializeObject(obj: Any) {
    obj.javaClass.kotlin.memberProperties
            .joinToStringWithBuilder(this, prefix = "{", postfix = "}") {
                serializeProperty(it, obj)
            }
}

private fun StringBuilder.serializeProperty(property: KProperty1<Any, *>, obj: Any) {
    val name = property.name
    serializeString(name)
    append(": ")
    serializePropertyValue(property.get(obj))
}

private fun StringBuilder.serializePropertyValue(value: Any?) {
    when (value) {
        null -> append("null")
        is String -> serializeString(value)
        is Number, Boolean -> append(value.toString())
        is Iterable<*> -> serializeArray(value)
        else -> serializeObject(value)
    }
}

private fun StringBuilder.serializeString(value: String) {
    append("\"")
    append(value)
    append("\"")
}

private fun StringBuilder.serializeArray(data: Iterable<*>) {
    data.joinToStringWithBuilder(this, prefix = "[", postfix = "]") {
        serializePropertyValue(it)
    }
}

private fun <T> Iterable<T>.joinToStringWithBuilder(builder: StringBuilder, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", callback: ((T) -> Unit)) {
    joinTo(builder, separator, prefix, postfix, limit, truncated) {
        callback(it)
        ""
    }
}
