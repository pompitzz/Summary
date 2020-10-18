# Chapter 7. 객체 분해
- 객체지향에선 데이터를 중심으로 데이터 추상화와 프러시저 추상화를 통합한 클래스를 이용해 시스템을 분해한다.
- 이 방식은 전통적인 기능 분해 방법인 프로시저 추상화에 집중한 방식보다 효과적이다.

## 프로시저 추상화와 기능 분해
- 데이터를 추상화하지 않고 프로시저만을 추상화 하는 전통적인 방식은 하향식 접근법을 따르게 된다.
- 최상위 기능을 메인함수에 정해놓고 점점 더 작은 단계로 분해하는 방식이다. 
- 이러한 하향식 접근방식은 추상화되지 않은 데이터에 의존하기 때문에 데이터의 변경에 영향이 크고, 요구 사항이 추가될 때 마다 메인함수의 수정이 필요해질 수 있다.

> 하향식 접근방식은 이미 문제가 해결되었고, 그 문제를 해결해야만 하는 로직을 작성할때 적합하다. 알고리즘이 대표적인 예시이고, 실제 프로덕션에서 동작하는 거대한 소프트웨어에는 알맞지 않다.


## 모듈
- **정보 은닉**은 시스템을 모듈 단위로 분해하기 위한 기본 원리로 시스템에서 자주 변경되는 부분을 상대적으로 덜 변경되는 인터페이스 뒤로 감추는 것이다.
- 모듈을 통해 복잡성과 변경 가능성을 감추어 시스템을 더 안정적으로 만들 수 있다.

### 모듈의 장점과 한계
- 모듈을 통해 전역 변수와 전역 함수를 제거할 수 있어 충돌을 방지할 수 있다.
- 정보 은닉을 통해 비즈니스 로직과 사용자 인터페이스에 대한 관심사를 분리할 수 있다.
- 모듈은 데이터를 감추고 데이터를 조작할 함수를 공개하지만 인스턴스에 대한 개념은 존재하지 않는다.
- 즉 모듈은 단지 대상 전체에 대한 추상화는 제공하지만 제각각의 인스턴스에 대한 추상화를 제공해주지 않는다. 

## 추상 데이터 타입
- 추상 데이터 타입은 프로시저 추상화가 아닌 데이터 추상화를 기반으로 구성된다.
- 추상 데이터 타입은 커스텀한 타입 정의가 가능하고, 타입에 대해 인스턴스화 할 수 있다.

> 추상 데이터 타입은 데이터와 기능을 분리해서 바라보므로 시스템의 상태만을 저장한다. 추상 데이터 타입을 이용해 기능을 구현하는건 외부에 존재하게 된다.

## 클래스
- 추상 데이터 타입과 클래스의 가장 큰 차이는 상속과 다형성을 지원하는데 있다.
- 클래스는 상속과 다형성을 활용할 수 있기 때문에 각 타입에 대해 **절차를 추상화 한다.**
    - 사용처에서는 각 타입을 구체적으로 알아야 하지만 절차가 추상화되었기 때문에 해당 타입의 오퍼레이션이 실제로 수행되는 동작을 알 수 없다.
- 추상 데이터 타입은 하나의 큰 타입안에 전체 타입을 감추고 오퍼레이션을 기준으로 **타입을 추상화 한다.**
    - 하나의 오퍼레이션에서 각 세부적인 타입별로 서로 다른 로직이 수행되기 때문에 구체적인 타입을 알 수 없다.  


### 추상 데이터 타입 VS 클래스
- 이 둘은 서로 다른 특징을 가지고 있기 때문에 상황에 따라 유연하게 사용할 수 있다.
- 타입 추가가 빈번할 경우 클래스를 사용하는것이 유용하다.
    - 클래스의 경우 절차가 추상화되었기 때문에 타입이 추가되어도 새로운 타입이 절차를 구현하는데 다른 타입들에게 영향을 주지 않는다.
    - 반면 추상 데이터 타입은 타입이 추상화되었기 때문에 타입이 추가되면 모든 오퍼레이션에서 새로운 타입에 대한 조건을 추가해줘야 한다.
- 오퍼레이션 추가가 빈번할 경우 추상 데이터 타입이 유용하다.
    - 추상 데이터 타입의 경우 타입이 추상화되었기 때문에 새로운 오퍼레이션이 추가되어도 기존 오퍼레이션의 변경이 필요가 없다.
    - 반면 클래스는 절차가 추상화되었기 때문에 오퍼레이션이 추가되면 관련된 모든 타입이 해당 오퍼레이션을 구현해야 한다.