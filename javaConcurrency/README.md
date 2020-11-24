# 자바 병렬 프로그래밍
- 스레드는 서로 다른 메모리 주소 공간을 공유하고 동시에 실행되므로 다른 스레드가 사용 중일지도 모르는 변수를 읽거나 수정할 수 있다.
    - 이러한 특징때문에 다른 스레드간 데이터 공유가 수월하지만 그만큼 위험성이 따른다.
- 동기화를 하지 않으면 컴파일러, 하드웨어, 실행 환경 등에서 명령어의 실행 시점, 순서를 자유롭게 조정할 수 있기 때문에 예상치 못한 결과를 겪을 수 있다.
    - 레지스터나 다른 스레드에 일시적으로 보이지 않는 프로세스별 캐시 메모리에 변수를 캐싱할 수도 있다.
- 스레드가 많은 프로그램은 그만큼 컨텍스트 스위칭이 빈번하고, 그로 인해 상당한 부담이 생길 수 있다.

## 스레드 안전성
- 여러 스레드가 해당 메서드에 접근할 때 외부의 추가적인 동기화없이 어떤 상황에서도 `정확하게 동작`하면 **스레드 안전**하다고 할 수 있다. 

### 1. 자바에서의 동기화
- 자바에서 동기화를 위한 기본 수단은 synchronized 블럭을 이용한 배타적인 락 방식이다.
- 하지만 volatile 변수, 명시적 락, 단일 연산 변수(atomic variable)를 사용하는 경우에도 `동기화`라는 용어를 사용한다.

> 서블릿처럼 상태가 없는 객체는 항상 스레드 안전하다. 스레드 안전성의 필요성은 해당 객체가 무언가를 기억할 필요가 있을 때 생긴다.

### 2. 단일 연산
- 외부에서 객체의 메서드를 호출할 때 해당 메서드가 `모두 수행 됨 or 전혀 수행되지 않음` 딱 두 가지로만 정의내릴 수 있다면 **단일 연산**으로 볼 수 있다.
- **스레드 안전성을 가지는 객체들의 작업은 항상 단일 연산을 만족해야 한다.**

### 3. 락
- 단일 연산을 지원하는 Atomic 클래스를 사용하더라도 여러 Atomic 연산을 조합하여 사용하면 스레드 안전성을 보장할 수 없는 경우들이 빈번하다.

#### 스레드 안전하지 않는 CacheService
```java
public abstract class AtomicDataCacheService {
    private final AtomicReference<Long> lastKey = new AtomicReference<>();
    private final AtomicReference<Data> lastData = new AtomicReference<>();
    
    public Data getData(long key) {
        if (lastKey.get() == key) {
            return lastData.get();
        }
        Data data = createData(key);
        // lastKey만 set된 시점에 해당 key와 동일한 key를 가진 메서드가 다른 스레드에서 호출되면 정확한 데이터가 전달되지 않는다.
        lastKey.set(key);  
        lastData.set(data);
        return data;
    }
    
    protected abstract Data createData(long key);
}
```

#### synchronized 블럭
- 모든 자바 객체는 synchronized로 내장된 락을 사용할 수 있다.(암묵적인 락, 모니터 락 이라고도 한다)
- synchronized는 Mutex로 동작하므로 반드시 한 번에 한 스레드만이 해당 락을 소유할 수 있어 스레드 안전성을 보장한다.
- **하지만 메서드 전체에 synchronized를 거는건 해당 메서드의 병렬성이 사라지므로 매우 비효율적이다.**

#### synchronized의 재진입성
- 암묵적인 락은 재진입이 가능하기 때문에 특정 스레드가 자기가 이미 확보한 락을 다시 한번 더 확보할 수 있다.
    - 락을 확보하는건 요청 단위가 아닌 스레드 단위로 이루어진다.
