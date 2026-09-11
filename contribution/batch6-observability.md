# 스프링 배치 6 관측(Observability) 조사 기록

2026-07-19 15장 검증 과정에서 발견한 스프링 배치 6.0.4 + 스프링 부트 4.1.0의 관측 공백을 재검토하기 위한 기록이다.
모든 내용은 실측으로 확인했고, 2026-07-19 업스트림 이슈로 게시했다: <https://github.com/spring-projects/spring-batch/issues/5463>

## 2026-09-01 후속: 6.0.6에서 해소

`launch.count` 누락은 6.0.6에서 고쳐졌다. PR #5464가 ``BatchObservabilityBeanPostProcessor``의
`bean instanceof TaskExecutorJobOperator` 검사를 프락시까지 인식하도록 바꿔서(원인 2), 빈 등록
순서에 기대던 우회책(원인 3의 대응)이 필요 없어졌다.

실측(`@EnableBatchProcessing` + 액추에이터, `ObservationRegistry` 빈 없음):

| 배치 버전 | `launch.count` | `jobOperator`의 레지스트리 |
| --- | --- | --- |
| 6.0.5 | 누락 | `ObservationRegistry.NOOP` |
| 6.0.6 | 기록됨 | `SimpleObservationRegistry` |

따라서 아래 '확정된 사실'의 1번은 **6.0.5 이하에만 해당한다.** 2번(스텝 빈 전제)과
3번(부트에서 문서 코드의 이중 기록)은 6.0.6에서도 그대로다. 책의 14장 ``BatchConfig``와
예제 리포 `ch14-modules`에서 우회용 `ObservationRegistry` 빈을 제거했다.

## 확정된 사실

### 1. `spring.batch.job.launch.count`는 대부분의 일반 구성에서 기록되지 않는다 (결정적 결함)

- 공식 레퍼런스(Micrometer support)는 이 메트릭을 제공 목록에 명시하고, `ObservationRegistry` 빈을 정의하면 메트릭이 활성화된다고 안내한다.
- 실측: 다른 `spring.batch.*` 메트릭은 모두 기록되지만 이 메트릭만 항상 누락된다. 문서의 코드를 그대로 넣어도 같다.
- 원인 1: `DefaultBatchConfiguration#getObservationRegistry()`가 `ObservationRegistry.NOOP`을 하드코딩으로 반환해 `JobOperatorFactoryBean`에 넘긴다. 부트의 `SpringBootBatchDefaultConfiguration`은 이를 오버라이드하지 않는다. 따라서 부트 자동 구성 경로에서는 사용자가 빈을 선언해도 'jobOperator'에 연결되지 않는다.
- 원인 2: 폴백인 `BatchObservabilityBeanPostProcessor`는 `bean instanceof TaskExecutorJobOperator` 검사를 하는데, 'jobOperator' 빈은 `JobOperatorFactoryBean`이 만든 JDK 동적 프록시(트랜잭션 어드바이스 + `JobOperator` 인터페이스)라 항상 거짓이다. 실패는 DEBUG 로그 한 줄로만 남는다.
- 원인 3(`@EnableBatchProcessing` 경로): `BatchRegistrar`는 레지스트라 실행 시점에 'observationRegistry' 빈 정의가 이미 있을 때만 참조를 걸어 준다. 부트 자동 구성(deferred import)은 그보다 늦게 등록돼 조건에서 탈락한다.
- 우회책: `@EnableBatchProcessing`이 붙은 설정 클래스에 `@Bean ObservationRegistry observationRegistry() { return ObservationRegistry.create(); }`를 선언하면 등록 순서상 참조가 연결된다. 이 우회책은 `@EnableBatchProcessing` 경로에서만 유효하고, 부트 자동 구성 경로에서는 통하지 않는다.
- 우회책의 이름 조건(6.0.4 바이트코드로 확인): `BatchRegistrar`는 `containsBeanDefinition(observationRegistryRef())` 검사를 통과할 때만 참조를 추가한다. `observationRegistryRef`의 기본값은 'observationRegistry'이므로 `@Bean` 메서드 이름이 이와 일치해야 하고, 다른 이름을 쓰려면 속성으로 지정해야 한다.
- 순서 의존성은 부트 없이도 존재한다(2026-07-19 실측, `issue-batch-observability-plain`): 순수 스프링에서 같은 빈을 `@EnableBatchProcessing`이 없는 별도 설정 클래스에 선언하면, 그 클래스를 먼저 등록했을 때만 launch.count가 기록되고(시나리오 A) 나중에 등록하면 누락된다(시나리오 B). 같은 클래스 안 선언(시나리오 C)은 항상 기록된다. 부트의 deferred import는 B의 순서를 항상 재현되게 만들 뿐이다.
- 부트에서도 항상 누락되는 것은 아니다(2026-07-22 실측, ch14-modules). 같은 `BatchConfig`를 임포트하는데도 모듈에 따라 결과가 갈린다. `ObservationRegistry` 빈을 지우고 잡을 실행한 뒤 확인하면 'admin-cli'는 누락(Push Gateway에 `spring_batch_job_launch_count*` 0건, 다른 배치 메트릭은 정상)이고 'admin-web'은 정상 기록된다. 웹 모듈에 딸려 오는 자동 구성이 많아 'observationRegistry' 빈 정의가 `BatchRegistrar` 실행 전에 등록되기 때문으로 보인다. 원인 3의 "부트 자동 구성은 항상 늦게 등록된다"는 서술은 모듈 구성에 따라 뒤집힐 수 있다.
- 검증 시 두 가지 함정에 주의한다(2026-07-22). 첫째, `spring.batch.job.launch.count`는 첫 잡 실행 시점에 생성되므로 기동 직후 `/actuator/metrics`를 조회하면 빈 유무와 무관하게 목록에 없다. 반드시 잡을 한 번 실행한 뒤 확인한다. 둘째, 진단 코드가 관측 대상을 바꾼다. `BatchConfig`에 `static BeanFactoryPostProcessor`를 `@Bean`으로 추가하면 빈 정의 등록 순서가 달라져서, 진단을 넣기 전에는 기록되던 'admin-web'이 진단을 넣으면 NOOP으로 관측된다.

