package com.qbounce.service;

import com.qbounce.dto.ZeroBounceResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/** Service for interacting with the ZeroBounce email validation API. */
@Service
@Slf4j
public class ZeroBounceService {

  private final WebClient webClient;
  private final String apiUrl;
  private final String apiKey;

  public ZeroBounceService(
      WebClient webClient,
      @Value("${zerobounce.api.url}") String apiUrl,
      @Value("${zerobounce.api.key}") String apiKey) {
    this.webClient = webClient;
    this.apiUrl = apiUrl;
    this.apiKey = apiKey;
  }

  /**
   * Validates an email address using the ZeroBounce API.
   *
   * @param email The email address to validate
   * @return ZeroBounceResponse containing the validation result
   */
  public ZeroBounceResponse validateEmail(String email) {
    log.debug("Validating email via ZeroBounce API: {}", email);

    try {
      String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
      URI uri =
          URI.create(apiUrl + "?api_key=" + apiKey + "&email=" + encodedEmail + "&ip_address=");

      ZeroBounceResponse response =
          webClient
              .get()
              .uri(uri)
              .retrieve()
              .bodyToMono(ZeroBounceResponse.class)
              .onErrorResume(
                  WebClientResponseException.class,
                  ex -> {
                    String body = ex.getResponseBodyAsString();
                    if (body != null && body.contains("Invalid API Key")) {
                      log.error(
                          "ZeroBounce API key is invalid or has no credits. Check zerobounce.api.key configuration.");
                      return Mono.just(
                          createErrorResponse(email, "ZeroBounce API key invalid or no credits"));
                    }
                    log.error(
                        "ZeroBounce API HTTP error for {}: {} - {}",
                        email,
                        ex.getStatusCode(),
                        body);
                    return Mono.just(
                        createErrorResponse(email, "API error: " + ex.getStatusCode()));
                  })
              .onErrorResume(
                  Exception.class,
                  ex -> {
                    log.error("ZeroBounce API call failed for {}: {}", email, ex.getMessage());
                    return Mono.just(createErrorResponse(email, "Service unavailable"));
                  })
              .block();

      if (response == null) {
        log.warn("ZeroBounce returned null response for {}", email);
        return createErrorResponse(email, "Empty response from validation service");
      }

      log.debug(
          "ZeroBounce result for {}: status={}, sub_status={}",
          email,
          response.getStatus(),
          response.getSubStatus());
      return response;

    } catch (IllegalArgumentException e) {
      log.error("Invalid URI constructed for email {}: {}", email, e.getMessage());
      return createErrorResponse(email, "Invalid email format for API call");
    } catch (Exception e) {
      log.error("Unexpected error validating {} via ZeroBounce: {}", email, e.getMessage());
      return createErrorResponse(email, "Validation failed");
    }
  }

  /**
   * Creates an error response when the API call fails.
   *
   * @param email The email that failed validation
   * @param errorMessage The error message
   * @return ZeroBounceResponse with error status
   */
  private ZeroBounceResponse createErrorResponse(String email, String errorMessage) {
    return ZeroBounceResponse.builder()
        .address(email)
        .status("unknown")
        .error(errorMessage)
        .build();
  }

  /**
   * Normalizes the ZeroBounce status to a simplified result.
   *
   * @param status The original ZeroBounce status
   * @return Normalized status (valid, invalid, or unknown)
   */
  public String normalizeStatus(String status) {
    if (status == null) {
      return "unknown";
    }

    return switch (status.toLowerCase()) {
      case "valid" -> "valid";
      case "invalid", "spamtrap", "abuse", "do_not_mail" -> "invalid";
      case "catch-all", "unknown" -> "unknown";
      default -> "unknown";
    };
  }
}
