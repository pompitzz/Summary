# 9장. 제네릭스
## 제네릭 타입 파라미터
- **자바와 다르게 코틀린은 raw 타입을 허용하지 않으므로 제네릭 타입의 타입인자를 컴파일러가 알 수 있게 해줘야 한다.**
- 제네릭의 기본적인 특징은 자바랑 대부분 동일하다.

```kotlin
// 타입 파라미터에 여러 제약을 가할 수 있다.
// T 타입은 반드시 CharSequence와 Appendable을 구현한 구현체여야 한다.
fun <T> ensureTrailingRerioid(seq: T)
        where T : CharSequence, T : Appendable {
    if (!seq.endsWith('.')) {
        seq.append('.')
    }
}

// 타입파라미터는 nullable하므로 null 불가능하게 막을 수 있다.
fun <T: Any> test(t: T): Nothing = TODO()
```

## 런타임 제네릭스의 동작
- JVM의 제네릭스는 타입 소거를 사용하므로 실제 바이트 코드엔 타입이 제거된 정보만 들어가있다.
- inline 함수와 reified 키워드를 활용하면 타입 인자가 지워지지 않고 게속해서 존재하도록 할 수 있다.

### 런타임의 제네릭: 타임 검사와 캐스트
- 코틀린도 자바와 동일하게 런타임엔 타입 파라미터 정보는 제거딘다.
- 런타임에 타입 정보가 제거되므로 런타임에 제네릭 타입에 대한 검사는 불가능하다.
- 즉 타입을 검사할 때`stringList is List`와 같이 제네릭 타입을 제외하고 검사를 하여야하지만 **코틀린은 제네릭 클래스에 반드시 제네릭 타입을 명시해야 한다.**
- `stringList is List<*>`와 같이 star projection을 활용할 수 있지만, 런타임에 타입 정보를 알수있도록 inline, reified를 활용하면 `stringList is List<String>`와 같이 타입 검사가 가능하다.
    
### 실체화한 타입 파라미터를 사용한 함수 선언
- 제네릭 클래스건 함수건 런타임엔 타입이 소거되어 확인이 불가능하지만, 인라인 함수의 타입 파라미터는 실체화시킬 수 있다.

```kotlin
// inline 함수와 reified를 활용하면 실체화된 타입으로 취급할 수 있다.
inline fun <reified T> isA(value: Any) = value is T

fun main() {
    println(isA<String>(1)) // false
    println(isA<String>("1")) // true

    // 실체화된 타입을 활용할 수 있는 예(원하는 타입의 원소만 가져옴)
    val items = listOf(1, "2", 3)
    println(items.filterIsInstance<Int>())
}
```

#### 왜 인라인 함수에서만 실체화된 타입을 쓸 수 있을까?
- 컴파일러는 인라인 함수의 바이트코드를 해당 함수를 호출한 모든 곳에 복사하여 삽입하는데 이 때 컴파일러는 실체화된 타입 인자를 통해 함수를 호출하는 곳에서의 정확한 타입을 알 수 있게 해준다.
    - **자바에서는 inline 함수도 보통의 함수처럼 호출하므로 reified를 사용하는 inline 함수를 호출할 수 없다.**
- 실체화한 타입 파라미터가 있는 함수는 타입 인자를 바이트코드에 넣기 위해 일반 함수보다 더 많은 작업 필요하고 이를 위해 반드시 inline이 가능해야 한다.

> 인라인 함수는 함수를 함수 파라미터로 가지는 등 성능에 효율적일 때 사용할 수도 있지만 위와 같이 실체화한 타입을 사용하기 위해 사용할 수도 있다.

### 실체화한 타입으로 클래스 참조를 대신하기
```kotlin
// class에 대한 정보를 파라미터로 받음
fun <T> printClass(clazz: Class<T>) = println(clazz)

// 실체화를 통해 타입 정보를 타입 파라미터로 받아서 활용할 수 있음
inline fun <reified T> printClassUsingReified() = printClass(T::class.java)

fun main() {
    printClass(Integer::class.java)
    printClassUsingReified<Integer>()
}
```

> 실체화한 타입 파라미터는 타입 검사, 리플렉션 등으로 사용할 순 있지만 해당 인스턴스를 생성하거나, 동반 객체 메서드를 호출하는 등의 작업은 불가능하다.


## 변성(variance): 제네릭과 하위 타입
- 변성(공변성, 무공변성, 반공변성)은 제네릭 타입의 기저 타입은 동일하나 타입 파라미터가 다를 때 서로 어떤 관게를 가지는지에 대한 개념으로 제네릭을 제대로 활용하기 위해 꼭 필요한 개념이다.

