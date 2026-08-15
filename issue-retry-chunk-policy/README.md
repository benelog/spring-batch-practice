# retryJob
- Reproduce issues : 'chunk-completion-policy' or 'commit-interval' with '#{jobParameters[...]}' is ignored when 'retry-limit' exists.

2014년에 별도 저장소(`benelog/batch-experiments`)에서 만든 Spring Batch 2.2 시절의 재현 프로젝트다. 저장소를 정리하면서 이력과 함께 이곳으로 옮겼다. 다른 `issue-*` 디렉터리와 달리 지금의 Spring Batch에서 확인한 문제가 아니다.

## How to run
- Options 1 : mvn test
- Options 2 : mvn exec:java
- Options 3 : mvn test -Dtest=RetryJobTest
