# 3장. 함수 정의와 호출

### 코틀린 컬렉션
- 코틀린은 자바와의 상호 작용을 편리하게 수행하기 위해 자바의 컬렉션을 사용한다.
    - 단 코틀린은 mutable, immutable 컬렉션을 나누어 사용한다. 

### Default and Named Argument
```kotlin
fun <T> joinToString(collection: Collection<T>, separator: String = ","): String {
    val builder = StringBuilder()
    for ((index, element) in collection.withIndex()) {
        if (index > 0) builder.append(separator)
        builder.append(element)
    }
    return builder.toString()
}

fun main() {
    joinToString(separator = "|", collection = listOf(1, 2, 3))
}
```
- 아규먼트에 디폴트 값을, 전달 파라미터에 이름을 직접 명시 가능
    - named argument로 자바의 빌더를 대체할 수 있을 듯
    
#### Default Argument를 자바에도 지원하려면?
- @JvmOverloads를 붙이면 각각의 아규먼트에 맞는 오버로딩 메서드를 만들어준다.

### 최상위 함수는 어떻게 생성될까?
- 바이트코드로 변환 후 자바로 디컴파일해보면 파일명Kt라는 클래스의 static 메서드로 정의된다.

### 최상위 프로퍼티 val? const?
- 최상위 프로퍼티에 val, var 모두 사용 가능하다.
- val는 재할당이 불가능한건 맞지만 실제 호출 시 내부의 getter를 호출한다.
- 상수를 선언할 때 getter를 호출하는건 자연스럽지 못하므로 `const val NAME = "Dexter`와 같이 const를 붙여주자.

## 확장 함수와 확장 프로퍼티
### 확장 함수
- 기존에 만들어져 있던 클래스의 함수를 외부에서 추가하여 확장시키는 기법

```kotlin
// String의 확장 함수 정의. 확장이 될 대상을 **수신 객체 타입**이라고 칭하며 실제 함수가 호출된 수신 객체는 해당 함수에서 this로 참조가능
fun String.lastChar(): Char = this[this.length -1]
```

> 확장 함수는 캡슐화를 지키므로 수신 객체를 this로 참조하더라도 확장 함수에서는 접근이 제한된 대상은 접근이 불가능

### 자바에서 확장 함수 호출하기
- 확장함수는 내부적으로 수신 객체를 첫번째 인자로 갖는 static method로 정의된다.
- 그러므로 자바에서도 정적 메서드 호출로 호출할 수 있으며, static method이므로 런타임에 부가 비용이 없다. 

> **확장 함수는 정적 메서드 호출에 대한 syntatic sugar일 뿐 대단한 것이 아니다**

### 확장함수는 오버라이딩 불가
```kotlin
open class Parent
class Child : Parent()

fun Parent.hi() = println("Parent.hi")
fun Child.hi() = println("Child.hi")

fun main() {
    Child().hi() // child 호출
    val parent: Parent = Child()
    parent.hi() // parent 호출
}
```
- 내부적으로 정적 메서드로 구현되므로 오버라이딩은 불가능하기 때문에 실제 인스턴스는 Child이나 Parent의 hi가 호출된다.


### 확장 프로퍼티
```kotlin
val String.lastChar: Char
    get() = get(length - 1)

var StringBuilder.lastChar : Char
    get() = get(this.length - 1)
    set(value: Char) {
        this.setCharAt(this.length - 1, value)
    }

fun main() {
    val sb = StringBuilder("Hello World")
    println(sb.lastChar)
    sb.lastChar = 'k' // lastChar의 set 프로퍼티 호출
    println(sb.lastChar)
}
```
- 기존 클래스 객체에 필드를 추가하는게 아니라 상태를 가질 순 없고 접근자 프로퍼티를 정의하여 사용 가능 

## 컬렉션 처리
### 가변 인자 함수
```kotlin
fun print(vararg args: String) {
    for (arg in args) {
        println(arg)
    }
}

fun printArray(args: Array<String>) {
    // Array 객체를 넘길때도 *를 반드시 붙여줘야 한다.
    print(*args)
}

fun main() {
    printArray(arrayOf("1", "2"))
}
```
### 중위 함수
```kotlin
val map = hashMapOf(1 to "one", 2 to "two")
```
- 인자가 하나뿐인 일반 메서드나 확장 함수는 중위 호출이 가능하다.

### 구조 분해
```kotlin
for ((key, value) in mutableMapOf(Pair("A", 1))) {

    }

    // withIndex를 활용하면 리스트의 index도 간편히 가져올 수 있다.
    for ((index, value) in mutableListOf(1, 2, 3).withIndex()) {

    }
}
```
- map을 key, value로 구조분해, list를 withIndex로 호출하여 index, value로 구조분해 하는등의 방식을 활용 가능.

## 문자열 및 정규식 다루기
```kotlin

fun regex() {
    // 명시적으로 정규 표현식을 표현
    val regex = "\\d\\d".toRegex()

    // 삼중 따옴표는 역슬래쉬를 두번써 이스케이핑이 필요 없다.
    val regex2 = """\d\d""".toRegex()
}
```
