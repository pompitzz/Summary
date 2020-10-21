# 8장. 고차 함수: 파라미터와 반환 값으로 람다 사용
- 코틀린은 함수 타입과 고차 함수를 지원한다.

## 1. 고차 함수 정의
- 고차 함수는 다른 함수를 인자로 받거나 함수를 반환하는 함수이다.
- 그러므로 filter, map, with 등이 모두 고차 함수로 볼 수 있다.

### 함수 타입
```kotlin
fun main() {
    // 함수도 타입이 존재한다. 타입을 명시하면 실제 람다식은 타입 추론이 가능하다.
    val sum: (Int, Int) -> Int = { x, y -> x + y }
    // nullable도 가능
    val sumNullable: (Int, Int) -> Int? = { x, y -> x + y }

    val actions: () -> Unit = { println(123) }
    // 함수 전체에 대해서도 nullable을 정의할 수 있다.
    // canReturnNull이 아닌 funOrNull을 위해서는 꼭 괄호가 필요하다.
    val actionsNullable: (() -> Unit)? = { println(123) }


    fun performRequest(
        url: String,
        // 가독성을 위해 파라미터 이름을 정의할 수 있다.
        callback: (code: Int, contnet: String) -> Unit
    ) {
        callback(1, "Hello")
    }
    performRequest("url") { code, contnet -> //TODO()
    }

    // 함수 타입만 미리 정해놓을 수도 있다.
    fun toAndThree(operation: (Int, Int) -> Int) {
        val result = operation(2, 3)
        println("The result is $result")
    }
}
```
#### IntelliJ IDEA 팁
- 인텔리에는 디버깅할 때 람다 코드 내부를 한 단계씩 실행해볼 수 잇는 스마트 스테핑을 제공한다.


### 자바에서 코틀린 함수 타입
- 컴파일된 코드 안에서 함수 타입은 일반 인터페이스로 변경된다.
- 즉 함수타입의 변수는 FunctionN 인터페이스를 구현하는 객체를 저장한다.
- 각 인터페이스는 invoke 메서드 정의가 하나 들어있다.
- 그러므로 자바에서도 코틀린 함수타입을 호출할 수 있게 된다.

 
### 파라미터 디폴트 값을 활용하여 null이 가능한 함수를 만들고 이를 안전하게 호출하기
```kotlin
fun <T> Collection<T>.joinToString(
    separator: String = ", ",
    prefix: String = "",
    suffix: String = "",
    transform: ((T) -> String)? = null
): String {
    val list = this
    return buildString {
        append(suffix)
        for ((index, element) in list.withIndex()) {
            if (index > 0) append(separator)
            // 람다식은 invoke 함수를 가지는 인터페이스임을 활용하여 null check를 사용할 수 있다.
            val str = transform?.invoke(element) ?: element.toString()
            append(str)
        }
        append(suffix)
    }
}
```

### 함수를 반환하는 함수 만들기
```kotlin
data class Person(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?
)


fun getPredicate(prefix: String = "", onlyWithPhoneNumber: Boolean = false): (Person) -> Boolean {
    val startsWithPrefix = { p: Person -> p.firstName.startsWith(prefix) || p.lastName.startsWith(prefix) }
    if (!onlyWithPhoneNumber) {
        return startsWithPrefix
    }
    // 함수 반환 타입으로 유추가 가능하므로 람다식을 반활할때도 it을 활용할 수 있다. 
    return { startsWithPrefix(it) && it.phoneNumber != null }
}
```

## 2. 인라인 함수: 람다의 부가 비용 없애기
- 코틀린이 보통 람다를 무명 클래스로 컴파일하지만 그렇다고 람다 식을 사용할 때마다 새로운 클래스가 만들지 않는다.
- 대신 람다가 변수를 포획하면 람다가 생성되는 시점마다 새로운 익명 클래스 객체가 생긴다.
- 이런 경우에는 실행 시점에 익명 클래스 생성에 대한 부가 비용이 발생하여 성능저하가 일어날 수 있다. 
- **inline 변경자를 활용하면 컴파일러는 그 함수를 호출하는 모든 문장을 함수 본문에 해당하는 바이트 코드로 바꿔치기 해준다!**

### 인라이닝이 작동하는 방식
- 인라인을 활용하면 함수를 호출하는 바이트코드 대신 함수 본문을 번역한 바이트코드로 컴파일될 것이다.

