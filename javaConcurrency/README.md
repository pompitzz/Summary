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
    - 독립적이기 때문에 시간이 오래 걸리는 부분에만 스레드 수를 늘릴 수 있기 때문에 확장성이 뛰어나다. 

### 3. Thread 인터럽트
- blocking 연산은 단순히 오래 걸리는 연산이 아니라, **특정 신호를 받아야 계속 실행할 수 있는 연산을 의미한다.**
- 그러므로 blocking 연산은 InterruptedException를 발생시킬 수 있다.
    - 대표적으로 Thread.sleep, BlockingQueue.take, BlockingQueue.take등이 있다.
- 이러한 특징때문에 Thread 클래스는 interrupt 메서드를 제공하여 스레드를 중단시킬 수 있다. (호출 시 InterruptedException가 발생할 것이다)
    - interrupt는 스레드 간의 협력을 위한 방법이므로 단지 interrupt를 요청하는 것이다. 
    - interrupt를 호출한다고 즉시 스레드를 중단시키지 않는다.(적절한 시점에 알아서 중단시킨다)
    

#### InterruptedException 처리
- 해당 예외를 던진다는 건 블로킹 메서드라는 의미이므로 해당 예외는 반드시 처리가 필요하다.
- 일반적인 예외 처리 방식처럼 예외를 외부에 그대로 전달할 수도, 예외를 복구 시킬 수도 있다.
- 가장 흔히 사용하는 방식은 Thread.currentThread().interrupt()를 호출하여 현재 스레드에서도 인터럽트를 발생시켜 상위 호출 메서드가 알 수 있도록 해주도록 한다.

### 4. 동기화 클래스
- 동기화 클래스는 상태 정보를 활용하여 스레드 간의 작업 흐름을 조절해주는 클래스이다.
- 대표적으로 세마포어, 배리어, 래치, 블로킹 큐 등이 있이 있으며 각 클래스들은 서로 다른 특징을 가진다.

#### 래치
- 래치는 일종의 관문과 같이 특정 상태에 이르기 전까지 관문을 닫아 작업들을 막아두었다가, 특정 상태가 되면 관문을 열어 모든 작업을 동시에 실행시킬 수 있게 해준다.

```java
@Slf4j
public class LatchTest {
    public static void main(String[] args) throws InterruptedException {
        int users = 5;
        CountDownLatch readyLatch = new CountDownLatch(users);

        IntStream.range(0, users)
                .mapToObj(i -> createRunnable(readyLatch, i))
                .forEach(CompletableFuture::runAsync);

        log.info("모든 사용자가 Ready할 때 까지 대기");
        readyLatch.await();
        log.info("게임 시작");
    }

    private static Runnable createRunnable(CountDownLatch readyLatch, int i) {
        return () -> {
            try {
                TimeUnit.SECONDS.sleep(i);
                log.info(i + "번 사용자 Ready");
                readyLatch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }
}
```
- 래치의 동작은 게임에서 모든 유저가 레디되었을 때 게임이 시작되는 것을 생각해보면 된다.

#### 세마포어
- 특정 자원이나 특정 연산을 동시에 사용할 수 있는 수를 제한하고 싶을 때 사용할 수 있는 클래스이다.
- 처음 생성 시 permit을 수를 지정해두고, acquire()로 permit 획득, release()로 permit 해제를 통해 수를 조절할 수 있다.

#### 배리어
- 다른 스레드를 특정 시점이 될 때 까지 기다리며, 해당 시점에 추가적인 작업을 수행할 수 있는 동기화 클래스이다.
    - 래치는 일회성이지만 배리어(CyclicBarrier)는 특정 시점에 대해 계속해서 동기화를 수행할 수 있다.
- 여러 스레드가 특정 배리어 포인트에서 기다렸다가 조건을 만족하면 필요한 작업을 수행하도록 할 수 있다.