- JVM은 스레드가 락을 확보하면 해당 스레드를 기록하고 락 확보 횟수를 1로 지정한다.
- 해당 스레드가 또 한번 더 락을 얻으려고 하면 확보 횟수를 2로 증가시킨다.
- 해당 스레드가 synchronized 블럭을 하나씩 빠져나갈 때 마다 확보 횟수를 감소시켜 **최종적으로 확보 횟수가 0이되면 락은 해제 된다.**

```java
class Parent {
    public synchronized void doSomeThing() {

    }
}

class Child extends Parent {
    @Override
    public synchronized void doSomeThing() {
        super.doSomeThing(); // 재진입성을 가지기 때문에 부모의 동기화 메서드를 호출하여도 데드락에 걸리지 않는다.
    }
}
```

### 4. 락으로 상태 보호
- 모든 변경할 수 잇는 공유 변수는 정확하게 **단 하나의 락으로 보호해야 한다.**
    - 락을 얻으면 다른 스레드가 동일한 락을 얻지 못하게 하는 것일 뿐 객체의 접근을 못하는건 아니다.
    - 공유 상태에 안전하게 접근할 수 있는 동기화 정책은 개발자에게 달렸다. 

### 5. synchronized 블럭을 최대한 쪼개어 성능 높이기
```java
public abstract class DataCacheService {
    private Long lastKey;
    private Data lastData;

    public Data getData(Long key) {
        synchronized (this) {
            if (lastKey.equals(key)) {
                return getData(key);
            }
        }
        // 동기화와 관련없는 부분은 동기화하지 않아 성능을 향상시킬 수 있다.
        Data data = createData(key);
        synchronized (this) {
            this.lastKey = key;
            this.lastData = data;
        }
        return data;
    }

    protected abstract Data createData(long key);
}
```
- 동기화를 제공할 수 있을 만큼 synchronized 블럭을 최대한 분리하여 성능을 향상시킬 수 있다.
- 성능을 향상시키려고 하면 코드가 복잡해지기 마련이다.
- 깔끔한 코드가 우선이며, 성능은 필요할 때 향상시키도록 하자.

## 객체 공유
- 여러 스레드에서 특정 객체를 동시에 사용하려고 할 때 안전하게 공유하는 방법을 알아본다.

### 1. 락과 가시성
- **여러 스레드에서 공통으로 사용하는 변수는 항상 적절한 동기화를 통해 가시성을 확보해야 한다.**
- 락은 상호 배제뿐만 아니라 정상적인 메모리 가시성을 확보하기 위해 사용하기도 한다.

#### 스테일 데이터
- 어떤 값을 읽을 때 해당 값이 최신 상태의 값이 아닌 경우를 스테일 데이터라고 한다.
- 쓰기에만 동기화를 고려하고 읽기에 동기화가 고려되지 않았을 때 발생할 수 있다.