### 2. 스텝 메트릭은 스텝이 빈일 때만 기록된다

- `BatchObservabilityBeanPostProcessor`가 잡·스텝 "빈"에만 레지스트리를 주입하는 구조다.
- 실측(ch05 예제): 스텝을 잡 안의 지역 변수로 만들면 `spring.batch.job`만 기록되고, 스텝을 `@Bean`으로 바꾸면 `spring.batch.step`이 나타난다.
- 이 전제는 공식 문서에 없다.

### 3. 공식 문서의 `ObservationRegistry` 코드는 부트에서 이중 기록을 일으킨다

- 문서 코드는 `DefaultMeterObservationHandler`를 수동 등록한다(부트 없는 환경용으로는 올바름).
- 부트 안에서는 `ObservationRegistryPostProcessor`가 모든 `ObservationRegistry` 빈에 핸들러를 또 붙여서, 잡 1회 실행에 `spring.batch.job` 타이머 count가 2로 기록된다(실측).
- 부트가 붙이는 핸들러는 `MetricsAutoConfiguration`이 자동 구성한 `MeterRegistry`로 만든 `DefaultMeterObservationHandler` 빈이다(부트 4.1.0 바이트코드로 확인). 문서 코드가 수동 등록하는 것과 같은 클래스이므로, 부트에서 `ObservationRegistry.create()`만 선언한 빈은 문서 코드와 같은 효과를 내는 더 짧은 코드다.
- 부트에서는 핸들러 없이 `ObservationRegistry.create()`만 선언해야 정확히 1회씩 기록된다.

### 4. 문서 공백

- 스프링 배치 레퍼런스: launch.count 누락 현상, 스텝 빈 전제, 부트에서의 이중 기록 모두 미기재.
- 스프링 부트 레퍼런스: 배치 메트릭 절은 스프링 배치 문서로 링크만 한다.

## 재현과 검증 방법

- 최소 재현 프로젝트: <https://github.com/benelog/spring-batch-practice/tree/main/issue-batch-observability>
    - `./gradlew bootRun` : launch.count 누락 + operator NOOP 진단 출력
    - `./gradlew bootRun --args='--spring.profiles.active=custom-registry'` : 문서 코드 그대로 넣은 변형. launch.count 여전히 누락 + 타이머 count=2(이중 기록)
- 비부트 재현 프로젝트: <https://github.com/benelog/spring-batch-practice/tree/main/issue-batch-observability-plain>
    - `./gradlew run` : 순수 스프링 컨텍스트 3개로 등록 순서 시나리오 실행(A 먼저 등록=기록, B 나중 등록=누락, C 같은 클래스=기록)
