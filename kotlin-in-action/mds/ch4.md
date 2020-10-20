# 4장. 클래스, 객체, 인터페이스

## 클래스 계층 정의
### 인터페이스와 클래스
```kotlin
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
```

### 자바에서 코틀린 인터페이스의 디폴트 메서드 구현
- 자바는 8부터 디폴트 메서드를 제공하지만 코틀린은 자바6부터 호환성을 제공해야 한다.
- 그래서 코틀린에서는 디폴트 메서드가 각 구현의클래스의 정적 메서드로 들어가게 된다.

### open, final, abstract 변경자
- 코틀린은 기본이 final이기 때문에 상속이 불가능하다.
- 상속을 위해서 open 변경자를 명시해줘야 한다.
    - 이는 메서드, 필드변수 모두 적용된다.
- override 메서드는 자동으로 open이 적용되는데 명시적으로 final을 붙여 상속을 막을 수 있다.
- abstract는 자바와 동일하게 추상 메서드를 의미한다.

### 스마트 캐스트와 상속
- 이전에 스마트 캐스팅을 위해선 클래스의 프로퍼티가 val이면서 커스텀 접근자를 구현하지 않아야 가능하다고 했다.
- 이는 클래스에도 적용되기 때문에 만약 클래스가 open되어 있다면 스마트 캐스트는 불가할 것이다.

### 가시성 변경자
- 코틀린은 기본이 public이며 default 접근자는 따로 존재하지 않는다. 
- 모듈 내부에서만 사용할 수 있는 internal 접근자를 따로 제공한다.
- public, internal, private, protected의 가시성 변경자가 존재하며 protected를 제외하곤 최상위에 선언도 가능하다.
 
```kotlin
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
```

### 코틀린과 자바의 가시성 변경자
- public, protected, private은 자바의 바이트코드 안에서 그대로 유지
- 하지만 private class같이 자바에서 구현이 불가능한 것들은 private 클래스를 패키지-전용 클래스로 컴파일한다.
- internal 변경자도 자바에서 지원되지 않는 변경자이므로 바이트코드상으론 public이 된다.
    - 그러므로 자바에서 접근이 가능하지만 internal 멤버의 이름을 의도적으로 바꾸어 외부에서 사용을 하기 어렵게 컴파일한다.
    
### 내부 클래스와 중첩 클래스
- 코틀린은 외부 클래스가 내부 클래스의 private 멤버에 접근이 불가능하다.

```kotlin
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
```

### 코틀린 중첩 클래스를 유용하게 사용할 수 있는 방법
```kotlin
// 계층 확장 제한을 가능하게 해주는 봉인 클래스 (자신의 외부에 자신을 상속한 클래스를 둘 수 없음)
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
```

## 뻔하지 않은 생성자와 프로퍼티를 갖는 클래스
### 코틀린의 생성자
- 코틀린 클래스의 생성자는 크게 주 생성자와, 부 생성자로 구분할 수 있다.
    - 주 생성자는 클래스 본문이 아닌 괄호 안에서 정의
    - 부 생성자는 클래스 본문 안에서 정의
    
```kotlin
// 주 생성자
open class User (val name: String) {}

// 위를 풀어쓰면 아래와 같다.
class User2 constructor(_name: String) {
    val name: String
    init {
        name = _name
    }
}

// 부모의 생성자 호출은 아래와같이 가능하며 괄호를 붙여 부모 클래스 생성자를 호출해줘야 한다.
class SubUser(name: String) : User(name)
```

> 코틀린은 모든 생성자 프로퍼티에 디폴트 값을 부여하면 자동으로 디폴트 생성자를 만들어준다.
> - 코틀린은 디폴트 파라미터가 있기 때문에 대부분의 부 생성자 오버로딩이 필요 없다.



### 인터페이스 프로퍼티
- 인터페이스에서 상태를 가질 순 없지만 추상 프로퍼티 정의가 가능하다.

```kotlin
interface Member {
    val name: String
    // 다른 프로퍼티를 활용하여 커스텀 접근자를 가지는 프로퍼티를 구현할 수도 있다.(상태를 가지면 안되므로 Backing Field가 존재 안함)
    val listCharName: Char
        get() = name.lastChar
}

// 추상 프로퍼티는 반드시 구현되어야 한다.
class PrivateMember(override val name: String) : Member {}
```

