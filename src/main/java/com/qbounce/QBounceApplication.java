package com.qbounce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * QBounce Service - Email Validation Cache Service.
 *
 * <p>This service provides a caching layer for email validation via ZeroBounce or similar services.
 * It caches validation results for a configurable period to reduce API calls and improve
 * performance.
 */
@SpringBootApplication(
    exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class QBounceApplication {

  public static void main(String[] args) {
    SpringApplication.run(QBounceApplication.class, args);
  }
}
