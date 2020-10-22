package com.example.demo.kotlin

import org.springframework.util.Assert
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty1


class HasTempFolder {
    // 프로퍼티가 아닌 getter에 애노테이션을 명시하는 방법
    // @property는 코틀린 프로퍼티 전체(getter, field 등등)에 적용될 수 있게 할 수 있다.
    @get:MyAnno
    val folder = Any()
}

annotation class MyAnno {

}

class Person2(val name: String, val age: Int)

fun foo(x: Int) = println(x)

var counter = 0
fun main() {
    // 최상위 프로퍼티는 Property0이다. 이 프로퍼티는 인자 없이 get을 호출한다.
    val kProperty: KMutableProperty0<Int> = ::counter
    kProperty.setter.call(10)
    Assert.isTrue(kProperty.get() == 10)

    val person = Person2("name", 1)
    // 객체의 프로퍼티는 Property1로 get호출 시 객체를 넘겨주면 된다.
    val ageProperty1: KProperty1<Person2, Int> = Person2::age
    Assert.isTrue(ageProperty1.get(person) == 1)
}