```kotlin
// 이 함수는 inline을 사용했으므로 자바의 synchronized 문과 같을 것이다.
inline fun<T> synchronized(lock: Lock, action: () -> T): T {
    lock.lock()
    try {
        return action()
    } finally {
        lock.unlock()
    }
}

fun foo(l: Lock) {
    println("Before sync")
    
    // 이 함수는 바이트코드상으로 함수 본문이 foo와 함께 컴파일 될 것이다.
    synchronized(l) {
        println("Action")
    }
    println("AfterSync")
}
```

> 한 인라임 함수를 여러곳에서 사용하면 각 호출하는 곳에 각각 복사하므로 바이트코드가 거대해질 수 있다.

### 인라인 함수의 한계
- 모든 람다식을 인라이닝할 수 없다.
- 인라이닝은 람다식을 본문에 직접 펼치기 때문에 상황에 따라 이러한 방식이 불가능 할때가 존재한다.
- 예를들어 함수의 파라미터에 람다식이 있었다. 그때 그 람다식을 바로 호출한다면 쉽게 인라이닝이 가능하다.
- **하지만 파라미터로 받은 람다식을 다른 변수에 저장한다면 그 변수는 객체의 참조를 가져야 한다.**
- 이 경우엔 함수의 본문에 인라이닝이 불가능할 것이다. 왜냐하면 그 객체의 참조를 가지기 위해서는 어딘가에 존재해야하기 때문이다.
    - 이러한 상황에 대비하여 의도적으로 인라이닝을 하지 못하도록 notinline 변경자를 붙여 인라인을 금지시킬 수 있다.

> 즉 인라인 함수의 본문에서 람다 식을 바로 호출하거나, 해당 람다 식을 인자로 전달받아 바로 호출하는 경우 그 람다를 인라이닝할 수 있다.

### 컬렉션 연산 인라이닝
- 컬렉션의 filter 같음 함수는 인라인 함수이기 때문에 filter 함수의 바이트코드는 그 함수에 전달된 람다 본문의 바이트코드와 함께 filter를 호출한 위치에 들어가게 된다.
- 그렇기 때문에 직접 if문을 돌리는거와 바이트코드는 거의 동일하여 성능은 차이가 없다.

### 시퀀스와 인라이닝
- 시퀀스는 지연 계산을 해야하기 때문에 filter와 같은 함수를 객체로 가지고 있어야 하기 때문에 인라이닝할 수 없다.
- **그러므로 지연 계산을 지원하는 시퀀스가 기본 컬렉션 함수보다 성능이 항상 좋은건 아니다. 오직 지연 계산의 이점이 필요할때만 성능이 좋다**

### 함수를 인라인으로 선언해야할 때
- 일반 함수 호출의 경우 JVM은 이미 강력하게 인라이닝을 지원해준다.
    - JVM은 코드 실행을 분석해서 가장 이익이 되는 방향으로 호출을 인라이닝한다.
    - 이러한 과정은 바이트코드를 실제 기계어 코드로 번역하는 과정(JIT)에서 일어난다.
    - JVM의 최적화를 활용하면 바이트코드에서는 각 함수 구현이 정확히 한 번만 있으면되고 그함수를 호출하는 부분에서 따로 함수 코드를 중복할 필요가 없다.
    - **반면 코틀린의 인라이닝은 바이트코드에서 각 함수 호출 지점을 함수 본문으로 대치하기 때문에 코드 중복이 생긴다.**
    - 그리고 스택트레이스도 직접 호출하는 것이 더 깔끔하다.
- 반면 람다를 인자로 받는 함수를 인라이닝하면 이익이 더 많다.
    - 첫째로 인라이닝을 통해 없앨 수 있는 부가 비용이 상당하다.
        - 함수 호출 비용을 줄일 수 있을 뿐만 아니라, 람다를 표현하는 클래스와 람다 인스턴스에 해당하는 객체를 만들 필요도 없어진다.
    - 둘로 현재의 JVM은 함수 호출과 람다를 인라이닝해 줄 정도로 똑똑하진 못하다.
    - 셋째로일반 람다로는 하지 못하는 추가 기능들도 존재한다.(non-local 리턴)
