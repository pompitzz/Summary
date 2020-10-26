package com.example.demo.kotlin.dsl

import org.springframework.util.Assert

open class Tag(val name: String) {
    private val children = mutableListOf<Tag>()

    protected fun <T : Tag> doInit(child: T, init: T.() -> Unit) {
        child.init()
        children.add(child)
    }

    override fun toString() = "<$name>${children.joinToString("")}</$name>"
}

fun table(init: TABLE.() -> Unit) = TABLE().apply(init)

// TABLE, TR, TD 클래스는 생성코드에 보여지면 안되므로 대문자로 정의하여 일반 클래스랑 구분한다.
class TABLE : Tag("table") {
    fun tr(init: TR.() -> Unit) = doInit(TR(), init)
}

class TR : Tag("tr") {
    fun td(init: TD.() -> Unit) = doInit(TD(), init)
}

class TD : Tag("td")

// 수신 객체 지정 람다를 통해 타입 안전하게 HTML에 대한 DSL을 제공할 수 있다.
fun main() {
    val htmlTable =
            table {
                // 자식들을 저장해놓기 때문에 동적으로 생성할 수도 있다.
                for (i in 1..2) {
                    tr {
                        td {

                        }
                    }
                }
            }
    Assert.isTrue(
            "<table><tr><td></td></tr><tr><td></td></tr></table>" == htmlTable.toString()
    )
}