```java
@Slf4j
public class CyclicBarrierTest {
    public static void main(String[] args) throws InterruptedException {
        int threads = 5; 
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threads, () -> log.info("barrier action 수행"));

        IntStream.range(0, threads * 2)
                .forEach(i ->
                        CompletableFuture.runAsync(() -> {
                            try {
                                log.info("barrier await");
                                cyclicBarrier.await();
                            } catch (InterruptedException | BrokenBarrierException e) {
                                Thread.currentThread().interrupt();
                            }
                        })
                );

        Thread.sleep(1000);
    }
}
```
- threads * 2번의 작업을 수행하므로 barrier action은 두번 수행될 것이다. 

### 5.효 율적이고 병렬성이 있는 캐시 구현하기
```java
@Slf4j
public class Memozier<S, T> {
    private final Computable<S, T> computable;
    private final Map<S, Future<T>> cache;

    public Memozier(Computable<S, T> computable) {
        this.cache = new ConcurrentHashMap<>();
        this.computable = computable;
    }

    public T compute(S source) {
        Future<T> future = cache.get(source);
        if (Objects.isNull(future)) {
            FutureTask<T> futureTask = new FutureTask<>(() -> computable.compute(source));
            // 단일 연산 메서드를 활용하여 안전성을 보장받을 수 있다.(동시에 해당 메서드를 호출해도 가장 먼저 put한 futureTask가 들어간다)
            cache.putIfAbsent(source, futureTask);
            future = cache.get(source);
            futureTask.run();
        }
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

interface Computable<S, T> {
    T compute(S source);
}
```
- ConcurrentHashMap과 Future를 적절히 활용하면 병렬성있는 캐시를 쉽게 구현할 수 있다. 

## 작업 실행
### 1. Executor 동작 주기
```java
public interface ExecutorService extends Executor {

    // 안전한 종료 절차 진행
    // - 새로운 작업은 받지 않지만 이전의 작업까지는 모두 끝냄
    void shutdown();

    // 강제 종료 절차 진행
    // - 현재 진행 중인 작업도 가능하면 취소, 대기 중인 작업은 모두 취소
    List<Runnable> shutdownNow();

    boolean isShutdown();
    
    // 종료를 확인할 수 있는 플래그도 제공
    boolean isTerminated();
    
    // ExecutorService가 종료 될 때 까지 기다릴 수 있다. 
    // (shutdown -> awaitTermination 으로 바로 호출하면 동기적으로 ExecService를 종료할 것이다.) 
    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;
    
    ...
}
```
- 위 메서드 외에도 작업 추가 및 동작 주기를 관리할 수 있는 다양한 메서드를 제공한다.

### 2. Timer보단 ScheduledThreadPoolExecutor를 사용하자
#### Timer의 단점
- 등록된 작업을 실행시키는 스레드가 하나이므로 실행을 예측할 수 없는 경우가 존재한다.
    - 주기가 10ms, 작업 시간이 40ms일 경우 어떻게 실행되는지 예측할 수 없다.
- 예외가 발생하면 Timer 스레드는 따로 예외를 처리하지 않기 때문에 스레드 자체가 멈출 수 있다.
    - 추가적으로 새로운 스레드를 스스로 만들어 작업을 이어나가지 않게 되어 있다.
    
#### ScheduledThreadPoolExecutor
- ScheduledThreadPoolExecutor를 사용하면 지연 작업 및 주기적 작업마다 여러 개의 스레드를 할당하여 작업을 원하는 시간에 실행할 수 있다.

## 중단 및 종료
- 스레드를 안전하고 빠르게 멈추게 하는 것은 어렵다. 자바에서는 스레드를 강제로 멈추게할 수 없고 인터럽트 요청을 보낼 뿐이다.
- 스레드가 중지 요청을 받고 작업을 중지하기 전엔 그와 관련된 작업들을 모두 종료하고 스레드를 멈추어야 한다.
    - 이러한 일은 스레드 자신이 처리하는 것이 가장 적절하고 시스템의 유연성을 키울 수 있다.
    
> 작업 중단 기능을 구현하고 적용하는건 모두 개발자의 몫이다.
    
### 1. 인터럽트
- 인터럽트는 다른 스레드에서 수행되는 작업을 중지하기 위한 방법으로 적절하다.
- Thread.currentThread().isInterrupted()를 통해 현재 스레드의 인터럽트 상태도 확인할 수 있다.

