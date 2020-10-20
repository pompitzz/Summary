package com.example.demo.kotlin

import java.lang.RuntimeException

enum class Color {
    RED,
    ORANGE,
    YELLOW
    ;
}

// java 스위치랑 비슷
fun getStringColor(color: Color) =
        when (color) {
            Color.RED -> "RED"
            Color.ORANGE -> "ORANGE"
            Color.YELLOW -> "YELLOW"
        }

fun getStringColor2(color: Color) =
        when (color) {
            Color.RED, Color.ORANGE, Color.YELLOW -> "COLOR"
        }

fun getStringColor3(color1: Color, color2: Color) =
        when {
            (color1 == Color.RED || color2 == Color.ORANGE) -> "RED ORANGE"
            else -> throw RuntimeException()
        }


interface Expr
class Num(val value: Int) : Expr
class Sum(var left: Expr, val right: Expr) : Expr

// 스마트 캐스팅을 지원한다.
fun eval(e: Expr): Int =
        when (e) {
            is Num -> e.value
            is Sum -> eval(e.left) + eval(e.right)
            else -> throw IllegalArgumentException()
        }

/**
 *  클래스의 프로퍼티를 스마트 캐스팅하고 싶다면 val이면서 커스텀 접근자가 정의되어 있지 않아야 한다.
 *  - var거나 커스텀 접근자가 있으면 언제가 같은 타입을 반환해준다는 것을 확신할 수 없기 때문에..
 */
fun valOnlyCanSmartCasting() {
    val sum = Sum(Num(1), Num(2))
    if (sum.left is Num) {
        //  println(sum.left.value) 컴파일 에러 (left는 var이다)
    }
    // 스마트 캐스팅 가능(rifht는 val이기 때문)
    if (sum.right is Num) {
        println(sum.right.value)
    }
}

fun expressionWhen(e: Expr): Int =
        when (e) {
            is Num -> {
                println(e.value)
                e.value // 표현식의 블럭문은 마지막 값이 리턴 값이 된다.
            }
            is Sum -> {
                println("${e.left} + ${e.right}")
                expressionWhen(e.left) + expressionWhen(e.right)
            }
            else -> throw IllegalArgumentException()
        }

// if절도 가능하지만 when이 더 깔끔해보인다.
fun expressionIf(e: Expr): Int =
        if (e is Num) {
            println(e.value)
            e.value // 표현식의 블럭문은 마지막 값이 리턴 값이 된다.
        } else if (e is Sum) {
            println("${e.left} + ${e.right}")
            expressionIf(e.left) + expressionIf(e.right)
        } else {
            throw IllegalArgumentException()
        }

fun iterationEx() {
    // 1~10 출력
    for (i in 1..10) {
    }

    for (i in 1..10 step 2) {
    }

    // 10에서 1까지 2 칸씩
    for (i in 10 downTo 1 step 2) {
        print("$i, ")
    }

    // map의 key, value를 for문으로 풀어낼 수 있다.
    for ((key, value) in mutableMapOf(Pair("A", 1))) {

    }

    // withIndex를 활용하면 리스트의 index도 간편히 가져올 수 있다.
    for ((index, value) in mutableListOf(1, 2, 3).withIndex()) {

    }
}

fun isSmallLetter(c: Char) = c in 'a'..'z' // 컴파일 -> 'a'<= c && c <= 'z'
fun isNotSmallLetter(c: Char) = c !in 'a'..'z'

fun regognize(c: Char): String =
        when (c) { // when절에서도 in 검증 방식 사용 가능
            in 'a'..'z' -> "is small letter"
            else -> "is not small letter"
        }


fun <T> joinToString(collection: Collection<T>, separator: String = ",", id: Long = 1L): String {
    val builder = StringBuilder()
    for ((index, element) in collection.withIndex()) {
        if (index > 0) builder.append(separator)
        builder.append(element)
    }
    return builder.toString()
}

fun main() {
    joinToString(separator = "|", collection = listOf(1, 2, 3))
    joinToString(collection = listOf(1, 2, 3))
}

// 주 생성자
open class User (val name: String) {}

// 위를 풀어쓰면 아래와 같다.
class User2 constructor(_name: String) {
    val name: String
    init {
        name = _name
    }
}

// 부모의 생성자 호출은 아래와같이 가능
class SubUser(name: String) : User(name)


interface Member {
    val name: String
    // 다른 프로퍼티를 활용하여 커스텀 접근자를 가지는 프로퍼티를 구현할 수도 있다.(상태를 가지면 안되므로 Backing Field가 존재 안함)
    val listCharName: Char
        get() = name.lastChar
}

// 추상 프로퍼티는 반드시 구현되어야 한다.
class PrivateMember(override val name: String) : Member {}