- 책 예제 검증: ch14-modules에서 `java -DjobName=settleOrderJob -jar admin-cli/build/libs/admin-cli-1.0.jar id=<유니크>` 실행 후 Push Gateway(9091)에서 `spring_batch_job_launch_count_seconds_count` 확인. 수정 전 0/20, `BatchConfig`에 빈 선언 후 20/20.
- 주의: 메트릭 검증은 `Metrics.globalRegistry`가 아니라 `MeterRegistry` 빈이나 Actuator 엔드포인트로 해야 한다. 관측 핸들러는 부트의 `MeterRegistry` 빈에 직접 기록하므로 전역 컴포지트에는 보이지 않는다(이전에 "간헐 누락"으로 오판한 원인).

## 반영 내역

- 예제 리포(benelog/spring-batch-practice)
    - `ch14-modules/batch-support/BatchConfig`에 `ObservationRegistry` 빈 추가 (launch.count 복구)
    - `issue-batch-observability/` 재현 프로젝트 추가
- 책(wikibook/springbatch)
    - 15장 기본 메트릭 절: 스텝 빈 전제, launch.count 누락 원인·복구, 부트 경로 재현, 문서 코드 이중 기록 (커밋 8367990, 15a9b0b, b665034)
    - 15장 우회책 문단 보강: 핵심이 `@EnableBatchProcessing` 클래스 안의 빈 선언임을 명시하고, 위치·이름 조건과 `observationRegistryRef` 속성 추가
    - 14장 `BatchConfig` 코드 목록 동기화 (콜아웃 <4>)
    - 5장·13장 비빈 스텝 예제에 스텝 메트릭 주의 (커밋 c34221a)

## 업스트림 이슈 (2026-07-19 게시: [#5463](https://github.com/spring-projects/spring-batch/issues/5463))

