package benchmark;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Reads {@value #ROWS} rows into {@link Person} records through {@code JdbcTemplate.query}.
 * <ul>
 * <li>{@code dataClassRowMapper}: {@link DataClassRowMapper}, the subject of the measurement</li>
 * <li>{@code manualRowMapper}: a hand-written mapper by column index, as a lower bound</li>
 * </ul>
 * {@code labels=snake} selects snake_case columns ({@code first_name}), and {@code labels=camel}
 * aliases them to the record component names ({@code first_name as firstName}).
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(3)
public class RowMapperBenchmark {

    static final int ROWS = 1000;

    static final String SNAKE_SQL = """
            select id, first_name, last_name, email, birth_date, age, balance, city, active, created_at
            from person order by id""";

    static final String CAMEL_SQL = """
            select id, first_name as firstName, last_name as lastName, email, birth_date as birthDate,
                   age, balance, city, active, created_at as createdAt
            from person order by id""";

    @Param({"h2", "mysql"})
    public String db;

    @Param({"snake", "camel"})
    public String labels;

    private SingleConnectionDataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private String sql;

    private final RowMapper<Person> dataClassRowMapper = new DataClassRowMapper<>(Person.class);

    private final RowMapper<Person> manualRowMapper = (rs, rowNum) -> new Person(
            rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
            rs.getObject(5, LocalDate.class), rs.getInt(6), rs.getBigDecimal(7),
            rs.getString(8), rs.getBoolean(9), rs.getObject(10, LocalDateTime.class));

    @Setup(Level.Trial)
    public void setUp() throws SQLException {
        Connection connection = switch (this.db) {
            case "h2" -> DriverManager.getConnection("jdbc:h2:mem:bench;DB_CLOSE_DELAY=-1", "sa", "");
            case "mysql" -> DriverManager.getConnection(
                    System.getProperty("mysql.url", "jdbc:mysql://localhost:3307/bench"
                            + "?useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true"),
                    System.getProperty("mysql.user", "root"),
                    System.getProperty("mysql.password", "bench"));
            default -> throw new IllegalArgumentException(this.db);
        };
        this.dataSource = new SingleConnectionDataSource(connection, true);
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.sql = (this.labels.equals("snake") ? SNAKE_SQL : CAMEL_SQL);
        createTable();

        List<Person> expected = this.jdbcTemplate.query(this.sql, this.manualRowMapper);
        List<Person> actual = this.jdbcTemplate.query(this.sql, this.dataClassRowMapper);
        if (expected.size() != ROWS || !expected.equals(actual)) {
            throw new IllegalStateException("DataClassRowMapper returned different rows");
        }
    }

    private void createTable() {
        this.jdbcTemplate.execute("drop table if exists person");
        this.jdbcTemplate.execute("""
                create table person (
                    id bigint primary key,
                    first_name varchar(50),
                    last_name varchar(50),
                    email varchar(100),
                    birth_date date,
                    age int,
                    balance decimal(12, 2),
                    city varchar(50),
                    active boolean,
                    created_at timestamp
                )""");
        List<Object[]> rows = new ArrayList<>(ROWS);
        for (int i = 1; i <= ROWS; i++) {
            rows.add(new Object[] {i, "First" + i, "Last" + i, "user" + i + "@example.com",
                    LocalDate.of(1970, 1, 1).plusDays(i * 7L), 20 + i % 50,
                    new BigDecimal(i).movePointLeft(2).add(BigDecimal.valueOf(1000)),
                    "City" + i % 100, i % 2 == 0, LocalDateTime.of(2026, 1, 1, 0, 0).plusMinutes(i)});
        }
        this.jdbcTemplate.batchUpdate("insert into person values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", rows);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        this.jdbcTemplate.execute("drop table person");
        this.dataSource.destroy();
    }

    @Benchmark
    public List<Person> dataClassRowMapper() {
        return this.jdbcTemplate.query(this.sql, this.dataClassRowMapper);
    }

    @Benchmark
    public List<Person> manualRowMapper() {
        return this.jdbcTemplate.query(this.sql, this.manualRowMapper);
    }

}
