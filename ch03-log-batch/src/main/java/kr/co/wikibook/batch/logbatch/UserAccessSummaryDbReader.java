package kr.co.wikibook.batch.logbatch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;

public class UserAccessSummaryDbReader {

  private final RowMapper<UserAccessSummary> rowMapper = (resultSet, index) ->
      new UserAccessSummary(
          resultSet.getString("username"),
          resultSet.getInt("access_count")
      );

  private final DataSource dataSource;
  private final LocalDate date;
  private PreparedStatement stmt;
  private Connection con;
  private ResultSet resultSet;
  private int rowCount = 0;

  public UserAccessSummaryDbReader(DataSource dataSource, LocalDate date) {
    this.dataSource = dataSource;
    this.date = date;
  }

  public void open() throws SQLException { // <2>
    this.con = DataSourceUtils.getConnection(dataSource);
    this.stmt =
        con.prepareStatement(AccessLogSql.COUNT_GROUP_BY_USERNAME, ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY); // <3>
    Instant from = date.atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant to = from.plus(1, ChronoUnit.DAYS);
    this.stmt.setObject(1, from);
    this.stmt.setObject(2, to);
    this.resultSet = stmt.executeQuery();
  }

  @Nullable
  public UserAccessSummary read() throws SQLException { // <4>
    if (resultSet.next()) {
      UserAccessSummary item = this.rowMapper.mapRow(resultSet, rowCount);
      rowCount++;
      return item;
    }
    return null;
  }

  public void close() { // <5>
    this.rowCount = 0;
    JdbcUtils.closeResultSet(this.resultSet);
    JdbcUtils.closeStatement(this.stmt);
    JdbcUtils.closeConnection(this.con);
  }
}