### 변성이 있는 이유: 인자를 함수에 넘기기
```kotlin
fun addContent(list: MutableList<Any>) {
    list.add(1)
}

fun main() {
    val strings = mutableListOf("a")
    // String은 Any의 하위타입이므로 List<Any>를 받는 함수 파라미터에 List<String>을 넘겨줄 수 있을거 같지만 
    // 실제 addContent 함수에서 처럼 구현되어 런타임 에러가 발생하여 타입 안전하지 못하므로 컴파일을 할 수 없다.
    addContent(strings)    
    strings.forEach{println(it.length)} // 에러 발생
}
```
- stringList가 addContent의 파라미터로 들어가기 위해선 MutableList\<String>이 MutableList\<Any>의 하위 타입이 되어야 한다.
- 이를 지키기 위해선 MutableList가 공변성을 가져야 한다. 

> 코틀린에서 T는 T?의 하위 타입이다. 즉 한 클래스에 두 가지타입(nullable type, not nullable type)이 존재한다.

### 공변성: 하위 타입 관계를 유지
- A가 B의 하위 타입일 때 Service\<A>가 Service\<B>의 하위 타입이라면 이는 공변성을 가진다.
- 제네릭은 기본적으로 무공변성을 지니기 때문에 위에서 MutableList\<Any>에 MutableList\<String>을 넣을 수 없었다. (하위 타입이 아니기 때문에)
- 하지만 코틀린은은 타입 파라미터에 out이라는 명령어를 통해 타입 파라미터가 공변성을 가지도록 할 수 있다. 

```kotlin
open class Animal
class Cat: Animal()

// out을 붙여 공변성을 지니게 하면 타입 안전성을 위해 해당 클래스는 타입 파라미터를 오직 out위치(생산)에 둘 수 있다.
class Herd<out T : Animal> {
    // T가 out 위치에 있으므로 가능. 해당 타입을 읽어 반환하는건 가능하다.
    fun getTypeParameter(): T = TODO()

    // T가 in 위치에 있으므로 에러 발생. 소비(쓰기)는 불가능
    // 공변성을 제공하게 되면 T는 T의 하위타입은 무엇이든지 사용가능하므로 해당 타입을 쓰는건 불가능하다. 
    // fun add(t: T) = TODO()
}

fun feedAll(animalHerd: Herd<Animal>) {
    for (animal in animalHerd.animals) {
        TODO()
    }
}

fun catTest(catHerd: Herd<Cat>) {
    // out을 지정해 공변성을 가지도록 하였으므로 Herd<Cat>은 Herd<Animal>의 하위 타입이 되어 호출이 가능하다.
    feedAll(catHerd)
}
```
- out 키워드를 붙이면 **공변성**은 제공해주나 이를 보장하기 위해 **내부에서 타입 파라미터를를 소비하는건 불가능하다.**
- List는 읽기 전용이므로 내부에서 요소를 소비할 일이 없기 때문에 out을 붙여 공변성을 보장해주도록 했다. 그래서 MutableList\<>와 달리 List\<>는 공변성을 제공한다.

> 변성(variance)은 코드에서 위험할 여지가 있는 함수를 호출하지 못하게 만들어 제네릭 타입의 안전성을 제공한다.
> - out 키워드는 내부에서 소비를 막아 공변성을 제공하더라도 제네릭 타입을 안전하게 사용할 수 있음을 보장한다.

### 반공변성: 뒤집힌 하위 타입 관계
```kotlin
interface Comparator<in T> {
    fun compare(e1: T, e2: T): Int {}
}
```
- 반공변성은 공변성의 반대이다. 반공변성을 가지는 클래스는 위와 같이 타입 값을 소비하는 것만 가능하다.
- String의 값을 비교하기 위해선 Comparator\<Any>를 사용할 수 있다. (String은 Any의 하위 타입이므로)
- 즉, Comparator의 타입파라미터가 자신의 상위 타입인 대상들은 Comparator로 사용할 수 있는 것이다.
- 그러므로 Comparator\<String>은 Comparator\<Any>의 상위 타입이 된다. 하지만 String은 Any의 하위 타입이다. 이것이 반공변성이다.
- **Service\<A>가 Service\<B>의 하위 타입일 때 A가 B의 상위 타입이라면 Service\<T>는 타입T에 반공변하다.** 
- in 키워드를 붙이면 반공변을 제공할 수 있고 이를 보장하기 위해 해당 클래스에선 소비만 가능하다.(in 위치에만 사용 가능)

