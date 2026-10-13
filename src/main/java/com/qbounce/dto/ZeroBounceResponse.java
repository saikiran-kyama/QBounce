package com.qbounce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO representing the response from ZeroBounce API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZeroBounceResponse {

  /** The email address that was validated. */
  @JsonProperty("address")
  private String address;

  /** The validation status (valid, invalid, catch-all, unknown, spamtrap, abuse, do_not_mail). */
  @JsonProperty("status")
  private String status;

  /**
   * The sub-status providing more details. Examples: antispam_system, greylisted,
   * mail_server_did_not_respond, etc.
   */
  @JsonProperty("sub_status")
  private String subStatus;

  /** The domain of the email address. */
  @JsonProperty("domain")
  private String domain;

  /** The SMTP provider (e.g., gmail, outlook, yahoo). */
  @JsonProperty("smtp_provider")
  private String smtpProvider;

  /** Whether the email is free (true/false). */
  @JsonProperty("free_email")
  private Boolean freeEmail;

  /** First name if detected. */
  @JsonProperty("firstname")
  private String firstname;

  /** Last name if detected. */
  @JsonProperty("lastname")
  private String lastname;

  /** Gender (male, female, unknown). */
  @JsonProperty("gender")
  private String gender;

  /** Country of the email address. */
  @JsonProperty("country")
  private String country;

  /** Region of the email address. */
  @JsonProperty("region")
  private String region;

  /** City of the email address. */
  @JsonProperty("city")
  private String city;

  /** Zipcode of the email address. */
  @JsonProperty("zipcode")
  private String zipcode;

  /** The date and time when the API processed the email. */
  @JsonProperty("processed_at")
  private String processedAt;

  /** Error message if the validation failed. */
  @JsonProperty("error")
  private String error;
}
