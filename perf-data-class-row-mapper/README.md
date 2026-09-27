# DataClassRowMapper benchmark

JMH benchmark for [spring-projects/spring-framework#37329](https://github.com/spring-projects/spring-framework/pull/37329). It reads 1,000 rows into a 10-component record through `JdbcTemplate.query` and compares `spring-jdbc` builds before and after the changes in the PR.

## What is measured

- `dataClassRowMapper`: `new DataClassRowMapper<>(Person.class)`, created once and reused like in an application.
- `manualRowMapper`: a hand-written `RowMapper` that reads the columns by index. It is not affected by the PR and shows the cost of the query itself.
- `labels=snake`: `select id, first_name, last_name, ...` (snake_case column labels, 4 of the 10 components are camelCase).
- `labels=camel`: `select id, first_name as firstName, ...` (column labels aliased to the component names).
- `db=h2`: H2 in-memory database. `db=mysql`: MySQL 8.4 in a local Docker container.

See [RowMapperBenchmark.java](src/main/java/benchmark/RowMapperBenchmark.java) and [Person.java](src/main/java/benchmark/Person.java).

## Results

Average time per query of 1,000 rows in µs (lower is better, ± is the JMH 99.9% confidence interval, 3 forks × 10 iterations). The raw JMH output is in [results/](results).

| db | labels | main | column lookup only | column lookup + setter loop skipped | manual mapper (reference) |
|---|---|---:|---:|---:|---:|
| h2 | snake | 10,982 ± 363 | 1,207 ± 74 | 800 ± 48 | 54 ± 2 |
| h2 | camel | 1,185 ± 79 | 1,070 ± 77 | 805 ± 34 | 56 ± 4 |
| mysql | snake | 8,072 ± 221 | 1,955 ± 119 | 1,775 ± 150 | 1,251 ± 111 |
| mysql | camel | 2,296 ± 258 | 1,925 ± 90 | 1,794 ± 110 | 1,246 ± 77 |

- main: `bc45a6a511`, the base of the PR.
- column lookup only: `a6e92c94a0`, the first revision of the PR.
- column lookup + setter loop skipped: `f3f92a00b6`, the current revision of the PR.
- manual mapper: taken from the run with `f3f92a00b6`. It stays within the error margins across the three runs.

Environment: Intel Core Ultra 7 258V, Linux 6.17, OpenJDK 25+36-LTS (Temurin), H2 2.5.252, MySQL 8.4.11 in Docker on the same machine, MySQL Connector/J 26.7.0.

## How to run

Start MySQL:

```bash
docker run -d --name perf-mysql -e MYSQL_ROOT_PASSWORD=bench -e MYSQL_DATABASE=bench -p 3307:3306 mysql:8.4
```

Build `spring-jdbc` from each commit of spring-framework with `./gradlew :spring-jdbc:jar` and pass the jars to the script. The other Spring modules come from the `7.1.0-SNAPSHOT` repository.

```bash
./run-benchmark.sh \
  1-main=/path/to/spring-jdbc-main.jar \
  2-pr-column-lookup=/path/to/spring-jdbc-pr.jar \
  3-pr-skip-setter-loop=/path/to/spring-jdbc-skip-setter-loop.jar
```

Without `-PspringJdbcJar`, `./gradlew run` uses the current `spring-jdbc` snapshot. JMH options go through `--args`, e.g. `./gradlew run --args='-p db=h2'`. MySQL connection settings can be changed with `-Dmysql.url`, `-Dmysql.user` and `-Dmysql.password`.
