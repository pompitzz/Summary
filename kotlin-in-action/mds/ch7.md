# 7장. 연산자 오버로딩과 기타 관례
## 산술 연산자 오버로딩
```kotlin
/**
 * a * b = times
 * a / b = div
 * a % b = mod(rem)
 * a + b = plus
 * a - b = minus
 *
 * 연산자 우선순위는 숫자 형태와 동일하게 적용된다.
 */
data class Point(val x: Int, val y: Int) {
    // 연산자 오버로딩은 opertator가 필수이다.
    operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)
    
    // 연산자 오버로딩도 오버로딩이 가능하므로 다양하게 만들 수 있다.
    operator fun plus(number: Int) = Point(x + number, y + number)
}

// 확장 함수로도 가능하다.
operator fun Point.minus(other: Point) = Point(x - other.x, y - other. y)

// 연산자 오버로딩 파라미터가 꼭 같을 필요가 없다.
operator fun Point.times(scale: Double) = Point((x * scale).toInt(), (y * scale).toInt())

// 교환 법칙은 성립되지 않으므로 이렇게 반대로 사용하기 위해 직접 정의해줘아 한다.
operator fun Double.times(p: Point) = Point((p.x * this).toInt(), (p.y * this).toInt())

fun main() {
    val p1 = Point(10, 20)
    val p2 = Point(30, 70)
    println(p1 + p2)
    println(p2 - p1)
    println((p2 + p1) * 0.3)
    println(0.3 * (p2 + p1))
}
```

### 복합 대입 연산자 오버로딩
- +=와 같은 복합 대입 연산자는 연산 후 참조를 그대로 유지하고 싶을떄 유용하다.
```kotlin
// 현재 리스트를 그대로 유지하면서 복합 연산자 오버로딩을 수행한다.
operator fun <T> MutableCollection<T>.plusAssign(element: T) {
    this.add(element)
}

fun main() {
    val numbers = arrayListOf<Int>()
    numbers += 1
    println(numbers[0])
    // 사실 +=는 plus, plusAssign 모두 컴파일 가능하다. 그러므로 둘 중하나만 정의해야 한다.
    // 빌더와 같이 변경 가능한 클래스라면 plusAssign, 그렇지 않고 불변 객체라면 plus를 제공해주자.
    numbers += 2
    println(numbers[1])
}
```

#### 코틀린 표준 라이브러리의 연산자 오버로딩 규칙
- +, -는 항상 새로운 컬렉션을 반환한다.
- 변경 가능한 컬렉션에서의 +=, -=는 메모리에 있는 객체 상태를 변경시킨다.
- 읽기 전용 컬렉션의 경우 복사본을 반환한다.

### 단항 연산자 오버로딩
```kotlin
/** 단항 연산자 (함수 파라미터가 없다)
 *  +a = unaryPlus
 *  -a = unaryMinus
 *  !a = not
 *  ++a, a++ = inc (표현은 같지만 실행 시점이 다름)
 *  --a, a-- = dec (표현은 같지만 실행 시점이 다름)
 */
operator fun Point.unaryMinus() = Point(-x, -y)

fun main() {
    val p1 = Point(10, 20)
    println(-p1)
}
```

## 비교 연산자 오버로딩
```kotlin
/** 동등 비교 연산자
 * a == b -> a?.equals(b) ?: (b == null)
 * - 동등성 검사는 null 검사도 하기때문에 null도 동등성 검사가 가능해진다.
 * - != 는 ==의 반대 결과 값을 반환해준다.
 * - equals도 Any를 확인해보면 연산자 오버로딩을 활용한 것이다.
 */


/** 순서 연산자
 *  자바의 Comparable에 들어있는 메서드를 관례로 사용한다.
 *  a >= b -> a.compareTo(b) >= 0
 */

class Person(
    val firstName: String, val lastName: String
) : Comparable<Person> {
    override fun compareTo(other: Person): Int {
        // 인자로 받은 함수를 차례로 호출하면서 값을 비교해줌
        return compareValuesBy(this, other, Person::lastName, Person::firstName)
    }
}

fun main() {
    // Comparable을 연산자 오버로딩으로 사용하기 때문에 기본 JAVA 클래스가 Comparable을 구현했다면 연산자 오버로딩을 사용할 수 있다.
    println("asd" < "csd")
}
```