수정 PR도 같은 날 게시: [#5464](https://github.com/spring-projects/spring-batch/pull/5464)

- 수정 방식: `BatchObservabilityBeanPostProcessor`에서 `AopProxyUtils.getSingletonTarget()`으로 프록시를 언랩한 뒤 타입 검사. 단위 테스트 2건(일반·프록시 오퍼레이터) 추가, 프록시 테스트는 main에서 실패(레드) 확인.
- 제안 3개 중 BPP 수정을 선택한 근거: 한 곳 수정으로 모든 구성 경로가 해결된다. `DefaultBatchConfiguration#getObservationRegistry()` 수정(제안 1)은 `@EnableBatchProcessing` 경로의 순서 문제를 해결하지 못한다. BPP는 타입 기반 `getBean` 조회라 순서·이름 제약도 함께 사라진다.
- 패치 스냅숏(6.0.5-SNAPSHOT) 종단 검증: 부트 재현 프로젝트에서 launch.count 기록·오퍼레이터 레지스트리 주입(NOOP → SimpleObservationRegistry)·타이머 count=1 확인, 비부트 3개 시나리오(A/B/C) 전부 기록 확인. 검증 후 재현 프로젝트 build.gradle은 6.0.4로 원복.
- 커밋은 DCO 서명(Signed-off-by)만 포함, 코오서 표시 없음. 빌드는 CI 기준 JDK 25 필요(JDK 17·21은 Error Prone 크래시).
- 게시 후 같은 날 워크어라운드 절을 편집했다: 예제 코드를 bare `ObservationRegistry.create()`에서 레퍼런스 문서의 빈 정의(핸들러 등록 포함)를 `@EnableBatchProcessing` 클래스 안에 배치한 형태로 교체하고, 부트 불릿을 "핸들러를 생략하라"로 뒤집었다. bare `create()`는 부트가 핸들러를 붙여 줄 때만 동작하는 부트 전용 형태(책 14장의 특수성)라서, 비부트 독자가 코드 블록만 복사하면 조용히 아무것도 기록되지 않기 때문이다. 레퍼런스 스니펫에는 클래스 컨텍스트가 없어 배치 위치·이름 조건이 드러나지 않는다는 점도 이 형태가 이슈의 핵심 주장을 더 선명하게 만든다.

대상: spring-projects/spring-batch. 관련 이력: #4222(관측 BPP 도입), #4226(launch 카운터 추가). 동일 이슈는 없음(2026-07-19 검색 기준).

### 이슈 개정 (2026-07-22)

게시 후 코멘트·리뷰가 0건인 상태에서 제목과 본문을 개정했다. 메인테이너가 웹 애플리케이션으로 재현을 시도하면 "재현 안 됨"으로 닫힐 위험이 있어서다.

- 제목: "is never recorded unless an `ObservationRegistry` bean is declared in the `@EnableBatchProcessing` class" → "is silently missing: the `JobOperator` keeps `ObservationRegistry.NOOP` on most configuration paths"
- 도입부: "In every other setup ... never recorded"를 빼고 두 경로를 구분했다. 부트 자동 구성 경로는 "never works", `@EnableBatchProcessing` 경로는 "depends on bean definition order".
- 원인 3: 순서 조건이지 확정적 실패가 아니라는 문장을 덧붙였다.
- 새 절 'The ordering condition can go either way': 같은 빌드의 CLI 모듈은 누락되고 웹 모듈은 기록되는 실측과, 재현 시 함정 두 가지(미터는 첫 잡 실행 때 생성됨, 진단용 `static BeanFactoryPostProcessor`가 순서를 바꿈)를 넣었다.
- Workaround: "restores the metric" → "makes the wiring independent of registration order".
- Possible fixes: BPP 수정안이 순서·이름 제약까지 없앤다는 문장을 추가했다.
- PR #5464 본문: "never recorded on any configuration path"를 경로별 서술로 바꿨다.

아래는 2026-07-19에 처음 게시한 본문이다(2026-07-22 개정 전 원문).

> **Title**: `spring.batch.job.launch.count` is never recorded unless an `ObservationRegistry` bean is declared in the `@EnableBatchProcessing` class
>
> **Version**: Spring Batch 6.0.4 (Spring Boot 4.1.0)
>
> The reference documentation lists `spring.batch.job.launch.count` among the provided metrics and shows an `ObservationRegistry` bean definition to enable metrics collection. In practice, the `JobOperator` does not receive the `ObservationRegistry` unless the bean is declared inside the configuration class annotated with `@EnableBatchProcessing`. In every other setup (including Spring Boot's batch auto-configuration and the documentation's exact snippet), this metric is never recorded, while all other `spring.batch.*` metrics are:
>
> 1. `DefaultBatchConfiguration#getObservationRegistry()` returns a hardcoded `ObservationRegistry.NOOP` and passes it to `JobOperatorFactoryBean`. Spring Boot's `SpringBootBatchDefaultConfiguration` does not override it, so declaring an `ObservationRegistry` bean has no effect on the operator.
> 2. The fallback `BatchObservabilityBeanPostProcessor` checks `bean instanceof TaskExecutorJobOperator`, but the `jobOperator` bean is a JDK dynamic proxy created by `JobOperatorFactoryBean`, so the check is always false and the operator is silently skipped (DEBUG log only).
> 3. With `@EnableBatchProcessing`, `BatchRegistrar` adds the `observationRegistry` property reference only when a bean definition with the name from `observationRegistryRef` (default: `observationRegistry`) already exists while the registrar runs. Spring Boot's auto-configured `ObservationRegistry` is registered later (deferred import), so the reference is not added.
>
> **Minimal reproducer**: <https://github.com/benelog/spring-batch-practice/tree/main/issue-batch-observability> (`./gradlew bootRun`)
>
> ```
> [DIAG] spring.batch meters recorded = [spring.batch.job, spring.batch.job.active, spring.batch.step, spring.batch.step.active]
> [DIAG] spring.batch.job.launch.count present = false
> [DIAG] jobOperator target class = ...TaskExecutorJobOperator, observationRegistry = ObservationRegistry.NOOP
> ```
>
> The `custom-registry` profile uses the exact `ObservationRegistry` snippet from the reference documentation: `launch.count` is still missing, and as a side effect every `spring.batch.*` metric is recorded twice in a Boot application (the manually registered `DefaultMeterObservationHandler` plus the handler Boot attaches to every `ObservationRegistry` bean).
>
> A plain Spring (non-Boot) reproducer in the same repository ([issue-batch-observability-plain](https://github.com/benelog/spring-batch-practice/tree/main/issue-batch-observability-plain), `./gradlew run`) shows that the ordering sensitivity of cause 3 exists without Spring Boot: the same `ObservationRegistry` bean declared in a separate configuration class is wired only when that class is registered before the `@EnableBatchProcessing` class. Boot's deferred auto-configuration simply makes this ordering always fail.
>
> **Workaround (application side)**
>
> Placing the `ObservationRegistry` bean definition from the reference documentation inside the `@EnableBatchProcessing` configuration class restores the metric:
>
> ```java
> @Configuration
> @EnableBatchProcessing
> public class BatchConfig {
>
>   @Bean
>   public ObservationRegistry observationRegistry(MeterRegistry meterRegistry) {
>     ObservationRegistry observationRegistry = ObservationRegistry.create();
>     observationRegistry.observationConfig()
>         .observationHandler(new DefaultMeterObservationHandler(meterRegistry));
>     return observationRegistry;
>   }
> }
> ```
>
> - The bean definition is the documentation's snippet unchanged; what makes it work are two requirements the documentation does not mention (its snippet shows no surrounding class): the bean definition must already exist when `BatchRegistrar` runs, which declaring it inside the `@EnableBatchProcessing` class guarantees, and the bean name must match `observationRegistryRef` (default: `observationRegistry`).
> - In a Spring Boot application, the manual `DefaultMeterObservationHandler` must be omitted (`return ObservationRegistry.create();` alone is enough): Boot auto-configures a `DefaultMeterObservationHandler` bean wired to the auto-configured `MeterRegistry` (`MetricsAutoConfiguration`), and `ObservationRegistryPostProcessor` attaches it to every `ObservationRegistry` bean. With the handler-registering snippet above, the same handler class ends up attached twice and every `spring.batch.*` metric is recorded twice.
> - I could not find a workaround for the Boot auto-configuration path (no `@EnableBatchProcessing`): `DefaultBatchConfiguration#getObservationRegistry()` hardcodes NOOP regardless of user-defined beans.
>
> **Possible fixes**
>
> - Resolve the registry from the application context in `DefaultBatchConfiguration#getObservationRegistry()` instead of hardcoding NOOP, e.g. `getBeanProvider(ObservationRegistry.class).getIfUnique(() -> ObservationRegistry.NOOP)`. This would fix the Boot auto-configuration path as well.
> - Make `BatchObservabilityBeanPostProcessor` proxy-aware, e.g. unwrap with `AopProxyUtils.getSingletonTarget(bean)` before the `instanceof` checks, so the proxied `jobOperator` bean can be handled on every path.
> - Documentation: note that in a Spring Boot application the `ObservationRegistry` bean should be declared without manually registering `DefaultMeterObservationHandler` (Boot attaches handlers automatically); the current snippet leads to double-counted metrics.
>
> Related: #4222, #4226

## 재검토 포인트

- 이슈를 하나로 올릴지, launch.count 결함과 문서 이중 기록 문제를 분리해서 올릴지(후자는 문서 이슈로 spring-batch 또는 스니펫 위치에 따라 분리 가능). → 2026-07-19 하나로 게시(#5463). 이중 기록은 Possible fixes의 문서 항목으로 포함했다.
- 게시 대상 검토(2026-07-19): 스프링 부트 쪽에 올리는 안을 검토했으나 spring-batch 게시를 권고. 세 원인 코드(`DefaultBatchConfiguration`·`BatchObservabilityBeanPostProcessor`·`BatchRegistrar`)와 해당 문서가 모두 spring-batch 소유다. `BatchRegistrar`의 등록 순서 민감성은 부트 없는 `@EnableBatchProcessing` 구성에서도 존재한다(`issue-batch-observability-plain`으로 실측 확인). 다른 설정 클래스에 선언한 빈은 그 클래스가 먼저 처리될 때만 연결되므로 순서에 달렸고, 부트의 deferred import는 이 실패를 항상 재현되게 만들 뿐이다. 배치 쪽 수정 제안(getBeanProvider 폴백)이 부트 경로까지 해결한다. 배치 팀이 NOOP 기본값을 의도로 판단하면 그때 부트에 `SpringBootBatchDefaultConfiguration` 오버라이드를 요청하는 후속 이슈를 검토한다.
- `BatchConfig`에 빈을 선언하는 책의 우회책이 향후 스프링 배치 수정으로 불필요해질 수 있다. 프레임워크가 고치면 15장 해설의 시제(현재 버전 기준임)를 확인할 것.
- 이슈 본문의 "In every other setup ... this metric is never recorded"는 2026-07-22 실측에 비추면 과한 단정이었다. 모듈 구성에 따라 부트 자동 구성 빈이 먼저 등록되어 연결되는 경우가 있다(admin-web). 같은 날 이슈 제목·본문과 PR 본문을 개정해 반영했다(아래 '이슈 개정' 참고). 결함의 본질(순서에 달렸다는 것)은 그대로여서 제안한 수정 방향은 바뀌지 않았다.
- Push Gateway는 푸시·삭제를 202로 받아 비동기 큐로 처리하므로, 검증 시 실행 직후 조회하면 반영 전일 수 있다(수 초 대기).
