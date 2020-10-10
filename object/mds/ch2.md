# Chapter 2. 객체지향 프로그래밍
### 객체지향 패러다임으로 전환
- 객체지향 패러다임으로 전환하기 위해선 클래스가 아닌 **객체**에 초점을 맞춰야 한다.
    - 1) 어떤 클래스가 필요한지 고민하기 전에 어떤 객체들이 필요한지 고민하라.
    - 2) 객체를 독립적인 존재가 아닌 기능을 구현하기 위해 협력하는 공동체의 일원으로 바라보자. 
    
### 메시지를 자율적으로 결정하는 객체
- 객체의 메서드를 호출하는 클라이언트 입장에서는 해당 메서드가 어떻게 구현되어있는지 알지 못한다.
- 단지 클라이언트는 메서드를 호출하고 그 결과를 받을 뿐이다.
- 그러므로 해당 메서드를 처리하기 위한 방법은 객체 스스로가 자율적으로 결정한다.

### 컴파일 타임과 런타임에 의존성 차이
- 객체지향 프로그래밍에서는 컴파일 타임과 런타임에 의존성이 달라질 수 있고 이 의존성이 다를 수록 코드를 이해하기 어려워지지만, 더 유연하고 확장 가능해진다.
- 컴파일 타임과 런타임의 의존성을 다르게 만들 수 있는 것은 객체지향의 다형적인 특성을 이용하면 된다.
- 객체의 일반 메서드는 런타임에 바인딩을 수행하는 지연 혹은 동적 바인딩을 수행하므로 이러한 기능들을 사용할 수 있다.

### 추상화와 유연성
- 추상화를 이용하면 시스템을 높은 수준에서 바라볼 수 있게 해준다. 그리고 추상화를 통해 설계와 실제 구현을 분히라여 유연한 설계를 가능하다.

### 상속과 합성
- 상속은 부모 클래스와 강한 결합이 이루어지고, 부모의 구현이 자식에게 노출되어 캡슐화를 약화시킨다.
- 그리고 부모와 자식 클래스간의 관계가 컴파일 타임에 정해지기 떄문에 설계가 유연해지지 않는다.
- 반면 합성은 인터페이스에 의존하도록하여 상속에서 생길 수 있는 문제를 모두 해결하여 결합도를 낮추고 유연할 설계를 얻을 수 있다.
 
 