## 3. 컬렉션과 범위에 대해 쓸 수 있는 관례
- 사용자 지정 클래스에도 []로 값을 세팅, 가져오는 것과 in 연산자로 원소를 확인하는 기능을 구현할 수 있다.

### get, set 관례
```kotlin
// get 관례는 [index or key]와 같이 접근을 가능하게 해준다.
operator fun Point.get(index: Int): Int {
    return when(index) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException("Invalid")
    }
}

data class MutablePoint(var x: Int, var y: Int)

// set 또한 관례가 존재하며 p[index] = value로 값을 세팅할 수 있다.
// 마지막 파라미터만 연산자의 우항, 나머지는 연산자의 좌항의 []안에 들어가게 된다.
// 즉 x[a, b] = c -> x.set(a, b, c) 이런식으로 사용할 수 있다.
operator fun MutablePoint.set(index: Int, value: Int) {
    when (index) {
        0 -> x = value
        1 -> y = value
        else -> throw IndexOutOfBoundsException("Invalid")
    }
}

fun main() {
    val point = Point(10, 20)
    println(point[0])
    println(point[1])

    val mutablePoint = MutablePoint(10, 20)
    println(mutablePoint)
    mutablePoint[0] = 30
    println(mutablePoint)
}
```

### in 관례

```kotlin
data class Rectangle(val upperLeft: Point, val lowerRight: Point)

// in 연산자는 comtains와 대응된다.
operator fun Rectangle.contains(p: Point): Boolean =
    // until은 x <= value < y 인 범위를 의미한다.
    // ..은 x  <= value <= y를 의미힌다.
    p.x in upperLeft.x until lowerRight.x && p.y in (upperLeft.y until lowerRight.y)

fun main() {
    val p1 = Point(10, 20)
    val p2 = Point(50, 50)
    val rectangle = Rectangle(p1, p2)

    // in의 좌항이 해당 함수의 파라미터가 되고 우항은 contains를 소유한 객체이다.
    println(Point(20, 30) in rectangle)
}
```

### rangeTo 관례
```kotlin
/** start..end -> start.rangeTo(end)
 *  - rangeTo는 함수의 **범위를 반환**
 *  - Comparable 인터페이스를 구현하고 있으면 rangeTo를 정의할 필요가 없다.
 *  - 코틀린 표준 라이브러리를 통해 비교 가능한 원소로 만들 수 있다.
 *      - operator fun <T: Comparable<T>> T.rangeTo(that: T): ClosedRange<T>
 */

fun main() {
    val now = LocalDate.now()

    // now.range(now.plusDays(10)) 이 된다.
    val vacation = now..now.plusDays(10)
    println(now.plusDays(3) in vacation)
}
```

### for 루프 iterator 관례
- for 루프에서 사용하는 in은 iterator를 호출해 hasNext, next 호출을 반복하는 식으로 변환된다.

```kotlin
// range에 대한 for문을 돌리기 때문에 ClosedRange<>.iterator를 정의한다.
operator fun ClosedRange<LocalDate>.iterator() : Iterator<LocalDate> =
    object : Iterator<LocalDate> {
        var current = start

        override fun hasNext() = current <= endInclusive

        override fun next() = current.apply {
            current = current.plusDays(1)
        }
    }

fun main() {
    // iterator 메서드를 확장 함수로 정의할 수 있기 떄문에 자바 문자열에 대한 for 루프가 가능해진다.
    for(c in "asd") {
        println(c)
    }
    
    val now = LocalDate.now()
    // 위에서 정의한 iterator를 이용해 for문을 돌릴 수 있다.
    for (date in (now .. now.plusDays(10))) {
        println(date)
    }
}
```

