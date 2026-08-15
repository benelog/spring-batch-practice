# simpleTaskJob
- POJO(`PrintTask`)를 `<batch:tasklet ref="printTask" method="run"/>`로 실행한다.
- step scope 빈에 `#{jobParameters['message']}`로 job parameter를 주입한다.

2014년에 별도 저장소(`benelog/batch-experiments`)에서 만든 Spring Batch 2.2 + XML 설정 시절의 예제다. 저장소를 정리하면서 이력과 함께 이곳으로 옮겼다.

## How to run
- Options 1 : mvn test
- Options 2 : mvn exec:java
- Options 3 : mvn test -Dtest=SimpleTaskJobTest
