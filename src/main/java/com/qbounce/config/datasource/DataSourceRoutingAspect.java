package com.qbounce.config.datasource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Routes database calls to the READ or WRITE datasource based on the {@link Transactional}
 * annotation's {@code readOnly} flag.
 *
 * <ul>
 *   <li>{@code @Transactional(readOnly = true)} → READ datasource (cache lookup)
 *   <li>{@code @Transactional} (default) → WRITE datasource (cache save / delete)
 * </ul>
 *
 * <p>Must run before the transaction advice so the datasource context is set before a connection is
 * acquired.
 */
@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class DataSourceRoutingAspect {

  private final DataSourceConnectionStatus connectionStatus;

  @Around("@annotation(transactional)")
  public Object routeByTransaction(ProceedingJoinPoint pjp, Transactional transactional)
      throws Throwable {
    DataSourceType type = transactional.readOnly() ? DataSourceType.READ : DataSourceType.WRITE;

    DataSourceType previous = DataSourceContextHolder.getDataSourceType();
    try {
      DataSourceContextHolder.setDataSourceType(type);
      log.debug(
          "Routing to {} datasource for {}.{}",
          type,
          pjp.getSignature().getDeclaringTypeName(),
          pjp.getSignature().getName());
      return pjp.proceed();
    } finally {
      if (previous != null) {
        DataSourceContextHolder.setDataSourceType(previous);
      } else {
        DataSourceContextHolder.clear();
      }
    }
  }
}
