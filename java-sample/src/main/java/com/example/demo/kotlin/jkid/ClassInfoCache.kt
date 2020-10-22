package com.example.demo.kotlin.jkid

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType

/** 역직렬화는 json -> object로 가는것이므로 json에 매칭되는 모든 키에 맞는 적절한 프로퍼티를 일일히 찾아야한다.
 *  - 매번 이런 검색을 수행하는건 효율적이지 못하므로 클래스별로 한번만 검색을 수행하도록 캐싱한다.
 */
class ClassInfoCache {
    private val cacheData = mutableMapOf<KClass<*>, ClassInfo<*>>()

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(cls: KClass<T>): ClassInfo<T> =
            cacheData.getOrPut(cls) { ClassInfo(cls) } as ClassInfo<T>
}

class ClassInfo<T : Any>(cls: KClass<T>) {

    private val className = cls.qualifiedName
    private val constructor = cls.primaryConstructor
            ?: throw JKidException("Class ${cls.qualifiedName} doesn't have a primary constructor")

    // JSON 파일의 각 키에 해당하는 파라미터를 저장
    private val jsonNameToParamMap = hashMapOf<String, KParameter>()

    // 파라미터에 대한 직렬화기 저장
    private val paramToSerializerMap = hashMapOf<KParameter, ValueSerializer<out Any?>>()

    // @DeserializeInterface 인자로 지정한 클래스 저장
    private val jsonNameToDeserializeClassMap = hashMapOf<String, Class<out Any>?>()

    init {
        constructor.parameters.forEach { cacheDataForParameter(cls, it) }
    }

    private fun cacheDataForParameter(cls: KClass<T>, param: KParameter) {
        // param setting
        val paramName = param.name ?: throw JKidException("Class $className has constructor parameter without name")
        val property = cls.declaredMemberProperties.find { it.name == paramName } ?: return
        val name = property.findAnnotation<JsonName>()?.name ?: paramName
        jsonNameToParamMap[name] = param

        // deserialize setting
        val deserializeClass = property.findAnnotation<DeserializeInterface>()?.targetClass?.java
        jsonNameToDeserializeClassMap[name] = deserializeClass

        // valueSerializer setting
        val valueSerializer = property.getCustomSerializer()
                ?: serializerForType(param.type.javaType)
                ?: return
        paramToSerializerMap[param] = valueSerializer
    }


    // 프로퍼티이름으로 생성자 파라미터를 가져올 수 있다.
    fun getConstructorParameter(propertyName: String): KParameter =
            jsonNameToParamMap[propertyName]
                    ?: throw JKidException("Constructor parameter $propertyName is not found for class $className")

    fun getDeserializeClass(propertyName: String): Class<out Any>? = jsonNameToDeserializeClassMap[propertyName]

    fun deserializeConstructorArgument(param: KParameter, value: Any?): Any? {
        val serializer = paramToSerializerMap[param]
        if (serializer != null) return serializer.fromJsonValue(value)

        validateArgumentType(param, value)
        return value
    }

    private fun validateArgumentType(param: KParameter, value: Any?) {
        if (value == null && !param.type.isMarkedNullable) {
            throw JKidException("Received null value for non-null parameter ${param.name}")
        }
        if (value != null && value.javaClass != param.type.javaType) {
            throw JKidException("Type mismatch for parameter ${param.name}: " +
                    "expected ${param.type.javaType}, found ${value.javaClass}")
        }
    }

    fun createInstance(arguments: Map<KParameter, Any?>): T {
        ensureAllParametersPresent(arguments)
        // callby는 디폴트 파라미터 값을 활용할 수 있게 해준다.
        return constructor.callBy(arguments)
    }

    private fun ensureAllParametersPresent(arguments: Map<KParameter, Any?>) {
        for (param in constructor.parameters) {
            if (arguments[param] == null && !param.isOptional && !param.type.isMarkedNullable) {
                throw JKidException("Missing value for parameter ${param.name}")
            }
        }
    }
}
}


class JKidException(message: String) : Exception(message)
