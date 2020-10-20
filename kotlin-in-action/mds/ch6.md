# 6장. 코틀린 타입 시스템
- 코틀린은 nullable 타입, 읽기 전용 컬렉션등 새로운 타입 시스템을 도입했다.
- 배열과 같이 자바 타입 시스템에서 불필요한 부분들을 제거하였다.

## Nullability
- NPE룰 피할 수 있게 돕기 위한 코틀린 타입 시스템의 특성으로 컴파일 시점에 null 에러를 파악할 수 있도록 해준다.
- 코틀린은 기본 타입이 null이 불가능하며, nullable 타입을 위해 ?를 붙이면 됨.

### 앨비스 연산자 ?:
- 어떤 값이 null일 떄 그 값대신 사용할 기본 값을 지정할 수 있다.
- `val t: String = str ?: "Default`

```kotlin
data class Address(val city: String, val country: String)

data class Company(val name: String, val address: Address?)

data class Employee(val name: String, val company: Company?)

fun print(employee: Employee) {
    // 엘비스 연산자로 throw도 가능
    val address = employee?.company?.address ?: throw IllegalArgumentException("Need Address")
    with(address) {
        print("city: $city, countyL $country")
    }
}
```

### 안전한 캐스팅: as?
```kotlin
fun findAddressCity(any: Any): String {
    val address = any as? Address ?: throw IllegalArgumentException("It is not address")
    return address.city
}
```
- 코틀린은 as?를 통해 ClassCastException이 발생하지 않도록 할 수 있다.

### null 아님 단언: !!
- !!는 null이 아님을 확신할 떄 사용하는 것으로 NPE를 감수할 수 있을 때 사용한다.

### let 함수
- nullable한 타입의 값일 때 null이 불가능한 함수의 파라미터로 넘기려고 할 때 let을 활용하면 된다.

```kotlin
fun sendEmail(message: String) = print(message)

fun main() {
    var email: String? = "email"

    // null 아니므로 함수 호출 O
    email?.let { sendEmail(it)}

    email = null

    // null 이므로 함수 호출 X
    email?.let { sendEmail(it)}
}
```

### null 불가능 타입을 원하지만 지연 초기화를 해야할 때
- null이 불가능한 타입을 사용하지만 상황에 따라 지연 초기화가 필요할 때가 존재한다.
- 그럴땐 lateinit을 활용하면 널이 불가능한 타입을 사용할 수 있다.
    - 프로퍼티 초기화전에 접근 시 예외가 발생한다.
    
```kotlin
class LateInit {
    private lateinit var kotlinService: Any
}
```

### nullable 확장 함수
- null이 될 수 있는 타입에 대해 확장함수를 정의하면 null을 쉽게 다룰수도 있다.
    - CharSequence의 isNullOrBlank 참고
 
### 타입 파라미터와 nullable
```kotlin
// 타입 파라미터는 유일하게 Any?로 추론되므로 nullable하다.
fun <T> some1(): T = TODO()

// 상한을 두어 null이 불가능하게 할 수도 있다.
fun <T : Any> some2(): T = TODO()
```

### 플랫폼 타입
- 코틀린이 null에 대한 정보를 알수 없는 타입으로 처리를 개발자에게 전적으로 맡긴다.
- 오직 자바에서 가져온 타입만 플랫폼 타입이 될 수 있다.
    - 즉 자바에서 가져온 타입을 쓸땐 null에 대해 주의를 기울여야 한다.

#### 왜 플랫폼 타입이 생겼나?
- 자바타입이 만약 모두 Nullable 타입이였다면 ArrayList\<String?>? 이런식으로 자바 타입을 사용해야 할 것이다.
- 모든 자바타입에 null 검사를 하는건 null 안정성보다 비용이 더 크기 때문에 플랫폼 타입을 두어 개발자가 처리할 수 있도록 하였다. 

> 실제로 null인 플래폼 타입을 null 불가능한 타입으로 변환을 시도하면 런타임 에러가 발생할 수 있다.

### 자바 메서드 오버라이드
- 자바 메서드를 오버라이다하고, 메서드 변수가 null 불가능한 타입을 선언된다면 null 아님을 단언하는 validation을 추가해줌

## 코틀린의 원시타입
- 코틀린은 원시 타입과 래퍼 타입의 구분이 없다. 코틀린 내부적으로 런타임에 가장 효율적인 방식으로 처리한다.
    - 대부분의 Int 타입은 자바 int로 컴파일되며, 컬렉셕이나 제네릭 같은곳에서만 래퍼 객체를 사용한다.
    
### Any, Any?: 최상위 타입
- 자바의 Object와 비슷하다. 즉 Any는 자바에서 Object로 컴파일 된다.

### Unit
- 자바의 void와 같은 기능을 한다.

### Nothing 타입: 정상적으로 끝날 수 없음을 명시한다.
- Nothing 타입은 오직 반환 타입으로만 쓸 수 있으며 Nothing을 반환하는 함수는 정상적으로 끝나지 않음을 알려준다.

```kotlin
// 정상적으로 끝날 수 없는 함수
fun fail(message: String) : Nothing {
    throw RuntimeException(message)
}

fun main() {
    // fail은 정상적으로 끝나지 않는 함수임을 알수 있으므로 country가 null이 아님을 확신할 수 있다.
    val country = Address("city", "country").country ?: fail("No City")
}
``` 

## 컬렉션과 배열
- 코틀린은 읽기 전용 클래스와 변경 가능 컬렉션(MutableCollection)을 분리했다.
- 읽기 전용 컬렉션이라고 하더라도 메서드를 타고가면서 변경 가능 컬렉션으로 언제든지 변경될 수 있다.

### 코틀린과 자바 컬렉션
- 코틀린의 모든 컬렉션은 자바 컬렉션 인터페이스의 구현체이므로 언제든지 서로 오갈 수 있다.

### 배열
```kotlin
fun main(args: Array<String>) {
    // array index for
    for (index in args.indices) {

    }

    // list -> array
    listOf(1).toIntArray()

    // array도 람다식으로 생성 가능
    val array: Array<String> = Array(12) { it.toString() }
    IntArray(5)
    intArrayOf(1, 2, 3, 4, 5)

    // array map도 가능하며 결과를 List가 됨
    val map: List<String> = array.map { it + 1 }

    // index, element for문도 가능하다.
    array.forEachIndexed { index, element -> println("index: $index, value: $element") }
}
```
