package com.example.demo.kotlin.dsl

class DEPENDENCIES {
    fun compile(text: String) {
        println(text)
    }

    // 자기자신을 호출할 때 스스로에 대한 람다를 받아 dsl 구문으로 받을 수 있다.
    operator fun invoke(body: DEPENDENCIES.() -> Unit) {
        body()
    }
}

fun main() {
    val dependencies = DEPENDENCIES()
    // invoke를 활용하면 dsl형식과 일반 메서드 호출 형식을 모두 지원하도록 할 수 있다.
    dependencies {
        compile("org.springframework.boot:spring-boot-starter-web")
    }
    dependencies.compile("org.springframework.boot:spring-boot-starter-web")
}
