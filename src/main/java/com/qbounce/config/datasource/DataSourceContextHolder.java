package com.qbounce.config.datasource;

/** Thread-local holder for the current datasource context. */
public class DataSourceContextHolder {

  private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

  private DataSourceContextHolder() {}

  public static void setDataSourceType(DataSourceType type) {
    CONTEXT.set(type);
  }

  public static DataSourceType getDataSourceType() {
    DataSourceType type = CONTEXT.get();
    return type != null ? type : DataSourceType.WRITE;
  }

  public static void clear() {
    CONTEXT.remove();
  }
}
