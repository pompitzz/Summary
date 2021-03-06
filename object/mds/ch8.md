# Chapter 8. 의존성 관리하기
- 협력은 필수적이나 과도한 협력은 위험하다.
- 객체지향 설계의 핵심은 협력을 위해 필요한 의존성을 유지하되, 변경을 방해하는 의존성은 제거하는데 있다.

> 어떤 하나가 다른 하나를 의존한다는 것은 의존하는 대상이 변경될 때 함께 변경될 수 있음을 의미한다.

## 의존성 이해하기
### 의존성 전이
- 의존하는 대상이 의존하고 있는 대상도 간접적으로 의존 한다고할 수 있다.
- 의존성은 함께 변경될 수 있는 가능성을 의미하므로 모든 경우에 의존성이 전이 되진 않는다.
    - **캡슐화가 잘 되어 있다면 의존성 전이는 일어나지 않을 것이다.**   

### 런타임 의존성, 컴파일 타임 의존성
- 객체지향에서 런타임 시점엔 객체를 중심으로 움직이며 객체간의 의존성이 존재한다.
- 하지만 컴파일 타임 이점엔 클래스가 중심이되어 클래스간에 의존성이 존재하게 된다.

> 이러한 차이가 유연하고 재사용한 코드를 설계할 수 있도록 해준다.
> - 컴파일 타임과 런타임의 의존성의 거리가 멀수록 설계가 유연해지고 재사용 가능해지지만 코드를 이해하기는 더 어려워 질 것이다.

## 유연한 설계
- 바람직한 의존성은 동일한 의존성에 대해 재사용이 가능해지며 낮은 결합도를 가지게 된다.
- 의존하는 대상에대해 최소한의 정보만 알도록하여 **컨텍스트를 독립**시키면 바람직한 의존성을 가지게할 수 있다.

### 의존성은 명시적으로 표현하자
- 의존성은 생성자, 수정자 메서드, 메서드 파라미터등 명시적으로 나타내야 한다.
- **의존성을 숨기게되면 의존성 파악을 위해 내부 구현 깊숙히 들어가야 하며, 의존성에 대한 재사용성이 떨어진다.**

### 생성과 사용의 책임을 분리하자
- 의존하는 대상을 클래스 내부에서 직접 new로 생성하는건 구체적인 클래스에 의존하므로 좋지 않다.
- 생성과 사용의 책임을 분리하고, 의존성을 명시적으로 들어내면 구체적인 클래스가 아닌 추상적인 클래스에 의존하도록하여 설계를 유연하게 만들 수 있다.


> 유연하고 재사용 가능한 설계는 객체가 **어떻게** 하는지가 아닌 **무엇을**하는지를 표현하는 클래스들로 구성되어야 한다.
> - 이는 코드에 드러난 로직을 해석할 필요없이 객체의 행동을 쉽게 분석할 수 있게 된다. 