## 4. 구조 분해 선언과 component 함수
```kotlin
/** 구조 분해 관례
 *  - val (a, b) = p --> a = p.component1(), b = p.component2()
 *  - data 클래스는 주 생성자 들에 들어 있는 프로퍼티에 대해서는 자동으로 컴파일러가 componentN을 만들어 준다.
 *  - 구조분해는 이터레이터와 함께 루프문에서 매우 유용하다.
 */

// 일반 클래스에는 직접 정의할 수 있다.
class NewPoint(val x: Int, val y: Int) {
    operator fun component1() = x
    operator fun component2() = y
    // (x, y) = NewPoint(1, 2) 이렇게 가능
}

data class NamedComponents(val name: String, val extension: String)

fun splitFilename(fullName: String): NamedComponents {
    val result = fullName.split('.', limit = 2)
    return NamedComponents(result[0], result[1])
}

fun splitFilename2(fullName: String): NamedComponents {
    // 배열도 component를 제공하므로 이렇게 써도 된다.
    // 코틀린 표준 라이브러리에서는 맨 앞의 다섯 원소에 대해 componentN을 제공한다.
    val (name, extesion) = fullName.split('.', limit = 2)
    return NamedComponents(name, extesion)
}

fun main() {
    // 구조 분해를 활용하면 값을 간단히 풀어서 가져올 수 있다.
    val (name, extension) = splitFilename("helloWorld.kt")
    println("$name.$extension")
}
```

## 5. 프로퍼티 접근자 로직 재활용: 위임 프로퍼티
- 위임 프로퍼티를 사용하면 값을 단순히 backing field에 저장하는것 보다 더 복잡한 방식으로 작동하는 프로퍼티를 구현할 수 있다.
    - 예를들어 프로퍼티 위임을 통해 자신의 값을 필드가 아닌 DB 테이블, 세션, 맵등에 저장이 가능하다.
- 이는 도우미 객체인 위임 객체가 필요하다.

```kotlin
class Foo {
    // 이렇게 위임을 설정하면
    var p: Type by Delegate()
    
    // 아래와 같이 구성될 것이다.
    // 실제 set, get은 delegate로 위임되어 로직이 수행될 것이다.
    private val delegate = Delegate()
    var p: Type
    set(value: Type) = delegate.setValue(..., value)
    get() = delegate.getValue(...)
}
```

#### 지연 초기화를 백킹 필드를 통해 구현
```kotlin
/** 이러한 패턴은 매우 자주 사용된다.
 * - 뒷받침하는 프로퍼티를 이용해 데이터를 지연 초기화 하는 기법이다.
 * - 하지만 이 방식은 스레드 안전하지 않고 프로퍼티가 많을 수록 귀찮아 질 것이다.
 */
class Person3(val name: String) {
    private var _emails: List<String>? = null
    val emails: List<String>
    get() {
        // emails은 딱 한번만 가져온다.
        if (_emails == null) {
            _emails = loadEmails(this)
        }
        return _emails!!
    }
}
```

#### 위임 프로퍼티 활용: by lazy()를 사용한 프로퍼티 초기화 지연
```kotlin
class Person4(val name: String) {
    /** 위임 프로퍼티는 백킹 필드와 값이 오직 한번만 초기화 됨을 보장하는 get 로직을 함께 캡슐화 해준다.
     *  - lazy 함수가 위임 객체를 반환하는 표준 라이브러리이다.
     *  - lazy 함수는 코틀린 관례에 맞는 getValue 메서드가 들어있는 객체를 반환해준다.
     **/
    val emails by lazy { loadEmails(this) }
}
```

### 위임 프로퍼티 구현해보기
- 위임 프로퍼티 없이 프로퍼티 변경을 리스너에 통지해주는 기능을 구현하고 리팩터링 한다.

