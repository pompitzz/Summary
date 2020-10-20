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

var StringBuilder.lastChar: Char
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


interface Clickable {
    fun click()

    // 자바 디폴트 메서드 같이 구현 정의 가능. (상태를 가질 순 없다.)
    fun showOff() = println("Clickable Interface")
}

// 구현, 상속은 :로 표현한다.
class Button : Clickable {
    override fun click() {
        TODO("Not yet implemented")
    }
}

interface Focusable {
    fun showOff() = println("I`m Focusable showOff")
}

internal open class TalkativeButton : Focusable {
    private fun yell() = println("yell")
    protected fun whisper() = println("whisper")
}

// 확장하려는 클래스가 internal이므로 가시성이 수준이 같거나 더 낮아야 한다.
internal fun TalkativeButton.giveSpeech() {
//    yell()  private이므로 호출 불가
//    whisper()  자바와 다르게 protected는 오직 하위 클래스에서만 사용 가능
}

class Outer {
    class Inner1 {
        // 코틀린은 기본이 자바의 static 클래스처럼 외부의 참조가 없는 중첩 클래스이다.
        // fun test() = this@Outer 외부 참조가 없으니 불가능
    }

    // 내부 클래스를 위해 inner를 명시적으로 붙여줘야 한다.
    inner class Inner2 {
        fun test() = this@Outer
    }
}

// 계층 확장 제한을 가능하게 해주는 봉인 클래스
sealed class Expr2 {
    class Num(val value: Int) : Expr2()
    class Sum(val left: Expr2, val right: Expr2) : Expr2()
}

// 봉인 클래스를 활용하면 when절에서 else를 사용하지 않아도 된다.
// 새로운 중첩 클래스가 생기면 when절을 반드시 구현해야 하므로 안전하다.
fun eval2(e: Expr2): Int =
        when (e) {
            is Expr2.Num -> TODO()
            is Expr2.Sum -> TODO()
        }

class AUser(val name: String) {
    var address: String = "unselected"
        set(value: String) {
            print("백킹 필드값(이전값): $field, 새로운 값: $value")
            field = value
        }
}

class LengthCounter {
    var counter: Int = 0
        private set // set은 클래스 내부에서만 사용할 수 있게 함.
}

// 직접 구현하지 않으면 innerList로 전부 위임하도록 해준다.
class DelegatingCollection<T>(innerList: Collection<T> = ArrayList<T>()) : Collection<T> by innerList {
    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }
}

// 싱글톤이면 충분한 것들에서 유용하게 사용된다.
object StringComparator : Comparator<String> {
    override fun compare(o1: String?, o2: String?): Int {
        TODO("Not yet implemented")
    }
}

data class Person(val name: String) {
    //  중접 객체로도 사용하며 외부에서 정적 메서드를 호출하는 거처럼 접근이 가능해진다.
    object NameComparator : Comparator<String> {
        override fun compare(o1: String?, o2: String?): Int {
            TODO("Not yet implemented")
        }

    }
}

class BUser private constructor(val name: String) {
    companion object {
        // private 생성자로 접근가능, 사용처에선 static 메서드처럼 호출 가능
        fun newUser(name: String) = BUser(name)
    }
}

// 동반객체도 인터페이스를 구현하여 다형성을 활용할 수 있다.
interface JSONFactory<T> {
    fun fromJSON(jsonText: String): T
}

class CUser(val name: String) {
    // 동반 객체도 인터페이스를 구현할 수 있다.
    companion object : JSONFactory<CUser> {
        override fun fromJSON(jsonText: String): CUser {
            TODO("Not yet implemented")
        }
    }
}

fun <T> loadFromJSON(factory: JSONFactory<T>): T {
    TODO()
}

class DUser(val name: String) {
    // 확장 함수를 사용하기 위해서 빈 동반 객체 정의가 필요
    companion object
}


// 외부에서 동반 객체의 확장 함수를 구현해 관시사를 분리할 수 있다.
fun DUser.Companion.fromJSON(json: String): DUser {
    TODO()
}

interface Sender {
    fun send()
}

fun sendSend(sender: Sender) = sender.send()

fun main() {
    sendSend(object : Sender {
        override fun send() {
            TODO("Not yet implemented")
        }
    })
}
