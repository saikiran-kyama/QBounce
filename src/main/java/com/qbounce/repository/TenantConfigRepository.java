package com.qbounce.repository;

import com.qbounce.model.TenantConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for TenantConfig entity. */
@Repository
public interface TenantConfigRepository extends JpaRepository<TenantConfig, Long> {
  Optional<TenantConfig> findByTenantId(String tenantId);
}