#### 기본 자바 빈즈를 이용해 구현
```kotlin
// 리스너의 목록을 관리하고 이벤트가 들어오면 통지한다
open class PropertyChangeAware {
    protected val changeSupport = PropertyChangeSupport(this)

    fun addPropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.addPropertyChangeListener(listener)
    }

    fun removePropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.removePropertyChangeListener(listener)
    }
}

class MyPerson(
    val name: String, age: Int, salaray: Int
) : PropertyChangeAware() {
    var age: Int = age
        set(value) {
            val oldValue = field
            field = value
            changeSupport.firePropertyChange("age", oldValue, value)
        }

    var salary: Int = salaray
        set(value) {
            val oldValue = field
            field = value
            changeSupport.firePropertyChange("salary", oldValue, value)
        }
}

fun main() {
    val myPerson = MyPerson("Jayden", 26, 1234)
    myPerson.addPropertyChangeListener(
        PropertyChangeListener { event ->
            println("Property ${event.propertyName} changed from ${event.oldValue} to ${event.newValue}")
        }
    )

    myPerson.age = 30
    myPerson.salary = 5500
}
```

#### 공통 모듈을 뽑아내서 재사용성 증가 시키기
```kotlin
class ObservableProperty(
    private val propName: String, private var propValue: Int,
    private val changeSupport: PropertyChangeSupport
) {
    fun getValue(): Int = propValue
    fun setValue(newValue: Int) {
        val oldValue = propValue
        propValue = newValue
        changeSupport.firePropertyChange(propName, oldValue, newValue)
    }
}

class MyPerson(
    val name: String, age: Int, salaray: Int
) : PropertyChangeAware() {
    val _age = ObservableProperty("age", age, changeSupport)
    var age: Int
        get() = _age.getValue()
        set(value) = _age.setValue(value)

    val _salary = ObservableProperty("salary", salaray, changeSupport)
    var salary: Int
        get() = _salary.getValue()
        set(value) = _salary.setValue(value)
}
```
- 도우미 클래스를 통해 get, set을 위힘하여 변경을 통지하도록 할 수 있다.

#### 위임 프로퍼티 구현 후 적용
```kotlin
class ObservableProperty(
    private var propValue: Int, private val changeSupport: PropertyChangeSupport
) {
    // 위임을 위해 코틀린 관례에 맞게 operator를 붙이고, 해당 객체를 프로퍼티를 넘겨줘야 한다.
    operator fun getValue(p: MyPerson, prop: KProperty<*>): Int = propValue
    operator fun setValue(p: MyPerson, prop: KProperty<*>, newValue: Int) {
        val oldValue = propValue
        propValue = newValue
        changeSupport.firePropertyChange(prop.name, oldValue, newValue)
    }
}

class MyPerson(
    age: Int, salary: Int
) : PropertyChangeAware() {
    // by 오른쪽의 객체를 **위임 객체**라고 부른다.
    // 코틀린은 위임 객체를 감춰진 프로퍼티에 저장하고, 주 객체의 프로퍼티를 읽거나 쓸때마다 위임 객체의 getValue, setValue를 호출해준다.
    var age: Int by ObservableProperty(age, changeSupport)
    var salary: Int by ObservableProperty(salary, changeSupport)
}
```

#### 코틀린 지원 라이브러리로 위임 객체 만들기
```kotlin
class MyPerson(
    age: Int, salary: Int
) : PropertyChangeAware() {
    /** observer를 정의하고 코틀린 위임 객체에 넘겨주면 된다.
     *  - 사실 위임 객체의 방법은 보이지 않는 접근자들을 만들어 주는 것이다.
     *  - get() -> <delegate>.getValue(v, <property>)
     *  - set() -> <delegate>.setValue(c, <property>, x)
     *  - 단순한 방법이지만 프로퍼티가 저장될 값을 맵, 디비 등으로 바꿀 수 있고, 프로퍼티를 읽거나 쓸 때 이벤트들을 추가하는 방식들을 간결하게 구현할 수 있다.
     */
      
    private val observer = {
        prop: KProperty<*>, oldValue: Int, newValue: Int -> changeSupport.firePropertyChange(prop.name, oldValue, newValue)
    }

    var age: Int by Delegates.observable(age, observer)
    var salary: Int by Delegates.observable(salary, observer)
}
```
