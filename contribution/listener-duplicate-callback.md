# 리스너 콜백 이중 호출 (스프링 배치 6.0.4)

12장(event-listener.adoc)을 6.0.4로 검증하다가 찾은 버그다.
2026-07-23에 스프링 배치에 보고했다.

- 이슈: https://github.com/spring-projects/spring-batch/issues/5466
- 수정 PR: https://github.com/spring-projects/spring-batch/pull/5467 (브랜치 `GH-5466`, 로컬 클론 `~/source/benelog/spring-batch`)
- 다른 사람이 올린 같은 수정 PR: https://github.com/spring-projects/spring-batch/pull/5478 (아래 '중복 PR' 참고)

## 재현 프로젝트

`~/source/benelog/spring-batch-practice/issue-duplicate-listener`

```bash
cd ~/source/benelog/spring-batch-practice/issue-duplicate-listener
./gradlew bootRun   # 두 잡을 돌려 콜백 호출 목록을 출력
./gradlew test      # 대조군은 통과하고 문제 케이스는 실패
```

이슈로 올릴 영어 본문은 그 프로젝트의 `README.md`에 있다. 원인 분석, 5.2와의 비교, 수정
제안, 우회 방법까지 담았으므로 그대로 붙여 넣으면 된다.

## 요약

`ItemReader`, `ItemProcessor`, `ItemWriter` 구현 클래스가

1. `StepListener` 하위 인터페이스를 구현하면서
2. 리스너 애너테이션이 붙은 메서드도 가지고 있으면

스텝에 리스너로 두 번 등록되어 인터페이스 콜백이 아이템마다 두 번 호출된다. 애너테이션
자체(`@BeforeStep`)는 한 번만 호출되고 인터페이스 콜백만 두 배가 된다.

원인은 `ChunkOrientedStepBuilder#addAsStreamAndListener()`의 두 분기가 배타적이지 않은
것이다. `instanceof StepListener`로 원본 객체를 넣고, `StepListenerFactoryBean.isListener()`로
프록시를 또 넣는다. 그 프록시는 애너테이션뿐 아니라 인터페이스로도 콜백을 연결하므로 같은
객체가 두 번 등록된다. 5.2의 `SimpleStepBuilder#registerAsStreamsAndListeners()`는 한 분기로
처리해서 한 번만 등록했다.

## 실측

```text
애너테이션 없음: [beforeProcess:1, afterProcess:1, beforeProcess:2, afterProcess:2]
@BeforeStep 추가: [beforeStep, beforeProcess:1, beforeProcess:1, afterProcess:1, afterProcess:1,
                   beforeProcess:2, beforeProcess:2, afterProcess:2, afterProcess:2]
```

## 중복 PR (2026-08-04)

내 PR이 리뷰를 받지 못한 채 열려 있는 동안, 다른 기여자(@n-dlms)가 이슈에 원인 분석 댓글을
달고 같은 수정을 담은 PR #5478을 올렸다. 본체 코드는 두 PR이 기능적으로 같다. 둘 다 팩터리
분기를 먼저 두고 `instanceof StepListener`를 `else if`로 내리는데, 이건 내가 이슈 본문의
'Suggested fix'에 코드로 적어 둔 형태 그대로다. 실제로 다른 것은 세 가지뿐이다.

- 주석 문구. 상대 쪽이 왜 두 분기가 배타적이어야 하는지까지 설명해서 더 낫다.
- ``StepListenerFactoryBean.getListener()`` 결과를 지역 변수에 담느냐 `add()` 인자로
  인라인하느냐. 내 PR은 원본 두 줄을 그대로 두어 diff를 줄였고, 이 형태가 `SimpleStepBuilder`,
  `FaultTolerantStepBuilder`의 같은 로직과도 모양이 같다. 동작 차이는 없다.
- 테스트. 내 PR은 ``ChunkOrientedStepIntegrationTests``에 `JobOperator`로 실제 잡을 돌리는
  통합 테스트 하나를 넣었고, 상대 PR은 ``ChunkOrientedStepTests``에 `ChunkOrientedStep`을 직접
  실행하는 단위 테스트 둘(대조군 + 문제 케이스)을 넣었다. 상대 쪽에는 애너테이션이 없는
  대조군이 있고, 내 쪽은 잡 전체 배선을 태운다.

PR #5478에 중복이라는 사실과 위 차이를 정리한 댓글을 달았다. 어느 쪽이 머지되든 좋고, 내 PR을
닫거나 상대의 대조군 테스트를 내 PR에 합치는 것 모두 가능하다고 밝혔다.
https://github.com/spring-projects/spring-batch/pull/5478#issuecomment-5182920436

댓글은 PR에 달았다. 이슈 타임라인에는 두 PR이 이미 cross-reference로 걸려 있어 중복이 되고,
어느 diff를 고를지는 PR에서 판단하기 때문이다.

## 책에 반영한 것

12장 '@BeforeStep 애너테이션 활용' 절에 WARNING으로 넣었다(커밋 86f21a3). 예제의
`CallUrlProcessor`가 정확히 이 조건에 걸려서 '호출 시도'/'응답 시간' 로그가 두 줄씩 남는다.
AGENTS.md 규칙에도 "한 클래스에 인터페이스 구현과 애너테이션을 섞지 않는다"를 넣었다.