#### 인터럽트 정책
- 인터럽트 요청이 들어왔을 때 요청을 받은 스레드가 어떻게 처리할 지에 대한 인터럽트 정책이 수립되어야 한다.
- 보통 범용적인 정책은 상위 수준의 메서드에도 현재 인터럽트가 발생한 것을 그대로 유지해서 알려줘 해당 측도 인터럽트에 대응할 수 있도록 하는 것이다.
    - 보통 작업은 그작업을 소유하는 스레드가 아닌 스레드 풀과 같은 작업을 실행을 전담하는 스레드를 빌려 사용한다.
    - 이런 작업 전용 스레드에서 인터럽트가 발생할 경우 해당 작업을 소유한 스레드에게도 인터럽트 상태를 알려줘 대응 가능하게 해주는 것이 좋다.
    - 이런 이유로 인해 블로킹 메서드에서 인터럽트가 발생하면 그대로 인터럽트를 전달한다.  
   
#### 인터럽트 처리
```java
public class Test {
    public Task getNextTask(BlockingQueue<Task> queue) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return queue.take();
                } catch (InterruptedException e) {
                    interrupted = true;
                    // 그냥 넘어가고 재시도 한다.
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```
- 바로 인터럽트를 넘겨주지 않고 모든 작업 완료 후 인터럽트 상태를 알려줄 수도 있다.

### 2. 스레드 기반 서비스 중단
- 스레드 풀을 사용하는 경우 애플리케이션이 종료될 때 스레드 풀을 안전하게 종료해야 한다.
- 하지만 스레드를 선점적으로 종료할 순 없기 때문에 스레드에게 종료를 부탁해야 한다.(인터럽트를 걸어 종료를 요청해야 한다)
- 이러한 스레드를 관리하는 일은 스레드를 생성하는 스레드 풀에서 담당해야 하며 ExecutorService는 shutdown, shutdownNow 메서드를 통해 종료 기능을 제공한다.

#### 독약
- 프로듀서-컨슈머 방식에서 독약이 되는 작업을 Queue에 넣어 해당 작업이 도착하면 종료하도록 만들 수 있다.

#### ExecutorService shutdownNow 메서드 약점 보완
- 해당 메서드는 수행되지 않는 작업은 제공해주지만, 실행 중이였다가 도중에 중지된 작업은 알려주지 않는다.
- ExecutorService를 상속받아 이러한 기능을 제공하도록 만들 수 있다. 

```java
public class TrackingExecutor extends AbstractExecutorService {
    private final ExecutorService exec;
    private final Set<Runnable> tasksCancelledAtShutdown = Collections.synchronizedSet(new HashSet<>());

    public TrackingExecutor(ExecutorService exec) {
        this.exec = exec;
    }

    public List<Runnable> getCancelledTasks() {
        if (!exec.isTerminated()) {
            throw new IllegalStateException();
        }
        return new ArrayList<>(tasksCancelledAtShutdown);
    }

    @Override
    public void execute(Runnable command) {
        exec.execute(() -> {
            try {
                command.run();
            } finally {
                // 작업을 종료할 때 현재 작업이 도중에 중지되었는지 확인 후 컬렉션에 추가한다.
                if (isShutdown() && Thread.currentThread().isInterrupted()) {
                    tasksCancelledAtShutdown.add(command);
                }
            }
        });
    }

    // 나머지 오버라이딩 메서드는 전부 위임한다.
}
```
- 스레드 풀을 shutdownNow로 즉시 종료하였을 때 수행되지 않은 작업 뿐만 아니라 도중에 수행이 중지된 작업들도 확인할 수 있다.

### 3. 비정상적인 스레드 상황 처리
- 스레드에서 예외가 발생해 비정상적으로 종료되었을 때 UncaughtExceptionHandler를 통해 예외를 핸들링 할 수 있다.
    - 해당 예외에서 필요한 로깅을 하던지, 스레드를 다시 생성하여 작업을 재개하도록 하는지 등을 할 수 있을 것이다.
