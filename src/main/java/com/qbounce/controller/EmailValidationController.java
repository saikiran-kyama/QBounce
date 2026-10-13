package com.qbounce.controller;

import com.qbounce.dto.EmailValidationRequest;
import com.qbounce.dto.EmailValidationResponse;
import com.qbounce.dto.ErrorResponse;
import com.qbounce.service.EmailValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for email validation endpoints. */
@RestController
@RequestMapping("/emails")
@Tag(name = "Email Validation", description = "Email validation and cache management endpoints")
@SecurityRequirement(name = "X-API-Key")
@Slf4j
public class EmailValidationController {

  private final EmailValidationService emailValidationService;

  public EmailValidationController(EmailValidationService emailValidationService) {
    this.emailValidationService = emailValidationService;
  }

  /**
   * Validates a list of email addresses.
   *
   * @param request The validation request containing email addresses
   * @return Response containing validation results for each email
   */
  @PostMapping("/validate")
  @Operation(
      summary = "Validate email addresses",
      description =
          "Validates a list of email addresses using ZeroBounce API with caching support. "
              + "Returns cached results when available, otherwise validates via API and caches "
              + "the result for future requests. "
              + "Each email is returned with one of three statuses: "
              + "**valid** (deliverable), **invalid** (undeliverable, spam trap, abuse, or do-not-mail), "
              + "or **unknown** (catch-all domain, ZeroBounce could not determine, or API error).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Validation completed. Each email maps to: `valid`, `invalid`, or `unknown`.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EmailValidationResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Mixed results",
                            value =
                                """
                                {
                                  "results": [
                                    { "email": "alice@example.com", "type": "PRIVATE", "accepted": true, "reason": "" },
                                    { "email": "bounce@spam.com", "type": "PRIVATE", "accepted": false, "reason": "mailbox_not_found" },
                                    { "email": "user@tempmail.com", "type": "TEMPORARY", "accepted": false, "reason": "domain_cache" }
                                  ]
                                }
                                """))),
        @ApiResponse(
            responseCode = "400",
            description = "Request body is missing or `data` list is empty.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Empty list",
                            value =
                                """
                                {
                                  "status": 400,
                                  "message": "Email list cannot be empty",
                                  "timestamp": "2026-04-07T17:30:00",
                                  "path": "/emails/validate"
                                }
                                """))),
        @ApiResponse(
            responseCode = "401",
            description = "Missing or invalid `X-API-Key` header.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Missing key",
                            value =
                                """
                                {
                                  "error": "Missing API key",
                                  "status": 401
                                }
                                """))),
        @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Server error",
                            value =
                                """
                                {
                                  "status": 500,
                                  "message": "Internal server error",
                                  "timestamp": "2026-04-07T17:30:00",
                                  "path": "/emails/validate"
                                }
                                """)))
      })
  public ResponseEntity<EmailValidationResponse> validateEmails(
      @Valid @RequestBody EmailValidationRequest request) {
    log.info("Received validation request for {} email(s)", request.getData().size());
    EmailValidationResponse response = emailValidationService.validateEmails(request);
    return ResponseEntity.ok(response);
  }
}
