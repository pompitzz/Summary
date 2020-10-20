# 5장. 람다로 프로그래밍

## 람다와 컬렉션
```kotlin
fun main() {
    val items = listOf(Item("item1", 10000), Item("item2", 20000))
    println(items.maxByOrNull { it.price }) // 스트림 원소를 it으로 바로 참조 가능하다.
    println(items.maxByOrNull(Item::price)) // 메서드 참조도 가능

    // 람다를 변수에 직접 할당할 수 있다.
    val sum = {x: Int, y: Int -> x + y}

    // 람다 본문을 바로 호출하도록 할 수 있다.
    run { println("Hello World") }
}
```
- 자바와 다르게 코틀린은 람다에서 final이 아닌 변수에 접근이 가능하다.
    - 컴파일러가 특별한 wrapper로 감싸서 참조는 그대로두고 내부의 값을 변경할 수 있도록 해준다.
    
## 컬렉션 함수형 API 
- 기본적인 자바 스트림에서 제공하는 filter, map 등등을 제공한다.

```kotlin
fun main() {
    val items = listOf(Item("item1", 10000), Item("item2", 20000))
    items.all { it.price < 10000 } // 모두 만족
    items.any { it.price < 10000 } // 하나라도 만족하는지?
    items.count { it.price < 10000 } // 키운팅

    val itemsList = listOf(items, items)
    val flattedItems = itemsList.flatten() // 단순히 펼치려면 flatten 쓰면된다.

    val maps = mapOf(1 to "1", 2 to "2")
    maps.mapValues { it + it } // mapValues 가능 
    maps.mapKeys { it + 1 } // mapKeys 가능
}
```

### 지연 계산을 위한 시퀀스
```kotlin
fun main() {
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
```

## 자바 함수형 인터페이스 활용
- 자바 메서드에 코틀린 람다를 전달할 수 있다.
    - 컴파일러가 함수형 인터페이스의 익명 클래스로 만들어줌
    
### 람다 vs 익명 클래스
- 람다와 익명 클래스는 간결성에서도 차이가 나지만, 재사용에서도 차이가 있다.
- 익명 클래스는 생성마다 새로운 인스턴스를 만들지만 람다는 재사용을 활용한다.

> 책 기준으로 inline 되지 않은 람다식은 구버전 호환을 위해 익명 클래스로 만들어짐.
> - 자바 8부터 제공하는 람다를 사용하도록 변경될 예정이다.
> - 대부분 기본 확장함수는 inline을 활용하므로 익명 클래스를 만들진 않는다.

### SAM 생성지: 람다를 함수형 인터페이스로 명시적 변경
```kotlin
// Runnable 같은 함수형 인터페이스는 SAM 생성자를 활용하자
fun createRunable() = Runnable { println("RUN!") }
val runnable = Runnable { println("RUN!")}
```
- 컴파일러가 자동으로 람다를 함수형 인터페이스 익명 클래스로 바꾸지 못할 상황에는 SAM 생성자를 활용한다.

## 수신 객체 지정 람다: with, apply

### with 함수(수신 객체 지정 람다)
```kotlin
fun buildString(): String {
    return with(StringBuilder()) { // with 활용
        append("Hello")
        append(" ")
        append("World")
        return toString()
    }
}

fun buildString2(): String =
        with(StringBuilder()) { // with 활용
            append("Hello")
            append(" ")
            append("World")
            toString() // 반환 값
        }
```
- 어떤 객체를 람다식에서 사용할 때 객체의 이름을 계속해서 반복하지 않도록 할 수 있다.

### apply 함수(수신 객체 반환 람다)
```kotlin
fun buildString3(): String =
        StringBuilder().apply {
            append("Hello")
            append(" ")
            append("World")
        }.toString() // 수신 객체를 반환하므로 toString을 호출


class User4() {
    var name: String = ""
        get() {
            TODO()
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
```

