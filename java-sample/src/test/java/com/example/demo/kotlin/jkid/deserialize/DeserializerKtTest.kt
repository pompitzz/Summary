package com.example.demo.kotlin.jkid.deserialize

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType

internal class DeserializerKtTest {
    @Test
    fun isAssignableFromTest() {
        assertThat(List::class.java.isAssignableFrom(listOf<String>().javaClass)).isTrue
        assertThat(Any::class.java.isAssignableFrom(listOf<String>().javaClass)).isTrue
        assertThat(List::class.java.isAssignableFrom(Any::class.java)).isFalse
    }

    @Test
    fun `생성자 파라미터들의 자바 타입이 어떻게 들어가는지 테스트`() {
        val primaryConstructor = Sample::class.primaryConstructor!!
        val parameters = primaryConstructor.parameters

        assertThat(parameters.size).isEqualTo(4)
        // 제네릭 클래스는 ParameterizedType이 된다.
        assertThat(parameters[0].type.javaType).isInstanceOf(ParameterizedType::class.java)
        assertThat(parameters[1].type.javaType).isInstanceOf(ParameterizedType::class.java)
        assertThat(parameters[2].type.javaType).isInstanceOf(Class::class.java)
        assertThat(parameters[3].type.javaType).isInstanceOf(Class::class.java)
    }

    class Sample(val list: List<String>,
                 val map: Map<String, String>,
                 val name: String,
                 val sample: Sample
    )
}
