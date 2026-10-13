package com.qbounce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity representing a cached email validation result. */
@Entity
@Table(name = "email_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCache {

  /** Email address being validated (primary key). */
  @Id
  @Column(name = "email", length = 255, nullable = false)
  private String email;

  /** Whether ZeroBounce reported this email as valid/deliverable. */
  @Column(name = "is_valid")
  private Boolean isValid;

  /** Domain classification (PUBLIC, PRIVATE, TEMPORARY, ROLE_BASED). */
  @Column(name = "type", length = 50)
  private String type;

  /** Sub-status or reason from the validation source. */
  @Column(name = "reason", length = 255)
  private String reason;

  /** Timestamp of when the email was validated. */
  @Column(name = "validated_at", nullable = false)
  private LocalDateTime validatedAt;
}
