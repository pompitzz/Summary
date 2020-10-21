# 9장. 제네릭스
## 제네릭 타입 파라미터
- 자바와 다르게 코틀린은 raw 타입을 허용하지 않는다.
- 그러므로 반드시 제네릭 타입의 타입인자를 컴파일러가 알 수 있게, 혹은 명시하여야 한다.
- 기본적인 특징은 자바랑 모두 동일

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
- inline 함수를 활용하면 타입 인자가 지워지지 않도록 할 수 있다. 이를 **실체화**라고 한다.

### 런타임의 제네릭: 타임 검사와 캐스트
- 코틀린도 자바와 동일하게 런타임엔 타입 파라미터 정보는 제거딘다.
- 대부분의 경우 컴파일러에서 명시된 타입만 다룰 수 있도록 보장해주지만 런타임에 타입을 지우므로 런타임에 타입 인자를 검사할 수 없다.
    - 코틀린은 제네릭 타입을 사용하기 위해 타입인자를 반드시 명시해야하는데 `strs is List<String>`은 불가능하다.
    - `stars is List<*>`와 같이 star projection을 활용하자.
    - 단, 컴파일 시점에 타입정보를 알 수 있으면 `is List<String>`도 가능하다.
    
### 실체화한 타입 파라미터를 사용한 함수 선언
- 제네릭 클래스건 함수건 런타임엔 타입이 소거되어 확인이 불가능하지만, 인라인 함수의 타입 파라미터는 실체화되어 타입 인자를 알 수 있다.

```kotlin
// inline 함수와 reified를 활용하면 실체화된 타입으로 취급할 수 있다.
inline fun <reified T> isA(value: Any) = value is T

fun main() {
    println(isA<String>(1))
    println(isA<String>("1"))

    // 실체화된 타입을 활용할 수 있는 예(원하는 타입의 원소만 가져옴)
    val items = listOf(1, "2", 3)
    println(items.filterIsInstance<Int>())
}
```

#### 왜 인라인 함수에서만 실체화된 타입을 쓸 수 있을까?
- 컴파일러는 인라인 함수의 바이트코드를 해당 함수를 호출한 모든 곳에 복사하여 삽입하는데 이 때 컴파일러는 실체화된 타입 인자를 통해 함수를 호출하는 곳에서의 정확한 타입을 알 수 있게 해준다.
- 자바에서는 inline 함수도 보통의 함수처럼 호출하므로 reified를 사용하는 inline 함수를 호출할 수 없다.
- 실체화한 타입 파라미터가 있는 함수는 타입 인자를 바이트코드에 넣기 위해 일반 함수보다 더 많은 작어비 필요하고, 항상 inline이 가능해야 한다.

> 인라인 함수는 함수를 함수 파라미터로 가지는 등 성능에 효율적일 때 사용할 수 있고, 위와 같이 실체화한 타입을 사용하기 위해 사용할 수도 있다.

### 실체화한 타입으로 클래스 참조를 대신하기
```kotlin
fun <T> printClass(clazz: Class<T>) = println(clazz)

inline fun <reified T> printClassUsingReified() = printClass(T::class.java)

fun main() {
    printClass(Integer::class.java)
    
    // inline, reified를 활용하면 클래스 참조를 대신할 수 있다.
    printClassUsingReified<Integer>()
}
```


> 실체화한 타입 파라미터는 타입 검사, 리플렉션 등으로 사용할 순 있지만 해당 인스턴스를 생성하거나, 동반 객체 메서드를 호출하는 등의 작업은 불가능하다.


## 변성: 제네릭과 하위 타입
- 변성은 제네릭 타입의 기저 타입은 동일하나 타입 파라미터가 다를 때 서로 어떤 관게를 가지는지에 대한 개념이다.
- 제네릭을 사용하기 위해선 변성을 반드시 이해하자.

### 변성(variance)이 있는 이유: 인자를 함수에 넘기기
- List<Any>를 받는 함수에 List<String>을 넘기면 안전할거 같지만 실상은 그렇지 않다.

```kotlin
fun addContent(list: MutableList<Any>) {
    list.add(1)
}
```
- 이런 함수가 잇다면 문제가 발생할 수 있다. 그러므로 코틀린은 이런 함수를 호출할 수 없도록 한다.
- 만약 MutableList가 아니라 List였다면 원소의 변경이 없을것이므로 안전하게 다룰 수 있어 함수를 호출할 수 있다.
- 즉 List와 MutableList는 서로 다른 변성을 가지고 있다.

> 코틀린에서 T는 T?의 하위 타입이고 T?는 T의 하위 타입이 아니다. T, T? 모두 같은 클래스이나 서로 다른 타입인 것을 통해 클래스와 타입은 비슷하지만 다른것을 알 수 있다.

