# Test projects for Spring-batch

2014년에 별도 저장소(`benelog/batch-experiments`)에서 만든 Spring Batch 2.x 시절 실험 프로젝트다. 저장소를 정리하면서 이력과 함께 이곳으로 옮겼다.

- [batch-retry-test](batch-retry-test) : 'chunk-completion-policy' or 'commit-interval' with '#{jobParameters[...]}' is ignored when 'retry-limit' exists.
- [tasklet-test](tasklet-test) : POJO를 `<batch:tasklet ref="..." method="..."/>`로 실행하면서 step scope 빈에 job parameter를 주입하는 예제.