#### 스테일 데이터보다 위험한 엉뚱한 데이터(64비트 연산에서)
- 데이터 읽기를 동기화하지 않았을 때 스테일 데이터를 가져오는건 최신 값이 아닐 뿐 이전에 사용되던 데이터이다.
- 하지만 Long과 같은 64비트 숫자에서 데이터를 동기화해주지 않으면 엉뚱한 데이터가 조회될 수 있다.
- JVM은 동기화되지 않은 64비트 숫자의 경우 두번의 32비트 연산을 사용할 수 있도록 허용한다.
- **그러므로 스테일 데이터를 신경쓰지 않아도 64비트 데이터의 경우 동기화가 되어 있지 않으면 잘못된 값을 사용할 수 있다.**
- [자바 명세 참고](https://docs.oracle.com/javase/specs/jls/se15/html/jls-17.html#jls-17.7)

#### 가시성 보장을 위한 volatile 변수
- volatile가 선언된 변수는 항상 최신 값을 읽을 수 있도록 해준다.
- **valatile를 사용하면 실행 순서는 재배치(reorder) 하지 않으며, 레지스터나 내부적인 캐시를 하지 않기 때문에 항상 메모리의 최신 값을 읽을 수 있다.**

#### volatile은 가시성만 보장해준다.
- 락을 사용하면 `연산의 단일성`을 보장해주지만 volatile은 단일성은 보장하지 않는다.
- volatile은 변수를 읽는 스레드는 많지만 변경하는 스레드가 하나일 때 주로 사용된다.

### 2. 스레드 한정
- 공유 객체가 단일 스레드에서 사용함을 보장할 수 있다면 동기화는 필요 없다.
- 이렇게 단일 스레드로 한정하는 기법을 통해 자동으로 스레드 안전성을 확보할 수도 있다.

#### 스레드 한정 기법 시용 예: JDBC 커넥션 풀
- 커넥션 풀에 존재하는 커넥션들은 여러 스레드에서 사용할 수 있으나, 동시에 한 스레드 에서만 사용할 수 있도록 되어 있어 스레드 안전하다.

#### 스레드 한정 기법 1) 스택 한정(지역 변수 활용)
- 지역 변수는 메모리 영역 중 스레드가 각각 독립적으로 가지는 스택에 존재하기 때문에 자동적으로 스레드 한정을 유지시킬 수 있다.

#### 스레드 한정 기법 2) ThreadLocal 활용
- ThreadLocal은 스레드 별로 다른 값을 사용할 수 있도록 관리해준다.
- ThreadLocal은 편리하지만 단점들이 존재한다.
    - 전역 변수가 아니지만 전역 변수처럼 동작해, 자기도 모르게 전역 변수를 남발하게 되어 시스템의 구조가 이상해질 수 있다.
    - ThreadLocal은 보이지 않는 연관 관계를 만들게 되므로 자기도 모르게 의존성이 강해질 수 있다.
    
### 3. 불변성
- 동기화 문제는 객체의 상태를 변경할 때 발생하므로 불변성을 보장하면 스레드 안전성을 확보할 수 있다.
    - 매번 불변 객체를 만드는 비용보다 동기화를 하기 위한 비용이 더 크므로 메모리 성능은 고려하지 않아도 된다.

#### 불변 객체와 volatile를 활용하여 스레드 동기화하기
```java
public abstract class OneValueDataCacheService {
    private volatile OneValueCache oneValueCache = new OneValueCache(null, null);

    public Data getData(Long key) {
        Data data = oneValueCache.getData();
        if (Objects.isNull(data)) {
            data = createData(key);
            oneValueCache = new OneValueCache(key, data);
        }
        return data;
    }

    protected abstract Data createData(long key);
}

@Getter
@RequiredArgsConstructor
class OneValueCache {
    private final Long lastKey;
    private final Data lastData;

    public Data getData() {
        if (Objects.isNull(lastData)) {
            return null;
        }
        return lastData;
    }
}
```
- 단일 연산으로 동작해야 하는 값들을 하나의 불변 클래스로 묶고 volatile을 사용하도록 하면 스레드 안전성을 보장할 수 있다.

### 4. 객체를 안전하게 공유하기
- 객체를 안전하게 공유하여 스레드 안전성을 확보해준다면 클라이언트에선 동기화를 신경쓰지 않도록 할 수 있다.
- 1) 스레드 한정
- 2) 읽기 전용 객체 공유
- 3) 동기화된 객체 공유

## 객체 구성

### 1. 스레드 안전한 클래스 설계
- 객체의 상태들을 캡슐화한다면 스레드 안전성을 확인하기 위해선 해당 객체 내부만 확인하면 되므로 논리적인 파악이 쉬워지고, 동기화 작업의 범위를 최대한 좁힐 수 있다.

