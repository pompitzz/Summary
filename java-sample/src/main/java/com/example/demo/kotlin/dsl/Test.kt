package com.example.demo.kotlin.dsl

// 파라미터를 일반 람다로 정의
fun myBuildString(
        buildAction: (StringBuilder) -> Unit
): String {
    val sb = StringBuilder()
    buildAction(sb)
    return sb.toString()
}

// 파라미터를 확장 함수 타입의 람다로 정의(수신 객체 지정 람다)
fun myBuildString2(
        buildAction: StringBuilder.() -> Unit
): String {
    val sb = StringBuilder()
    sb.buildAction()
    return sb.toString()
}

// 실제 코틀린에 구현된 방식. apply를 활용하여 더 간다히 정의할 수 있다.
fun buildStringByKotlinLib (
        buildAction: StringBuilder.() -> Unit
) = StringBuilder().apply { buildAction }.toString()

// with를 활용할 수도 있다.
fun buildStringUsingWith(
        buildAction: StringBuilder.() -> Unit
) = with(StringBuilder(), buildAction).toString()

fun main() {
    // 일반 람다이므로 it을 명시적으로 붙여줘야 한다.
    myBuildString {
        it.append("hello")
        it.append("world")
    }

    // 수신 객체 지정 람다이므로 this가 자동적으로 바인딩되므로 it을 생략하여도 된다.
    myBuildString2 {
        append("hello")
        append("world")
    }
}
