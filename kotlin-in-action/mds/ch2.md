# 2장. 코틀린 기초
- 코틀린은 루프를 제외하고 대부분 `식(expression)`으로 구성됨
- expression은 값을 만들지만 statement는 블럭의 최상위 요소로 존재할 뿐 값을 만들지 않는다.

### 함수와 변수
```kotlin
// 함수는 fun으로 정의하며 블럭문이 아닌 식으로도 정의 가능
fun hello() = println("Hello")

// val은 java final과 동일하게 재할당이 불가능
val name1: String = "Dexter"

// var는 재할당 가능
var name2: String = "Dexter" 
```

```kotlin
// if문도 expression으로 사용 가능
fun max(a: Int, b: Int) = if (a > b) a else b
```
- expression을 함수의 본문으로 구성하여 간결하게 표현가능

### 굳이 변수를 뒤에 선언하는 이유?
- 변수를 뒤에 지정하게 되면 `타입 지정을 생략할 수 있게 해준다.`

### 문자열 템플릿
```kotlin
val name = "Dexter"

// 자바에 비해 훨씬 더 간편하게 문자열을 다룰 수 있다.
println("Hello $name")
println("Hello ${name}")
```

### 클래스
```kotlin
class Person(
    // val은 읽기 전용으로 비공개 필드와 getter 제공
    val name: String,
    // var은 변경 가능하므로 비공개 필드와 getter, setter 제
    var age: Integer,
)

val person = Person("Dexter", 26)
println(person.name) // 프로퍼티로 직접 접근하면 게터가 호출된다.
println(person.age)
```

### 커스텀 접근자
```kotlin
class Rectangle(val height: Int, val width: Int) {
    // 커스텀 접근자를 지정할 수 있다.
    val isSquare: Boolean 
        get() = height == width
}
```

### enum과 when
```kotlin
enum class Color {
    RED,
    ORANGE,
    YELLOW
    ;
}

// java 스위치랑 비슷
fun getStringColor(color: Color) =
        when (color) {
            Color.RED -> "RED" 
            Color.ORANGE -> "ORANGE" 
            Color.YELLOW -> "YELLOW" 
        }

fun getStringColor2(color: Color) =
        when (color) {
            Color.RED, Color.ORANGE, Color.YELLOW -> "COLOR"
        }

fun getStringColor3(color1: Color, color2: Color) = 
        when {
            (color1 == Color.RED || color2 == Color.ORANGE) -> "RED ORANGE"
            else -> throw RuntimeException()
        }
```

### 스마트 캐스팅
- 타입검사와 동시에 형변환을 하도록하여 스마트 캐스팅 지원
```kotlin
interface Expr
class Num(val value: Int) : Expr
class Sum(var left: Expr, val right: Expr) : Expr

// 스마트 캐스팅을 지원한다.
fun eval(e: Expr): Int =
        when(e) {
            is Num -> e.value
            is Sum -> eval(e.left) + eval(e.right)
            else -> throw IllegalArgumentException()
        }

/**
 *  클래스의 프로퍼티를 스마트 캐스팅하고 싶다면 val이면서 커스텀 접근자가 정의되어 있지 않아야 한다.
 *  - var거나 커스텀 접근자가 있으면 언제가 같은 타입을 반환해준다는 것을 확신할 수 없기 때문에.. 
  */
fun main() {
    val sum = Sum(Num(1), Num(2))
    if (sum.left is Num) {
    //  println(sum.left.value) 컴파일 에러 (left는 var이다)
    }
    // 스마트 캐스팅 가능(rifht는 val이기 때문)
    if (sum.right is Num) { 
        println(sum.right.value)
    }
}
```

### expression when, if
```kotlin
fun expressionWhen(e: Expr): Int =
        when(e) {
            is Num -> {
                println(e.value)
                e.value // 표현식의 블럭문은 마지막 값이 리턴 값이 된다.
            }
            is Sum -> {
                println("${e.left} + ${e.right}")
                expressionWhen(e.left) + expressionWhen(e.right)
            }
            else -> throw IllegalArgumentException()
        }

// if절도 가능하지만 when이 더 깔끔해보인다.
fun expressionIf(e: Expr): Int =
        if (e is Num) {
            println(e.value)
            e.value // 표현식의 블럭문은 마지막 값이 리턴 값이 된다.
        } else if (e is Sum) {
            println("${e.left} + ${e.right}")
            expressionIf(e.left) + expressionIf(e.right)
        } else {
            throw IllegalArgumentException()
        }
```

### 이터레이션
```kotlin
fun iterationEx() {
    // 1~10 출력
    for (i in 1..10) {
    }

    for (i in 1..10 step 2) {
    }

    // 10에서 1까지 2 칸씩
    for (i in 10 downTo 1 step 2) {
        print("$i, ")
    }

    // map의 key, value를 for문으로 풀어낼 수 있다.
    for ((key, value) in mutableMapOf(Pair("A", 1))) {

    }

    // withIndex를 활용하면 리스트의 index도 간편히 가져올 수 있다.
    for ((index, value) in mutableListOf(1, 2, 3).withIndex()) {

    }
}
```

### in으로 범위 검사
```kotlin

fun isSmallLetter(c: Char) = c in 'a'..'z' // 컴파일 -> 'a'<= c && c <= 'z'
fun isNotSmallLetter(c: Char) = c !in 'a'..'z'

fun regognize(c: Char): String =
        when (c) { // when절에서도 in 검증 방식 사용 가능
            in 'a'..'z' -> "is small letter"
            else -> "is not small letter"
        }
```

### 코틀린은 모두 언체크 예외로 이루어져 있다.