### 공변성: 하위 타입 관계를 유지
- A가 B의 하위 타입일 때 Service\<A>가 Service\<B>의 하위 타입이라면 이는 공변성을 가진다.
- 제네릭은 기본적으로 무공변성을 지니기 때문에 위에서 MutableList\<Any>에 MutableList\<String>을 넣을 수 없었다. (하위 타입이 아니기 때문에)
- 하지만 코틀린은은 타입 파라미터에 out이라는 명령어를 통해 타입 파라미터가 공변성을 가지도록 할 수 있다. 

```kotlin
open class Animal
class Cat: Animal()

// out을 붙이면 해당 타입 파라미터를 생산할 수만 있게 되어 제역이 생긴다.
class Herd<out T: Animal>(val animals: MutableList<Animal>) {
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
```
- out 키워드를 붙이면 **공변성**은 제공해주나 이를 보장하기 위해 **내부에서 타입 파라미터를 를 소비하는건 불가능하다.**
- List는 읽기 전용이므로 내부에서 요소를 소비할 일이 없기 때문에 out을 붙여 공변성을 보장해주도록 했다. 그래서 MutableList\<>와 달리 List\<>는 공변성을 제공한다.

> 변성(variance)는 코드에서 위험할 여지가 있는 함수를 호출하지 못하게 만들어 제네릭 타입의 안전성을 제공한다.
> - out 키워드는 내부에서 소비를 막아 공변성을 제공하더라도 제네릭 타입을 안전하게 사용할 수 있음을 보장한다.

### 반공변성: 뒤집힌 하위 타입 관계
```kotlin
interface Comparator<in T> {
    fun compare(e1: T, e2: T): Int {}
}
```
- 반공변성은 공변성의 반대이다. 반공변성을 가지는 클래스는 위와 같이 타입 값을 소비하기만 한다.
- String의 값을 비교하기 위해선 Comparator\<Any>를 사용할 수도 Comparator\<String>을 사용할 수도 있다.
- Comparator의 타입파라미터가 자신의 상위 타입인 대상들은 Comparator로 사용할 수 있는 것이다.
- 즉 Comparator\<String>은 Comparator\<Any>의 상위 타입이 된다. 하지만 String은 Any의 하위 타입이다. 이것이 반공변성이다.
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

    // 반환(생산)하는건(out) 클라이언트 입장에서 정해진 타입보다 하위 타입이 올 수 한다. 만약 상위 타입이 온다면 현재 반환 타입으로 지정한 타입의 모든것을 제공해줄 수 없다.
    // 소비하는건(in) 정해진 타입보다 상위 타입이 올 수 있다. 상위 타입은 해당 타입이 가지는 최소한을 제공해줄 수 있으므로 해당 타입에 존재하는 소비가 가능하다. 하지만 만약 하위 타입이오면 해당 타입보다 더 특수화되었기 때문에 불가능하다.
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
- 반공변에 대한 스타 프로젝션은 \<in Nothing>이 될 것이다. 이는 소비자 역할만 할 수 있지만 우리는 어떤 타입에 어디를 소비할 지 알 수 없다.

> 스타 프로젝션은 자바 와일드 카드처럼 타입의 데이터를 읽기만하지만 어떤 타입인지 알 필요 없을 떄 일반 제네릭 타입 파라미터보다 간결하게 사용할 수 있다.


### 생산자, 소비자..
- 제네릭 캘래스에서 생성만 한다면 왜 공변성을 가질 수 있을까?
    - 생산자의 가장 대표적인건 반환값이라고 할 수 있다.
    - 우리가 T 타입을 반환한다고 했을 때 T 타입의 하위 타입을 반환하더라도 타입 안정성을 보장할 수 있다.
    - T타입의 하위 타입은 결국 T타입이기 때문이다.
    - 상위 타입의 경우 T타입보다 추상화되어 있기 때문에 T타입의 역할을 대체할 수 없다.
- 제네릭 클래스에서 소비만 한다면 왜 불공변성을 가질 수 있까?
    - 소비자의 가장 대표적인건 함수 파라미터일 것이다.
    - 함수 파라미터를 T타입으로 받고, 이 타입을 사용하려고 한다면 우리는 T타입보다 상위 타입을 사용할 수 있다.
    - 싱위 타입은 추상화된 버전이기 때문에 상위 타입은 타입 안전하게 사용할 수 있다.
    - 반면 하위 타입은 더 구체적이기 때문에 타입 안전하게 사용할 수 없다.
        - 하위 타입이 내가 될 순 있지만 내가 하위 타입이 될 순 없다. 
