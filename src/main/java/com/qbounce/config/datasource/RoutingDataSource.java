package com.qbounce.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/** Routes database connections to WRITE or READ datasource based on thread context. */
public class RoutingDataSource extends AbstractRoutingDataSource {

  @Override
  protected Object determineCurrentLookupKey() {
    return DataSourceContextHolder.getDataSourceType();
  }
}