- 하지만 inline 함수는 코드 크기에 주의를 기울여야 한다. 
    - 인라인 함수는 해당 함수의 본문에 해당하는 바이트코드를 호출 지점에 복사하기 때문에 인라인 함수의 코드 크기가 크다면 바이트코드가 상대적으로 매우 커질 수 있다.
    - 코틀린 기본 지원 inline 함수들을 보면 모두 크기가 아주 작다는 사실을 알 수 있다.

```kotlin
fun main() {
    // Closeable을 구현체들은 use를 사용할 수 있다.
    // 코틀린은 확장함수로 람다를 활용하여 try-with-resource같은 기능이 필요 없이 확장 함수로 이를 제공해준다
    // 이또한 인라인함수 이므로 doRun은 실제로 main 함수에서 실행될 것이다.
    // 그리고 만약 **use 람다 식안에서 return을 하게되면 nonlocal이기 때문에 main 함수 자체에 대한 return이 된다.**
    BufferedReader(FileReader(""))
        .use { doRun(it) }
}

fun doRun(bufferedReader: BufferedReader): List<String> {
    return listOf(bufferedReader.readLine())
}
```

## 3. 고차 함수 안에서 흐름 제어
### 인라인 함수라서 가능한 non-local return
```kotlin
fun lookForAlice(people: List<Person2>) {
    // 일반적인 for문에서는 발견즉시 for문을 종료하여 함수를 리턴할 수 있다.
    for (person in people) {
        if (person.name == "Alice") {
            println("Found")
            return
        }
    }
    println("Alice is not found")

    // 자바와 다르게 forEach에서도 for문과 같이 return이 가능해진다.
    // 여기서 return은 lookForAlice 함수의 return이 된다.
    // 이렇게 자신의 상위 스코프의 블록을 반환하게 만드는 것을 non-local return이라고 부른다.
    // 이건 inline 함수의 특징때문에 가능한 것이기 때문에 inline 함수만 적용된다.
    people.forEach {
        if (it.name == "Alice") {
            println("Found")
            return
        }
    }
    println("Alice is not found")
}
```

### 레이블을 활용한 로컬 return
```kotlin
fun lookForAlice2(people: List<Person2>) {
    // 로컬 리턴은 for 루프의 break과 비슷한 역할을 수행해준다.
    people.forEach label@{
        if (it.name === "Alice") return@label
    }
    println("Alice might be somewhere")

    people.forEach{
        // 로컬 리턴을 함수 이름을 통해 할 수도 있다.
        if (it.name === "Alice") return@forEach
    }
}

```

> 람다식의 레이블을 명시하면 함수 명을 활용할 수 없다. 그리고 람다 식에는 레이블이 2개 이상 붙을 수 없다.

```kotlin
fun main2() {
    // 레이블은 this에도 적용된다.
    // 레이블을 걸면 일반 this는 외부 참조를 가지게 될 것이다.
    StringBuffer().apply sb@{ 
        this@sb.append("Hello World")
    }
}
```
- local 반환문은 장황하고, 람다 안에 여러 위치에 return@label이 필요해지면 사용이 불편한다.
- 그래서 코틀린은 코드 블록을 여기저기 전달하기 위한 다른 해법을 제공하며 그것이 바로 익명 함수이다.

#### 익명함수는 기본적으로 로컬 return이다.
```kotlin
fun lookForAlice3(people: List<Person2>) {
    people.forEach(fun(person) {
        // return은 익명 함수를 지칭한다.
        if (person.name == "Alice") return
        println("${person.name} is not Alice")
    })

    // 반환 타입은 명시해줘야 한다.
    people.filter(fun(person): Boolean { return person.age < 30 })
    
    // 식이 본문인 함수를 활용하면 리턴타입이 및 명시적 리턴이 필요없다.
    people.filter(fun(person) = person.age < 30)
}
```
- 익명함수는 일반 함수와 동일하나 함수 이름이나 파라미터 타입을 생략할 수 있는 차이일 뿐이다.

> 람다 함수는 기본적으로 논로컬 리턴을 특징으로 하고 익명 함수는 자기 자신을 로컬 리턴을 특징으로 한다.
> - 람다는 label을 통해 로컬 리턴이 가능하지만 익명 함수는 논로컬 리턴이 불가능하다.
> - 익명 함수는 일반 함수와 같아보이지만 사실 람다 식의 문법적 편의일 뿐이다.  