- ThreadPoolExecutor를 생성할 때 필요한 ThreadFactory에 해당 핸들러를 넘겨주면 됨.

### 4. JVM 종료
- JVM은 보통 1) 예정된 절차로 종료, 2) 갑자기 종료 되는 경우가 있다.

#### 종료 훅
- JVM이 예정된 절차로 종료될 경우 등록된 종료 훅을 동시에 실행시킨다.

```java
public void start() {
    Runtime.getRuntime()
            .addShutdownHook(new Thread() {
                @Override
                public void run() {
                    LogService.this.stop();
                }
            });
    logger.start();
}
```
- 종료 훅을 이용하여 JVM이 종료될 때 원하는 Service가 stop되도록 할 수 있다.

## 스레드 풀 활용
### 1. 스레드 부족으로 인한 데드락
- 스레드 풀에서 수행되는 작업이 의존성을 가지고 있으며, 스레드 풀이 충분히 크지 않을 경우 데드락이 걸릴 수 있다.

```java
public class ThreadDeadlock {
    // 싱글 스레드 풀
    ExecutorService exec = Executors.newSingleThreadExecutor();
    
    public class RenderPageTask implements Callable<String> {
        public String call() throws Exception {
            Future<String> header, footer;
            header = exec.submit(new LoadFileTask("header.html"));
            footer = exec.submit(new LoadFileTask("footer.html"));
            String page = renderBody();

            // 싱글 스레드 풀이므로 해당 연산은 영원히 종료되지 않는다.
            return header.get() + page + footer.get();
        }
        
    }
}
```
- 싱글 스레드 풀이므로 의존성 있는 두 작업은 데드락에 걸린다.
    - 스레드 풀에 서로 의존하는 작업이 만핟면 스레드 풀 수를 제한하지 않는 것이 좋다.
- 데드락 이외에도 오래 걸리는 작업으로 인한 레이턴시 문제를 위해 타임아웃 설정 하자.

