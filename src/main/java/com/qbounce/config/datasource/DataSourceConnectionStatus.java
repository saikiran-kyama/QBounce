package com.qbounce.config.datasource;

import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Tracks the connection status of each datasource. */
@Slf4j
@Component
public class DataSourceConnectionStatus {

  private final Map<DataSourceType, Boolean> connectionStatus = new EnumMap<>(DataSourceType.class);
  private final Map<DataSourceType, String> connectionErrors = new EnumMap<>(DataSourceType.class);

  public DataSourceConnectionStatus() {
    for (DataSourceType type : DataSourceType.values()) {
      connectionStatus.put(type, false);
      connectionErrors.put(type, "Database not configured");
    }
  }

  public void markConnected(DataSourceType type) {
    connectionStatus.put(type, true);
    connectionErrors.put(type, null);
    log.info("Datasource {} is now CONNECTED", type);
  }

  public void markDisconnected(DataSourceType type, String errorMessage) {
    connectionStatus.put(type, false);
    connectionErrors.put(type, errorMessage);
    log.warn("Datasource {} is DISCONNECTED: {}", type, errorMessage);
  }

  public boolean isConnected(DataSourceType type) {
    return Boolean.TRUE.equals(connectionStatus.get(type));
  }

  public String getErrorMessage(DataSourceType type) {
    return connectionErrors.get(type);
  }

  public String getStatusSummary() {
    StringBuilder sb = new StringBuilder("DataSource Status:\n");
    for (DataSourceType type : DataSourceType.values()) {
      sb.append(
          String.format(
              "  %s: %s%n",
              type, isConnected(type) ? "CONNECTED" : "DISCONNECTED - " + getErrorMessage(type)));
    }
    return sb.toString();
  }
}
