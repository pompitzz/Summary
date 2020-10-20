package com.example.demo.kotlin

import java.io.File

data class Item(val name: String, val price: Int)

private fun sequenceTest() {
    val items = listOf(Item("item1", 10000), Item("item2", 20000))

    // 기본 확장 함수는 지연 계산을 하지 않는다.
    items.filter { it.price < 1000 }
            .map { it.price }

    // 지연 계산을 위해 asSequence를 이용하면된다.
    // - 자바 스트림과 동일하므로 stream을 써도된다. 자바 8 이전의 호환성을 위해 asSequence가 생김
    items.asSequence()
            .filter { it.price < 1000 }
            .map { it.price }
            .toList()

    // 무한 시퀀스를 만들 수 있다.
    generateSequence(0) { it + 1 }
            .takeWhile { it <= 100 }
            .forEach { println(it) }


    // 확장함수와 무한 시퀀스를 활용하여 부모 파일이 hidden일 때 까지 계속 탐색하도록 함수를 만듬
    fun File.isInsideHiddenDirectory() =
            generateSequence(this) { it.parentFile }
                    // hidden file을 찾으면 멈춘다.
                    .any { it.isHidden }

    File("/Users/dongmyeonglee/Projects/simple-summary/java-sample/src/main/java/com/example/demo/kotlin/lambda.kt")
            .isInsideHiddenDirectory()
}


// Runnable 같은 함수형 인터페이스는 SAM 생성자를 활용하자
fun createRunable() = Runnable { println("RUN!") }
val runnable = Runnable { println("RUN!") }


fun buildString(): String {
    return with(StringBuilder()) {
        append("Hello")
        append(" ")
        append("World")
        return toString()
    }
}

fun buildString2(): String =
        with(StringBuilder()) {
            append("Hello")
            append(" ")
            append("World")
            toString() // 반환 값
        }


fun buildString3(): String =
        StringBuilder().apply {
            append("Hello")
            append(" ")
            append("World")
        }.toString() // 수신 객체를 반환하므로 toString을 호출


class User4() {
    var name: String = ""
        get() { TODO()
        }
    var age: Int = 0
        get() {
            TODO()
        }
}


// apply를 통해 빌더처럼 사용 가능(굳이?)
fun main() {
    User4().apply {
        name = "DEXTER"
        age = 13
    }
}