#### 상태 소유권
- 컬렉션들은 컬렉션 자체 객체 뿐만아니라 컬렉션 요소들의 객체들도 소유할 수 있다.
- 이런 경우 `소유권을 분리`하여 컬렉션 자체에 대한 소유권은 컬렉션 클래스가, 각 요소 데이터들은 해당 데이터를 사용하는 클라이언트가 소유권을 가지고 동기화를 해줘야 한다.
    - Map이랑 비슷한 구조로 구성돤 ServletContext는 클라이언트에서 setAttribute, getAttribute를 통해 객체를 등록하거나 뽑아낼 수 있다.
    - setAttribute, getAttribute의 경우 클래스 내부적으로 동기화가 되어 있어 동기화하지 않아도 되지만 그로 인해 얻어지는 객체들은 ServletContext가 소유권을 가지지 않으므로 스레드 안전성을 따로 확보해줘야 한다.
    
### 2. 인스턴스 한정 기법
```java
@ThreadSafe
public class PersonSet {

    @GuardedBy("this")
    private final Set<Person> mySet = new HashSet<>();
    
    public synchronized void addPerson(Person p) {
        mySet.add(p);
    }
    
    public synchronized  boolean containsPerson(Person p) {
        return mySet.contains(p);
    }
}
```
- 위와 같이 사용될 객체를 캡슐화하여 원하는 기능만 내어주고 동기화 한다면 스레드 안정성을 확보할 수 있다. 
    - Collections.synchronizedList 같은 경우 데코레이터 패턴을 통한 인스턴스 한정 기법으로 스레드 안전성을 보장해준다.

#### 자바 모니터 패턴
```java
public class PrivateLock {
    private final Object myLock = new Object();
    @GuardedBy("myLock") String test;
    
    void someMehtod() { 임
        synchronized (myLock) { // myLock은 private로 캡슐화 되었으므로 test는 안전하다
            System.out.println(test);
        }
    }
}
```
- 락을 private 변수로 캡슐화하고 해당 락을 암묵적인 락으로 활용하여 스레드 안전성을 보장할 수도 있다.

### 3. 스레드 안전성 위임
- 사용하려는 객체가 이미 스레드 안전성을 확보하고 있을 때 동기화가 필요할까?
- 단지 위임하는 거라면 필요 없을 것이다.
- 그리고 사용하려는 객체들이 독립적이고, 단일 연산을 보장해줄 수 있다면 동기화가 필요 없다.
- 하지만 사용하려는 객체들이 서로 관계를 가지고 있다면 따로 동기화를 해주어야 한다.

### 4. 스레드 안전하게 구현된 클래스에 기능 추가
- 이미 구현된 클래스에 기능을 추가할 때 위임하는 방식을 주로 사용한다.
- 이때 락 대상을 잘못 지정하면 스레드 안전해보이지만 실제론 스레드 안전하지 못할 수 있다.

#### 잘못된 동기화
```java
@NotThreadSafe
public class ListHelper<E> {
    public List<E> list = Collections.synchronizedList(new ArrayList<>());
    
    public synchronized boolean putIfAbsent(E x) {
        boolean absent = !list.contains(x);
        if (absent)
            list.add(x);
        return absent;
    }
}
```
- putIfAbsent는 동기화 블록이 있어 스레드 안전해보이지만 그렇지 않다.
- 우리가 동기화해야 하는건 list이지만 현재는 ListHelper에 대한 lock을 통해 동기화를 이루고 있기 때문에 list 입장에서 단일 연산이라고 볼 수 없다.

#### 스레드 안전하게 변경
```java
@ThreadSafe
public class ListHelper<E> {
    public final List<E> list = Collections.synchronizedList(new ArrayList<>());
    
    public boolean putIfAbsent(E x) {
        // list 락을 사용하도록 동기화 하여야 한다.
        synchronized (list) {
            boolean absent = !list.contains(x);
            if (absent)
                list.add(x);
            return absent;
        }
    }
}

```
- list로 락을 잡아 동기화하도록 하면 스레드 안전성을 보장할 수 있다.

## 스레드 안전한 JDK 라이브러리
### 1. 병렬 컬렉션 
#### ConcurrentHashMap
- 락 스트라이핑을 이용하여 세밀한 동기화를 하기 때문에 병렬성과 확장성이 뛰어난 클래스이다.
    - 읽기와 쓰기를 동시에 처리할 수도, 쓰기 연산을 제한된 횟수 만큼 동시에 수행할 수도 있다.
