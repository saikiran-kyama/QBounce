package com.qbounce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Error response DTO for API errors. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

  /** HTTP status code. */
  private int status;

  /** Error message. */
  private String message;

  /** Timestamp of the error. */
  private String timestamp;

  /** Request path that caused the error. */
  private String path;
}
