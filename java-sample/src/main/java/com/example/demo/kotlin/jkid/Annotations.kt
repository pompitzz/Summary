package com.example.demo.kotlin.jkid

import com.example.demo.kotlin.jkid.serialize.ValueSerializer
import kotlin.reflect.KClass


/** Json에서 제외시키는 애노테이션
 * - **Retention** 자바는 기본이 클래스 파일까지만 유지하는거지만 코틀린은 런타임까지 유지한다.
 * - target이 property인건 자바에서 사용할 수 없음(자바에서 프로퍼티는 큰 범위), 원한다면 FIELD를 쓸 것
 */
@Target(AnnotationTarget.PROPERTY)
annotation class JsonExclude

@Target(AnnotationTarget.PROPERTY)
annotation class JsonName(val name: String)

// 커스텀한 직렬화를 사용하고 싶을 때 사용하는 애노테이션
annotation class CustomSerializer(
        // ValueSerializer의 타입 파라미터는 어떤 것이든 올 수 있으므로 스타 프로젝션 사용
        val serializerClass: KClass<out ValueSerializer<*>>
)

// 인터페이스 타입인 프로퍼티를 역직렬화할 때 구현체가 필요하므로 구현체를 직접 정의하는 애노테이션
annotation class DeserializeInterface(val targetClass: KClass<out Any>) // out을 안붙이면 하위타입을 정의하질 못할 것이다.

