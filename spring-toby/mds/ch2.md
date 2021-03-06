# 1-2장 테스트
- 처음 작성한 혹은 기존의 코드가 변경될 때 테스트가 있다면 해당 코드를 변경하더라도 테스트를 통해 코드의 변경이 시스템의 오류를 유발하는지 간단히 확인할 수 있다.

### 네거티브 테스트의 중요성
- 개발자는 성공하는 테스트만 작성하기 때문에 실제 발생할 수 있는 문제들을 테스트 작성에서 피하게 될 수 있다.
- 비정상적으로 시스템이 작동되도록 하여 예상된 예외가 발생하는지 확인하는 네거티브 테스트를 항상 만들어야 한다.

### TDD의 장점
- 테스트를 먼저 작성하면 테스트를 빼먹을 일이 없다.
- 테스트와 기능 구현을 동시에 완료할 수 있다.
- 테스트를 작성하면서 기존에 생각했던 방법보다 더 좋은 방법이 떠오를 수 있다.
 

### 침투적 기술과 비침투적 기술
- 침투적 기술은 기술을 적용했을 때 코드내에 기술과 관련된 의존성이 명확히 들어나는 것이다.  
    - 어떤 기술을 사용하기 위해 특정 인터페이스나 클래스를 강제로 사용해야 하는 것 처럼
- 비침투적 기술은 코드내에 아무런 영향을 주지 않고도 해당 기술을 사용할 수 있게 되는 것이다.
    - 따라서 코드엔 외부의 기술의 영향을 받지않고 순수한 코드로 남을 수 있게 해준다.
    
> 스프링은 비침투적 기술의 대표적인 예이다. 

### 버그 테스트
- 버그가 발생하였다면 테스트가 제대로 작성되지 않은 확률이 높다.
- 버그가 발생했을 때 바로 코드를 수정하지말고, 버그 수정전엔 테스트가 실패하고 버그 수정 후에 테스트가 성공할 수 있는 테스트 코드를 먼저 작성하자.
    - 이를 통해 해당 버그를 명확하게 분석할 수 있게 해줘 테스트의 완성도를 높일 수 있다.
