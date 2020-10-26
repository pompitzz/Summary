package com.example.demo.kotlin


// 타입 파라미터에 여러 제약을 가할 수 있다.
// T 타입은 반드시 CharSequence와 Appendable을 구현한 구현체여야 한다.
fun <T> ensureTrailingRerioid(seq: T)
        where T : CharSequence, T : Appendable {
    if (!seq.endsWith('.')) {
        seq.append('.')
    }
}

// 타입파라미터는 nullable하므로 null 불가능하게 막을 수 있다.
fun <T : Any> test(t: T): Nothing = TODO()

// inline 함수와 reified를 활용하면 실체화된 타입으로 취급할 수 있다.
inline fun <reified T> isA(value: Any) = value is T

fun <T> printClass(clazz: Class<T>) = println(clazz)

inline fun <reified T> printClassUsingReified() = printClass(T::class.java)

// out을 붙이면 해당 타입 파라미터를 생산할 수만 있게 되어 제역이 생긴다.
class Herd<out T : Animal> {
    val animals = emptyList<T>()
    // T가 out 위치에 있으므로 가능. 생산은 가능
    fun getTypeParameter(): T = TODO()

    // T가 in 위치에 있으므로 에러 발생. 생산은 불가능
    // fun add(t: T) = TODO()
}

fun feedAll(animalHerd: Herd<Animal>) {
    for (animal in animalHerd.animals) {
        TODO()
    }
}

fun catTest(catHerd: Herd<Cat>) {
    // out을 지정했으므로 Herd<Cat>은 Herd<Animal>의 하위 타입이 된다.
    feedAll(catHerd)
}

open class Animal {
    fun getDouble(): Double = TODO()
}

class Cat : Animal()

fun catToNumber(f: (Cat) -> Number): Number = TODO()


// 코틀린도 사용 지점 변성을 지원한다. 만약 클래스 에서 이미 변성이 지정되어 있다면 따로 사용 지점 변성을 하지 않아도 된다.
fun<T> copyData(source: MutableList<out T>,
                destination: MutableList<in T>) {
    for (item in source) {
        destination.add(item)
    }
}

fun addContent(list: MutableList<Any>) {
    list.add(1)
}

fun main() {
    println(isA<String>(1)) // false
    println(isA<String>("1")) // true

//     변성 규칙에 따라 Animal -> Double은 Cat -> Number의 하위 타입이므로 파라미터로 넘길 수 있다.
//    catToNumber { animal: Animal -> animal.getDouble() }
//
//     반환(생산)하는건(out) 클라이언트 입장에서 정해진 타입보다 하위 타입이 올 수 한다. 만약 상위 타입이 온다면 현재 반환 타입으로 지정한 타입의 모든것을 제공해줄 수 없다.
//     소비하는건(in) 정해진 타입보다 상위 타입이 올 수 있다. 상위 타입은 해당 타입이 가지는 최소한을 제공해줄 수 있으므로 해당 타입에 존재하는 소비가 가능하다. 하지만 만약 하위 타입이오면 해당 타입보다 더 특수화되었기 때문에 불가능하다.
}
