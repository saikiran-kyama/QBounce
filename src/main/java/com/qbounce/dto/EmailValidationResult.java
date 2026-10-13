package com.qbounce.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Individual email validation result. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailValidationResult {
  private String email;
  private boolean accepted;
  private String type;
  private String reason;
}