### 2. 스레드 풀 크기 조절
- [Java의 ThreadPoolExecutor, Spring의 ThreadPoolTaskExecutor 게시글 참고하자](https://pompitzz.github.io/blog/java/threadPoolExecutor.html)


### 3. 스레드 풀 집중 대응 정책
- 스레드 풀이 가득 차고, 큐 또한 가득차있다면 집중 대응 정책이 동작하며 기본적으로는 **중단 정책**이 적용된다.
    - setRejectedExecutionHandler를 통해 대응 정책을 정할 수 있다. 

#### 중단 정책
- RejectedExecutionExecption예외를 던지기 때문에 execute를 호출하는 측에서 해당 예외 처리 필요

#### 제거 정책
- 추가하려고 한 작업을 제거시킴

#### 오래된 항목 제거 정책
- 가장 오래된 작업을 제거하고 새로운 작업을 추가함
    - 우선순위 큐를 사용하고 있다면 우선순위가 가장 높은 작업이 제거될 것이므로 그런 경우 사용하지 말자.

#### 호출자 실행 정책
- 해당 작업을 호출한 스레드에서 실행하도록 하는 정책

### 4. 스레드 팩토리
- 스레드 풀에서 스레드를 생성할 땐 항상 스레드 팩토리에서 생성하게 된다.

#### 스레드 팩토리를 직접 작성할 필요가 있는 경우
- 스레드에 의미 있는 이름을 정의하거나, 스레드에 대한 로깅을 추가하거나, ExceptionHandler를 직접 지정하고 싶을 때 사용할 수 있다.

```java
@Slf4j
public class MyAppThread extends Thread {
    public static final String DEFAULT_NAME = "MyAppThread";
    private static volatile boolean debugLifecycle = false;
    private static final AtomicInteger created = new AtomicInteger();
    private static final AtomicInteger alive = new AtomicInteger();

    public MyAppThread(Runnable r) {
        this(r, DEFAULT_NAME);
    }

    public MyAppThread(Runnable r, String name) {
        super(r, name + "-" + created.incrementAndGet());
        // 직접 예외 처리를 지정할 수 있다.
        setUncaughtExceptionHandler((thread, exception) ->
                log.error("UNCAUGHT in thread. threadName: {}", thread.getName(), exception));
    }

    @Override
    public void run() {
        boolean debug = debugLifecycle;
        if (debug) log.debug("Created {}", getName());
        try {
            alive.incrementAndGet();
            super.run();
        } finally {
            alive.decrementAndGet();
            if (debug) log.debug("Exiting {}", getName());
        }
    }
}
```
- 예외를 직접 핸들링하도록 하고, 스레드 운영에 대한 로깅 및 통계 처리를 가능하게 한다.

### 4. ThreadPoolExecutor 상속
- ThreadPoolExecutor를 상속받아 기능을 추가할 수 있고, beforeExecute 같은 훅 메서드를 활용할 수 있다.

#### 훅 메서드 동작 방식
- 훅 메서드는 Executor가 동작하는 과정에서 사용했던 각종 자원을 반납하는 등의 일을 처리하거나 마지막으로 특정 알람, 로깅, 통계 처리에 적당한 메서드이다.
- afterExecute는 예외가 발생하더라도 반드시 동작하도록 되어 있다. (시스템 Error는 제외)
- 하지만 beforeExecute에서 예외가 발생하면 작업이 수행되지 않아 afterExecute는 실행되지 않을 것이다.
- terminated는 모든 작업과 모든 스레드가 종료되고 스레드 풀 종료 절차 마무리 후 마지막에 동작된다.

#### 훅 메서드를 활용하여 통계 기능이 추가된 스레드 풀 구현
```java
@Slf4j
public class TimingThreadPool extends ThreadPoolExecutor {
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();
    private final AtomicLong numTasks = new AtomicLong();
    private final AtomicLong totalTime = new AtomicLong();

    public TimingThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        log.debug("Thread {}: start {}", t, r);
        startTime.set(System.nanoTime());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        try {
            long endTime = System.nanoTime();
            long taskTime = endTime - startTime.get(); // ThreadLocal을 활용하여 스레드별 고유 상태를 관리할 수 있다.
            numTasks.incrementAndGet();
            totalTime.addAndGet(taskTime);
            log.debug("Thread {}: end {}, time: {}ns", t, r, taskTime);
        } finally {
            super.afterExecute(r, t);
        }
    }

    @Override
    protected void terminated() {
        try {
            log.info("Terminated: avg time={}ns", totalTime.get() / numTasks.get());
        } finally {
            super.terminated();
        }
    }
}
```

## 데드락 및 그 밖의 활동성 문제
### 1. 데드락
- 보통 DB 시스템은 데드락을 검출한 후 복구 하는 기능이 있지만 JVM은 데드락을 회복할 수 없기 때문에 데드락이 발생하는 순간 게임은 끝이다.

#### 락 순서에 의한 데드락
```java
public class LeftRightDeadlock {
    private final Object left = new Object();
    private final Object right = new Object();

    public void leftRight() {
        synchronized (left) {
            synchronized (right) {
                doSomeThing();
            }
        }
    }
    
    public void rightLeft() {
        synchronized (right) {
            synchronized (left) {
                doSomeThing();
            }
        }
    }
}
```
- leftRight(), rightLeft()를 동시에 수행하면 각각 하나씩 lock을 가져 데드락이 발생할 수 있다.
- 꼭 순서대로 락을 얻는게 아니더라도, 객체간의 메시지 교환 상에서 순서로 인해 데드락이 발생할 수 있다.
    - 객체 간의 메시지 교환으로 인해 발생하는 데드락을 방지하기 위해선 **오픈 호출**을 이용하자.
    
#### 오픈 호출
- 외부 메서드를 호출할 땐 락을 확보하지 않은 상태에서 해당 메서드를 호출하는 것을 **오픈 호출**이라고 한다.

### 2. 데드락 방지 및 원인 추적
- 데드락 가능성을 확인 하기 위해선 여러 개의 락을 확보하는 부분을 찾아내야 한다.
- 위에서 언급한 오픈 호출 방식을 활용하고 있다면 락을 찾기가 수월해질 것이다.

#### 락의 시간 제한
- Lock 클래스엔 시간 제한이 있는 tryLock이 존재한다. 
- 암묵적인 동기화 블럭이 아닌 이를 활용하면 데드락을 방지할 수 있다.

#### 스레드 덤프를 통해 데드락 분석
- 데드락을 방지하는 것이 최우선이나, 데드락이 발생했다면 JVM이 만들어내는 스레드 덤프를 활용해 데드락이 발생한 위치를 알아내야 한다.
- 스레드 덤프는 모든 스레드의 스택 트레이스가 담겨 있고, 스택의 어느 부분에서 어떤 락을 확보 했는지 그리고 대기 중인 스레드가 어느 락을 확보하려고 대기 중인지를 알 수 있다.
- JVM은 스레드 덤프를 생성하기 전에 락 대기 상태 그래프에서 사이클이 발생했는지, 즉 데드락이 발생한 부분이 있는지 확인한다.
    - 만약 데드락이 있다고 판단될 시 어느 락과 어느 스레드가 데드락에 관여하고 있는지에 대한 정보를 덤프에 포함시킨다.

### 3. 그 밖의 활동성 문제점
#### Starvation(기아 상태)
- 스레드가 작업을 진행하는데 필요한 자원을 영영 할당받지 못하는 상태
- 스레드가 우선순위에서 계속 밀려나 영영 자원을 할당 받지 못할 수 있다.
- 스레드 순서를 지정할 수 있지만 운영체제에 의존적이므로 사용하지 말자.

#### 늦은 응답
- 여러 자원들이 CPU를 가지고 경쟁이 심해지면, 응답성이 떨어진게 된다.

#### 라이브락
- 대기 중인 상태가 아니더라도, 특정 작업의 결과를 받아야만 실행할 수 있는 작업이 존재할 때 선수 작업이 계속 실패하여 계속해서 재시도하게 무한 루프에 빠지는 상태
    - 메시지 전송 -> 전송 실패 -> 전송 트랜잭션 롤백 후 큐에 쌓음 -> 큐에서 꺼내서 메시지 전송 -> 전송 실패 -> 전송 트랜잭션 롤백 후 큐에 쌓음
    - 이러한 방식을 무한히 반복하는 것을 생각해보면 된다.

> 라이브락은 에러를 너무 완벽하게 처리하고자 하여 회복 불가능한 에러를 회복할 수 있다고 판단해 무한히 재시도하는 과정에서 발생한다.

## 성능과 확장성
- 성능을 높이기 위해 안전성을 떨어뜨리는 것은 최악의 상황이며 결국 성능과 안전성을 모두 놓치게 될 것이다.
- 성능을 높이기 위해선 구체적인 요구사항과 수치가 있어야 한다. 

### 1. 암달의 법칙
- 병렬 작업에 따른 성능을 예측할 수 있는 공식으로 다음과 같다.

```
속도 증가량 <= 1 / (F + ((1-F)/N))
N: 프로세서 수
F: 순차적으로 실행해야하는 작업의 비율 
```
- F가 0.5(50%)라면 N이 무한대여도 결국 2가 최대이다.
    - 즉 순차적인 작업이 많으면 아무리 프로세서가 많더라도 성능을 향상시키긴 어렵다.
- **애플리케이션의 병렬 작업의 성능을 높이기 위해선 동기 작업의 수가 얼마나 적은지가 가장 중요하다.**
    - 병렬로 성능을 높이기 위해선 먼저 동기 작업을 확실히 파악하자.
    
#### 모든 프로그램엔 동기 작업이 반드시 존재한다.
- Blocking Queue를 사용하더라도 작업을 Queue에 가져오는 연산은 동기화되어 있을 것이다.
- 각 병렬 작업을 마지막에 취합하여 저장하는 등의 작업도 결국 동기화 작업이 된다.

### 2. 스레드와 비용
#### 컨텍스트 스위칭
- CPU가 현재 스레드에서 다른 스레드로 넘어가 작업을 처리하기 위해 컨텍스트 스위칭이 일어난다.
- 스레드들이 빈번하가 대기 상태에 들어간다면 컨텍스트 스위칭 수가 늘어날 것이고 이는 부하에 영향을 줄 수 있다.

### 3. 락 경쟁 줄이기
- 병렬 애플리케이션에서 가장 큰 위협은 특정 자원을 독점적으로 사용하도록 제한하는 락이다.
- 락 경쟁이 심해져 컨텍스트 스위칭이 많아질 수록 성능에 영향을 줄 수 있다.

#### 락 유지 시간 줄이기
- 락 유지 시간을 줄이면 락 경쟁을 최소화할 수 있다.
- 락 유지 시간을 줄이기 위해 락이 꼭 필요한 부분에만 락을 걸도록 하고 I/O 작업 같이 시간이 소모되는 작업엔 락을 걸지 않도록 주의하자.

#### 락 분할
- 락 유지 시간을 줄이는 방법으로 락 분할이 있다.
- 여려 변수들을 하나의 락으로 잡기보다 각각 분리해서 락을 잡도록하여 유지 시간을 줄여 경쟁을 줄이는 방식이다.

#### 락 스트라이핑
- 하나의 독립적인 변수에도 특정 크기의 단위로 락을 쪼개어 관리하는 방법이다.
- ConcurrentHashMap은 16개의 락 배열을 이용하여 전체 해시 범위의 1/16씩을 담당하는 락을 나누어 놓아 성능을 향상시킨다.

#### 핫 필드
- 여러개의 락이 의존하는 카운터 변수와 같은 것들을 핫 필드라고 한다.
- 이는 여러개의 락이 의존하기 때문에 락 분할같은 기법을 사용할 수 없다.
- 그러므로 핫 필드를 최소화하면 성능을 높일 수 있을 것이다.
- ConcurrentHashMap은 핫 필드를 최소화하여 성능을 높였다. 
    - 이로인해 size 메서드가 100% 정확하지 않다.
    
### 4. 성능 측정의 함정 피하기
#### 가비지 컬렉션
- GC는 언제 실행될 지 알 수 없기 때문에 테스트를 수행하동안 GC가 수행될 수도, 되지 않을 수도 있다.
- GC가 테스트에 영향을 줄 수 있었다면 GC가 실행되지 않은 경우 제대로된 테스트가 되지 않을 것이다.
- GC를 명확히 동작하도록 하여 테스트의 정확성을 올릴 수 있게 하자.

#### 동적 컴파일
- C, C++은 정적 컴파일된 상태로 실행하지만, 자바의 경우 바이트코드를 동적으로 컴파일 하기 떄문에 성능 측정이 더 까다롭다.
    - 자바는 JIT 컴파일러가 자주 사용되는 구문들을 기계어로 컴파일하여 재사용한다. 

## 명시적인 락
### 1. ReentrantLock
- 자바 1.5부터 제공하는 Lock 인터페이스 구현체로 해당 락을 잡으면 synchronized 블록에 진입한 것과 동일한 효과를 가진다.
- synchronized는 블럭을 벗어나면 자동으로 락을 해제하지만 명시적인 락은 그렇지 않으므로 반드시 락을 직접 해제해줘야 한다.

#### 시간 제한이 있는 락 확보 방법
```java
public boolean trySendOnSharedLine(String message, long timeout, TimeUnit unit) throws InterruptedException {
    long nanosToLock = unit.toNanos(timeout) - estimatedNanosToSend(message);
    if (!lock.tryLock(nanosToLock, TimeUnit.NANOSECONDS)) {
        return false;
    }
    
    try {
        return sendOnSharedLine2(message);
    } finally {
        lock.unlock();
    }
}
```
- 명시적인 락을 락 폴링에 시간제한을 두어 순서로 인한 데드락을 회피하게 할 수 있다.

#### 인터럽트를 걸 수 있는 락 확보 방법
```java
// 인터럽트를 걸 수 있는 락을 확보한다.
public boolean sendOnSharedLine(String message) throws InterruptedException {
    lock.lockInterruptibly();
    
    try {
        // 락 대기 상태 도중에 인터럽트를 걸 수 있으므로 해당 기능이 수행되지 않을 수 있다. 
        return cancellableSencOnSharedLine(message);
    } finally {
        lock.unlock();
    }
}
```
- 락을 확보하기 위해 대기 상태에 들어간 스레드에 인터럽트를 걸어 중지 요청을 보낼 수 있다.

### 2. 공정성
- ReentrantLock은 기본적으로 불공정 락 방법을 제공하며, 공정한 락 방법으로 변경 가능하다.
    - 불공적 락 방법은 순서가 없이 락을 확보하는 것, 공정한 방식은 요청한 순서대로 락을 확보하는 것이다.
    - 보통 불공정 방식이 빠르다.

#### 불공정한 방법이 빠른 이유
- 대기 상태에 있는 스레드가 다시 실행 상태로 돌아가 실행되기 까지는 많은 시간이 소모된다.
- A가 해제하려고 할 때 B는 대기 상태에 있었고 C가 이제 막 락을 확보하려고 시도한다면 B보단 C에게 락을 주는게 성능에 좋을 것이다.

### 3. synchronized(암묵적인 락) vs ReentrantLock(명시적인 락)
- ReentrantLock은 타임 아웃, 인터럽트 가능등 다양한 기능을 제공하여 유연성을 가진다.
- 하지만 synchronized는 명시적인 락 보다 상당한 장점을 가진다.

#### synchronized 장점
- 익숙하면서 간결
- 락을 직접 해제할 필요 X
- JVM 내부 기능으로 버전 업에 따른 성능 향상 가능성이 큼 

> 명시적인 락은 암묵적인 락으로 해결할 수 없는 복잡한 경우에만 사용하도록 하자.

### 4. Read-Write 락
- ReentrantLock은 mutual exlustion 락이므로 한 시점에 한 스레드만 락을 가질 수 있다.
- 반면 ReadWriteLock의 경우 readLock, writeLock을 구분하여 readLock의 경우 가시성 보장만을 제공해 성능을 높일 수 있다.

```java
public interface ReadWriteLock {
    Lock readLock();
    Lock writeLock();
} 
```

## 자바 메모리 모델
- 멀티프로세스 시스템에서 각 프로세서 안에는 개별적인 캐시 메모리를 보유하고 있다.
- 즉 일반적인 경우 프로세스 간의 캐시 데이터 일관성은 보장받지 못한다.
- 메모리 내용을 일관성 있게 프로세서간에 공유하기 위해선 하드웨어에 맞는 특별한 명령어(메모리 배리어)를 사용하여야 한다.
- 하지만 자바에선 자체적인 메모리 모델인 JVM을 구성하고 JVM 내부적으로 이를 처리해주고 있기 때문에 하드웨어 메모리에 대해 신경쓰지 않아도 된다.
- 자바에선 단지 프로그램 내부의 동기화 기법에만 집중하면 된다.

### 1. 늦은 초기화 기법
#### 더블 체크 락
```java
public class DoubleCheckedLocking {
    private static Resource resource;
    
    public static Resource getInstance() {
        if (resource == null) {
            synchronized (DoubleCheckedLocking.class) {
                if (resource == null) {
                    resource = new Resource();
                }
            }
        }
        return resource;
    }
}
```
- 이 방식은 객체 생성자가 초기화 안전성을 보장하지 않는다면 스레드 안전성을 지키지 못한다.
    - resource 자체는 null을 반환하지 않겠지만 resouce 객체 내부의 상태들은 올바르지 않은 값을 가질 수 있다.
    
#### 홀더 활용
```
public class EngerInitialization {
    private static class ResourceHolder {
        public static Resource resource = new Resource();
    }
    
    public static Resource getResource() {
        return ResourceHolder.resource;
    }
}
```
- JVM은 Holder 클래스를 사용하기 전 까지 클래스 초기화를 하지 않으므로 getResource()를 호출하기 전 까지 클래스를 초기화 하지 않는다. 