### 커스텀 접근자의 Backing field
- 프로퍼티는 값을 직접 저장하는 프로퍼티와 커스텀 접근자를 활용해 매번 새로운 값을 계산하는 프로퍼티가 존재한다.
- 이 두가지 방법을 조합하여 값이 변경할 때 이전 값과 현재 저장할 값을 이용하여 원하는 로직을 수행하도록 할 수 있다.
- 이를 위해 접근자 안에서 해당 프로퍼티의 Backing field에 접근할 수 있어야 한다.

```kotlin
class AUser(val name: String) {
    var address: String = "unselected"
        set(value: String) {
            // backing field는 `field`로 접근가능 (get에서 field를 참조할 수 있지만 읽기만 가능하다.)
            print("백킹 필드값(이전값): $field, 새로운 값: $value")
            field = value
        }
}

fun main() {
    val aUser = AUser("name")
    aUser.address = "서울시" // setter 호출
    aUser.address = "부산시" // setter 호출
//    ## 출력 ##
//    백킹 필드값(이전값): unselected, 새로운 값: 서울시
//    백킹 필드값(이전값): 서울시, 새로운 값: 부산시
}
``` 

#### 클라이언트 입장에서의 Backing Field
- 해당 프로퍼티를 사용하는 클라이언트 입장에서는 Backing Field에 대해 알 필요가 없다.
- 디폴트 접근자로 구현을 하더라도 코틀린 내부적으로 Backing Field를 생성해주기 때문이다.
- 단, 직접 커스텀 접근자를 구현하였는데 거기서 Backing Field를 사용하지 않으면 , Backing Field는 존재하지 않게 된다.

### 프로퍼티 접근자 가시성 변경
```kotlin
class LengthCounter {
    var counter: Int = 0
        private set // set은 클래스 내부에서만 사용할 수 있게 함.
}
```

## 컴파일러가 생성한 메서드: 데이터 클래스와 클래스 위임
- JVM언어에서는 hash 컬렉션의 사용 방식으로 인해 equals, hashCode를 반드시 동시에 알맞게 구현해줘야 하는 규칙이 있다.
    - 최적화를 위해 hashCode로 비교 후 equals로 한번 더 비교하기 때문이다.
- 이런 보일러 플레이트 같은 메서드들을 자동 구현해주는 data class가 존재한다. -> `data class User(val name: String)`

### 클래스 위임: by
- 상속을 하지 않고 클래스에 새로운 동작을 추가하기 위해선 주로 **데코레이터 패턴**을 활용한다.
- 데코레이터 패턴을 위해선 동일한 인터페이스를 구현해야하고, 관련되지 않은 모든 동작도 하나씩 위임해줘야 한다.
- 코틀린은 언어적으로 이러한 위임을 간편히 할 수 있도록 제공해준다.

```kotlin
// 직접 구현하지 않으면 innerList로 전부 위임하도록 해준다.
class DelegatingCollection<T>(innerList: Collection<T> = ArrayList<T>()) : Collection<T> by innerList {
    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

```

### object 키워드: 클래스 선언과 동시에 인스턴스 생성(싱글톤 보장)
- object 키워드는 클래스 선언과 동시에 인스턴스를 생성하여 싱글톤을 보장해준다.
    - 주로 싱글톤, 익명 내부 클래스, 동반 객체에서 사용된다.
    
```kotlin
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
```

### 동반 객체
- 코틀린은 클래스 내부에서 static 메서드를 제공해주지 않는다.(최상위 함수와, 객체 선언이 가능해서)
- 하지만 상황에 따라 클래스 내부에 private 접근자로 접근하기 위해 클래스 내부에 구현되어야 할 필요가 있다.
- 이런경우 동반 객체를 활용하여 private 접근자에 접근이 가능하도록 할 수 있다.

```kotlin
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

fun main() {
    // 동반 객체가 구현한 인터페이스를 파라미터로 가지는 메서드에 CUser를 넘겨 다형성 활용 가능
    loadFromJSON(CUser)
}
```

### 동반 객체 확장
```kotlin
class DUser(val name: String) {
    // 확장 함수를 사용하기 위해서 빈 동반 객체 정의가 필요
    companion object
}


// 외부에서 동반 객체의 확장 함수를 구현해 관시사를 분리할 수 있다.
fun DUser.Companion.fromJSON(json: String): DUser {
    TODO()
} 
```

### 객체 식: 익명 내부 클래스
```kotlin
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
```
- `object : className() {}` 로 익명 내부 클래스를 정의할 수 있다.
- 주로 메서드에 넘겨줄 객체를 즉시 생성할 때 사용된다.
    - 이 경우 object는 싱글톤을 보장하지 않는다. 호출될 때 마다 새로운 object를 생성한다.
