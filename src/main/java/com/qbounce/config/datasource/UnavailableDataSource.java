package com.qbounce.config.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/** Placeholder DataSource that always throws SQLException when the database is unavailable. */
public class UnavailableDataSource implements DataSource {

  private static final String ERROR_MESSAGE = "Database is not available";

  @Override
  public Connection getConnection() throws SQLException {
    throw new SQLException(ERROR_MESSAGE);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    throw new SQLException(ERROR_MESSAGE);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new SQLException(ERROR_MESSAGE);
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new SQLException(ERROR_MESSAGE);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new SQLException(ERROR_MESSAGE);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    throw new SQLException(ERROR_MESSAGE);
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException(ERROR_MESSAGE);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException(ERROR_MESSAGE);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new SQLException(ERROR_MESSAGE);
  }
}
