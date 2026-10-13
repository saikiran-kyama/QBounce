package com.qbounce.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for email validation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailValidationRequest {

  /** Tenant ID making the validation request. */
  @NotEmpty(message = "Tenant ID is required")
  private String tenantId;

  /** List of email addresses to validate. */
  @NotEmpty(message = "Email list cannot be empty")
  private List<ContactInput> data;
}
