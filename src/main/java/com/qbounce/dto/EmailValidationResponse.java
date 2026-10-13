package com.qbounce.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for email validation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailValidationResponse {

  /** Per-email validation results. */
  private List<EmailValidationResult> results;
}
