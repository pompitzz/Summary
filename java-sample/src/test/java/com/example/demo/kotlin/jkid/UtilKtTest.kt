package com.example.demo.kotlin.jkid

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class UtilKtTest {
    @Test
    fun isPrimitiveOrStringTest() {
        assertThat(String::class.java.isPrimitiveOrString()).isTrue
        assertThat(Double::class.java.isPrimitiveOrString()).isTrue
        assertThat(Long::class.java.isPrimitiveOrString()).isTrue
        assertThat(Int::class.java.isPrimitiveOrString()).isTrue
        assertThat(Boolean::class.java.isPrimitiveOrString()).isTrue
        assertThat(Float::class.java.isPrimitiveOrString()).isTrue
        assertThat(UtilKtTest::class.java.isPrimitiveOrString()).isFalse
    }

    @Test
    fun createInstanceTest() {
        SampleWithNoArg::class.createInstance()

        assertThatThrownBy { SampleWithoutNoArg::class.createInstance() }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Class must have a no-argument constructor")
    }

    @Test
    fun asJavaClassTest() {
        val subJavaClass = Sub::class.java
        assertThat(subJavaClass.asJavaClass()).isEqualTo(subJavaClass)

        val supJavaClass = subJavaClass.genericSuperclass.asJavaClass()
        assertThat(supJavaClass).isEqualTo(Sup::class.java)
    }

    @Test
    fun arrayZipTest() {
        val array = Array<Int>(5) { it }
        val list = listOf(1, 6, 3, 2)

        for ((left, right) in (array zip list)) {
            println("left: $left, right: $right")
        }
    }

    internal class SampleWithNoArg
    internal class SampleWithoutNoArg(val name: String)
    internal open class Sup<T>
    internal class Sub : Sup<String>()

}