```kotlin
public interface Function1<in P1, out R> : Function<R> {
    public operator fun invoke(p1: P1): R
}
```
- Function1 인터페이스를 보면 in, out을 모두 가질 수 있는 것을 알 수 있다.

```kotlin
open class Animal {
    fun getDouble(): Double = TODO()
}

class Cat : Animal()

fun catToNumber(f: (Cat) -> Number): Number = TODO()

fun main() {
    // 변성 규칙에 따라 Animal -> Double은 Cat -> Number의 하위 타입이므로 파라미터로 넘길 수 있다.
    catToNumber { animal: Animal -> animal.getDouble() }

    // 반환 하는건 클라이언트 입장에서 하위 타입이 반환되더라도 상위 타입을 대체할 수 있기 때문에 가능하다. 반대로 만약 상위 타입이 온다면 현재 반환 타입으로 지정한 타입의 모든것을 제공해줄 수 없다.
    // 소비 하는건 정해진 타입보다 상위 타입이 올 수 있다. 상위 타입은 해당 타입이 가지는 최소한을 제공해줄 수 있으므로 해당 타입에 존재하는 소비가 가능하다. 하지만 만약 하위 타입이오면 해당 타입보다 더 특수화되었기 때문에 불가능하다.
``` 

### 사용 지점 변성: 타입이 언급되는 지점에서 변성 지정
- 위에서 사용한 out, in 방식은 **선언 지점 변성**이라고 하며 클래스 선언 시 변성을 지정하면 모든 장소에 영향을 끼치므로 편리하다.
- 하지만 자바에서는 제네릭 클래스를 사용할 때마다 필요하다면 직접 변성을 정의해야 하며 이를 **사용 지점 변성**이라고 한다.

```java
public interface Stream<T> extends BaseStream<T, Stream<T>> {
    <R> Stream<R> map(Function<? super T, ? extends R> mapper);
}
```
- 자바는 사용 지점 변성이기 때문에 위와 같이 Function을 사용하는 모든곳에서 직접 변성을 지정해야 한다.

```kotlin
// 코틀린도 사용 지점 변성을 지원한다. 만약 클래스 에서 이미 변성이 지정되어 있다면 따로 사용 지점 변성을 하지 않아도 된다.
// 사용 지점 변성은 자바와 동일하기 떄문에 out T -> ? extends T, in T -> ? super T와 동일하다.
fun<T> copyData(source: MutableList<out T>,
                destination: MutableList<in T>) {
    for (item in source) {
        destination.add(item)
    }
}
```

### 스타 프로젝션: 타입 대신 *
- 자바의 와일드 카드와 동일하게 생각하면 된다.
- Service\<*>는 사실 Service\<out Any?>로 동작한다고 할 수 있다.
- *는 어떤 타입을 넣을 지 정확히 모르지만 제네릭 안전성을 위해선 오직 out만 가능할 것이다.

> 스타 프로젝션은 자바 와일드 카드처럼 타입의 데이터를 읽기만하지만 어떤 타입인지 알 필요 없을 때 일반 제네릭 타입 파라미터보다 간결하게 사용할 수 있다.


### 생산자, 소비자..
- 제네릭 클래스에서 생성(읽기)만 한다면 왜 공변성을 가질 수 있을까?
    - 생산자의 가장 대표적인건 제네릭 클래스 내부에서 반환 타입으로 정의하는 것이다.
    - 우리가 T 타입을 반환한다고 했을 때 T 타입의 하위 타입을 반환하더라도 T의 하위 타입은 T 타입을 대체할 수 있으므로 타입 안정성을 보장할 수 있다.
    - 반면 상위 타입의 경우 T타입보다 추상화되어 있기 때문에 클라이언트가 원하는 T타입을 완벽히 대체할 수 없다.
- 제네릭 클래스에서 소비(쓰기)만 한다면 왜 불공변성을 가질 수 있까?
    - 소비자의 가장 대표적인건 제네릭 클래스 내부의 함수 파라미터로 받는 것이다.
    - 함수 파라미터를 T타입으로 받고, 이 타입을 사용하려고 한다면 우리는 T타입보다 상위 타입을 사용할 수 있다.
    - 싱위 타입은 현재 타입에 비해 추상화된 버전이기 때문에 이를 소비하는건 문제가 되지 않는다.
    - 반면 하위 타입의 경우 더 구체화되어 있고 어떤 하위타입이 들어오는지 정확히 알 수 없으므로 소비하는건 불가능 하다.
    - 하위 타입이 내가 될 순 있지만 내가 하위 타입이 될 순 없다. 
