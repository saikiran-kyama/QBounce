package com.qbounce.config.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Dual datasource configuration for qbounce-service.
 *
 * <ul>
 *   <li><b>WRITE</b> - INSERT/UPDATE/DELETE on email_cache
 *   <li><b>READ</b> - SELECT from email_cache (cache lookup)
 * </ul>
 */
@Slf4j
@Configuration
public class DataSourceConfiguration {

  private final DataSourceConnectionStatus connectionStatus;

    @Value("${spring.datasource.writer.url:}")
  private String writeUrl;

    @Value("${spring.datasource.writer.username:}")
  private String writeUsername;

    @Value("${spring.datasource.writer.password:}")
  private String writePassword;

    @Value("${spring.datasource.writer.driver-class-name:com.mysql.cj.jdbc.Driver}")
  private String writeDriverClassName;

    @Value("${spring.datasource.reader.url:}")
  private String readUrl;

    @Value("${spring.datasource.reader.username:}")
  private String readUsername;

    @Value("${spring.datasource.reader.password:}")
  private String readPassword;

    @Value("${spring.datasource.reader.driver-class-name:com.mysql.cj.jdbc.Driver}")
  private String readDriverClassName;

  @Value("${spring.jpa.hibernate.ddl-auto:update}")
  private String ddlAuto;

  @Value("${spring.jpa.show-sql:false}")
  private boolean showSql;

  public DataSourceConfiguration(DataSourceConnectionStatus connectionStatus) {
    this.connectionStatus = connectionStatus;
  }

  @Bean(name = "writeDataSource")
  public DataSource writeDataSource() {
    if (!isConfigured(writeUrl)) {
      log.warn("WRITE datasource is not configured");
      connectionStatus.markDisconnected(DataSourceType.WRITE, "Database not configured");
      return new UnavailableDataSource();
    }
    try {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(writeUrl);
      config.setUsername(writeUsername);
      config.setPassword(writePassword);
      config.setDriverClassName(writeDriverClassName);
      config.setPoolName("WritePool");
      config.setMaximumPoolSize(10);
      config.setMinimumIdle(2);
      config.setConnectionTimeout(5000);
      config.setValidationTimeout(3000);

      HikariDataSource dataSource = new HikariDataSource(config);
      if (testConnection(dataSource)) {
        connectionStatus.markConnected(DataSourceType.WRITE);
        log.info("WRITE datasource connected: {}", writeUrl);
      } else {
        connectionStatus.markDisconnected(DataSourceType.WRITE, "Connection test failed");
      }
      return dataSource;
    } catch (Exception e) {
      log.error("Failed to initialize WRITE datasource: {}", e.getMessage());
      connectionStatus.markDisconnected(DataSourceType.WRITE, e.getMessage());
      return new UnavailableDataSource();
    }
  }

  @Bean(name = "readDataSource")
  public DataSource readDataSource() {
    if (!isConfigured(readUrl)) {
      log.warn("READ datasource is not configured");
      connectionStatus.markDisconnected(DataSourceType.READ, "Database not configured");
      return new UnavailableDataSource();
    }
    try {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(readUrl);
      config.setUsername(readUsername);
      config.setPassword(readPassword);
      config.setDriverClassName(readDriverClassName);
      config.setPoolName("ReadPool");
      config.setMaximumPoolSize(15);
      config.setMinimumIdle(3);
      config.setConnectionTimeout(5000);
      config.setValidationTimeout(3000);
      config.setReadOnly(true);

      HikariDataSource dataSource = new HikariDataSource(config);
      if (testConnection(dataSource)) {
        connectionStatus.markConnected(DataSourceType.READ);
        log.info("READ datasource connected: {}", readUrl);
      } else {
        connectionStatus.markDisconnected(DataSourceType.READ, "Connection test failed");
      }
      return dataSource;
    } catch (Exception e) {
      log.error("Failed to initialize READ datasource: {}", e.getMessage());
      connectionStatus.markDisconnected(DataSourceType.READ, e.getMessage());
      return new UnavailableDataSource();
    }
  }

  @Primary
  @Bean(name = "dataSource")
  public DataSource routingDataSource(
      @Qualifier("writeDataSource") DataSource writeDataSource,
      @Qualifier("readDataSource") DataSource readDataSource) {

    RoutingDataSource routingDataSource = new RoutingDataSource();

    Map<Object, Object> targetDataSources = new HashMap<>();
    targetDataSources.put(DataSourceType.WRITE, writeDataSource);
    targetDataSources.put(DataSourceType.READ, readDataSource);

    routingDataSource.setTargetDataSources(targetDataSources);

    DataSource defaultDataSource = selectDefaultDataSource(writeDataSource, readDataSource);
    routingDataSource.setDefaultTargetDataSource(defaultDataSource);
    routingDataSource.afterPropertiesSet();

    log.info("Routing DataSource configured");
    log.info(connectionStatus.getStatusSummary());

    return routingDataSource;
  }

  private DataSource selectDefaultDataSource(DataSource write, DataSource read) {
    if (connectionStatus.isConnected(DataSourceType.WRITE)) {
      return write;
    } else if (connectionStatus.isConnected(DataSourceType.READ)) {
      log.warn("Using READ datasource as default (WRITE unavailable)");
      return read;
    } else {
      throw new IllegalStateException(
          "Application startup failed: both WRITE and READ databases are unavailable.");
    }
  }

  @Primary
  @Bean(name = "entityManagerFactory")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      @Qualifier("dataSource") DataSource dataSource) {

    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("com.qbounce");
    em.setPersistenceUnitName("default");

    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setShowSql(showSql);
    em.setJpaVendorAdapter(vendorAdapter);

    Map<String, Object> properties = new HashMap<>();
    properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    properties.put("hibernate.show_sql", String.valueOf(showSql));

    String effectiveDdlAuto = connectionStatus.isConnected(DataSourceType.WRITE) ? ddlAuto : "none";
    properties.put("hibernate.hbm2ddl.auto", effectiveDdlAuto);

    em.setJpaPropertyMap(properties);
    return em;
  }

  @Primary
  @Bean(name = "transactionManager")
  public PlatformTransactionManager transactionManager(
      @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  private boolean isConfigured(String url) {
    return url != null && !url.isBlank();
  }

  private boolean testConnection(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      return connection.isValid(3);
    } catch (SQLException e) {
      log.warn("Connection test failed: {}", e.getMessage());
      return false;
    }
  }
}
