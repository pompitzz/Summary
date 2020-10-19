package com.example.demo.kotlin

fun test() = "Name"

fun test2() = "123"

class Name {
    fun test3() = "123"
}


open class Parent
class Child : Parent()

fun Parent.hi() = println("Parent.hi")
fun Child.hi() = println("Child.hi")



val String.lastChar: Char
    get() = get(length - 1)

var StringBuilder.lastChar : Char
    get() = get(this.length - 1)
    set(value: Char) {
        this.setCharAt(this.length - 1, value)
    }

fun print(vararg args: String) {
    for (arg in args) {
        println(arg)
    }
}

fun printArray(args: Array<String>) {
    // Array 객체를 넘길때도 *를 반드시 붙여줘야 한다.
    print(*args)
}

// 코틀린의 확장 함수 제공으로 편리하게 문자열을 다룰 수 있다.
fun parsePath(path: String) {
    val directory = path.substringBeforeLast('/')
    val fullName = path.substringAfterLast('/')
    val fileName = fullName.substringBeforeLast('.')
    val extenstion = fullName.substringAfterLast('.')
    val substringBeforeLast = extenstion.substringBeforeLast("123")
}

fun regex() {
    // 명시적으로 정규 표현식을 표현
    val regex = "\\d\\d".toRegex()

    // 삼중 따옴표는 역슬래쉬를 두번써 이스케이핑이 필요 없다.
    val regex2 = """\d\d""".toRegex()
}


