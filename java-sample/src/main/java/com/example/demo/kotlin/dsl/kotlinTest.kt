package com.example.demo.kotlin.dsl


interface Matcher<T> {
    fun test(value: T)
}

class startWith(val prefix: String) : Matcher<String> {
    override fun test(value: String) {
        if (!value.startsWith(prefix)) {
            throw AssertionError("String $value does not start with $prefix")
        }
    }
}

// 중위 함수를 정의
infix fun <T> T.should(matcher: Matcher<T>) = matcher.test(this)

fun test1() {
    // DSL을 활용하면 테스트 코드를 깔끔하게 유지시킬 수 있다.
    "hello" should startWith("h")
}


// start는 단순히 dsl 문법을 위해 사용되는 것
object start

infix fun String.should(x: start) = StartWrapper(this)

class StartWrapper(val value: String) {
    infix fun with(prefix: String) =
            if (value.startsWith(prefix))
                Unit
            else
                throw AssertionError("String $value does not start with $prefix")
}

fun test2() {
    // 중위 함수를 활용하면 이렇게도 가능
    "hello" should start with "h"
}

