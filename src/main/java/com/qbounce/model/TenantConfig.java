package com.qbounce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity for storing tenant-specific validation configurations. */
@Entity
@Table(name = "tenant_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfig {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", unique = true, nullable = false)
  private String tenantId;

  @Column(name = "allow_public")
  @Builder.Default
  private boolean allowPublic = true;

  @Column(name = "allow_private")
  @Builder.Default
  private boolean allowPrivate = true;

  @Column(name = "allow_temporary")
  @Builder.Default
  private boolean allowTemporary = false;

  @Column(name = "allow_role_based")
  @Builder.Default
  private boolean allowRoleBased = true;

  @Column(name = "allow_unknown")
  @Builder.Default
  private boolean allowUnknown = false;
}
