
package com.qbounce.config.datasource;

import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * jOOQ configuration.
 *
 * <p>Uses the routing datasource so jOOQ queries automatically route
 * to WRITE or READ pool based on the active {@link DataSourceType}.
 */
@Configuration
public class JooqConfig {

  @Bean
  public DSLContext dslContext(@Qualifier("dataSource") DataSource dataSource) {
    return DSL.using(dataSource, SQLDialect.MYSQL);
  }
}