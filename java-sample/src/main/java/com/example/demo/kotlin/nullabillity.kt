package com.example.demo.kotlin

import java.lang.IllegalArgumentException

data class Address(val city: String, var country: String?)

data class Company(val name: String, var address: Address?)

data class Employee(val name: String, var company: Company?)

fun print(employee: Employee) {
    // 엘비스 연산자로 throw도 가능
    val address = employee.company?.address ?: throw IllegalArgumentException("Need Address")
    with(address) {
        print("city: $city, countyL $country")
    }
}

fun findAddressCity(any: Any): String {
    val address = any as? Address ?: throw IllegalArgumentException("It is not address")
    return address.city
}

fun sendEmail(message: String) = print(message)


class LateInit {
    private lateinit var kotlinService: Any
}

// 타입 파라미터는 유일하게 Any?로 추론되므로 nullable하다.
fun <T> some1(): T = TODO()

// 상한을 두어 null이 불가능하게 할 수도 있다.
fun <T : Any> some2(): T = TODO()


// 정상적으로 끝날 수 없는 함수
fun fail(message: String): Nothing {
    throw RuntimeException(message)
}

fun main(args: Array<String>) {
    // array index for
    for (index in args.indices) {

    }

    // list -> array
    listOf(1).toIntArray()

    // array도 람다식으로 생성 가능
    val array: Array<String> = Array(12) { it.toString() }
    IntArray(5)
    intArrayOf(1, 2, 3, 4, 5)

    // array map도 가능하며 결과를 List가 됨
    val map: List<String> = array.map { it + 1 }

    // index, element for문도 가능하다.
    array.forEachIndexed { index, element -> println("index: $index, value: $element") }
}
