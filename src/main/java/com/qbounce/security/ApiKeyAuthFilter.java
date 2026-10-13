package com.qbounce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Filter to authenticate incoming requests using X-API-Key header. */
@Component
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

  private static final String API_KEY_HEADER = "X-API-Key";
  private static final String[] PUBLIC_PATHS = {
    "/swagger-ui", "/api-docs", "/v3/api-docs", "/actuator", "/health"
  };

  private final String apiKey;

  public ApiKeyAuthFilter(@Value("${api.security.key}") String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestPath = request.getRequestURI();

    // Allow public paths (Swagger, health checks, etc.)
    if (isPublicPath(requestPath)) {
      filterChain.doFilter(request, response);
      return;
    }

    String providedApiKey = request.getHeader(API_KEY_HEADER);

    if (providedApiKey == null || providedApiKey.isBlank()) {
      log.warn("Missing API key for request to {}", requestPath);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType("application/json");
      response.getWriter().write("{\"error\": \"Missing API key\", \"status\": 401}");
      return;
    }

    if (!apiKey.equals(providedApiKey)) {
      log.warn("Invalid API key for request to {}", requestPath);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType("application/json");
      response.getWriter().write("{\"error\": \"Invalid API key\", \"status\": 401}");
      return;
    }

    log.debug("API key authenticated for request to {}", requestPath);
    filterChain.doFilter(request, response);
  }

  /**
   * Checks if the request path is a public path that doesn't require authentication.
   *
   * @param requestPath The request URI path
   * @return true if the path is public, false otherwise
   */
  private boolean isPublicPath(String requestPath) {
    for (String publicPath : PUBLIC_PATHS) {
      if (requestPath.startsWith(publicPath)) {
        return true;
      }
    }
    return false;
  }
}
