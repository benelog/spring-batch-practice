# simpleTaskJob
- POJO(`PrintTask`)를 `<batch:tasklet ref="printTask" method="run"/>`로 실행한다.
- step scope 빈에 `#{jobParameters['message']}`로 job parameter를 주입한다.

## How to run
- Options 1 : mvn test
- Options 2 : mvn exec:java
- Options 3 : mvn test -Dtest=SimpleTaskJobTest