- 병렬성을 보장하기 위해 isEmpty, size 같은 메서드는 정확한 값을 반환하지 못한다.

#### CopyOnWriteArrayList
- 동기화된 List 클래스보다 병렬성을 훤씬 높이고자 만들어진 클래스이다.
- 해당 클래스는 반복문을 순회할 때 락을 걸거나, 컬렉션을 따로 복사하지 않아도 된다.
- 해당 클래스는 컬렉션이 변경될 때 마다 복사하는 방식을 통해 스레드 안전성을 보장해준다.
    - Iterator를 사용하는 그 시점에 복사본을 통해 순회한다.
    - 이런한 특징 때문에 데이터 수정이 많은 경우 오히려 성능이 좋지 않을 수 있다.
    
### 2. Blocking Queue, Product-Consumer 패턴
#### Producer-Consumer
- Product-Consumer 패턴은 작업의 생산자와 소비자를 분리할 수 있어 개발 과정을 명확하게 단순화 시킬 수 있고, 필요에 따라 부하조절도 가능하다.
- 이런 구현은 Blocking Queue를 이용하는 경우가 많이 있다.
    - 프로듀서는 작업을 큐에 쌓기만하고, 컨슈머는 큐에 있는 작업을 가져와 처리한다.
    - 프로듀서와 컨슈머는 서로에 대해 의존하지 않고 자신이 해야할 일에만 신경쓰면 된다.

#### Java BlockingQueue
- Java BlockingQueue의 put 메서드는 큐가 가득 차 있는 경우 공간이 생길 때 까지 대기한 후 데이터를 삽입한다.
- Java BlockingQueue의 take 메서드는 큐가 비어있는 경우 데이터가 쌓일 때 까지 대기한 후 데이터를 꺼내온다.
- LinkedList를 활용한 LinkedBlockingQueue, ArrayList를 활용한 ArrayBlockingQueue가 존재하며 PriorityBlockingQueue또한 존재한다.
- 프로듀서가 컨슈머에게 직접 정보를 넘겨주는 SynchronousQueue도 존재하며 이는 컨슈머에게 프로듀서의 정보를 제공해줄 수 있으며 컨슈머에게 데이터를 전달하는 순간이 매우 짧아진다.

#### Blocking Queue로 FileCrawler and FilePrinter 만들기
```java
public class BlockingQueueEx {

    public static void main(String[] args) {
        BlockingQueue<File> fileBlockingQueue = new LinkedBlockingQueue<>(10);
        new Thread(new FileCrawler(fileBlockingQueue, new File("/"))).start();
        new Thread(new FilePrinter(fileBlockingQueue)).start();
    }

    @RequiredArgsConstructor
    static class FileCrawler implements Runnable {
        private final BlockingQueue<File> fileBlockingQueue;
        private final File root;

        @Override
        public void run() {
            try {
                crawl(root);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void crawl(File file) throws InterruptedException {
            File[] files = Optional.ofNullable(file.listFiles()).orElse(new File[]{});
            for (File eachFile : files) {
                if (eachFile.isDirectory()) {
                    crawl(eachFile);
                } else {
                    fileBlockingQueue.put(eachFile);
                }
            }
        }
    }

    @RequiredArgsConstructor
    static class FilePrinter implements Runnable {
        private final BlockingQueue<File> fileBlockingQueue;

        @Override
        public void run() {
            try {
                while (true) {
                    File file = fileBlockingQueue.take();
                    System.out.println("file: " + file.getName());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```
- Blocking Queue로 Producer-Consumer 기반의 FileCrawler, FilePrinter를 만들 수 있다.
- Cralwer와 Printer는 BlockingQueue로만 의존하기 때문에 독립적인 확장이 가능하다.
    - 독립적이기 때문에 시간이 오래 걸리는 부분에만 스레드 수를 늘릴 수 있다. 
