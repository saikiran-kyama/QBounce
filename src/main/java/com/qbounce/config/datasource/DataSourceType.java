package com.qbounce.config.datasource;

/** Identifies which datasource to use. WRITE - for INSERT/UPDATE/DELETE. READ - for SELECT. */
public enum DataSourceType {
  WRITE,
  READ
}
