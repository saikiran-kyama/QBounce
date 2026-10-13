package com.qbounce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI/Swagger configuration for QBounce Service. */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI qbounceOpenApi() {
    final String securitySchemeName = "X-API-Key";
    return new OpenAPI()
        .info(
            new Info()
                .title("QBounce Service API")
                .description(
                    "Email Validation Cache Service - Validates emails via ZeroBounce and caches"
                        + " results for improved performance")
                .version("1.0.0")
                .contact(new Contact().name("QBounce Team")))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name("X-API-Key")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)));
  }
}
