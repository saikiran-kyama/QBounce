package com.qbounce.service;

import com.qbounce.dto.ContactInput;
import com.qbounce.dto.EmailValidationRequest;
import com.qbounce.dto.EmailValidationResponse;
import com.qbounce.dto.EmailValidationResult;
import com.qbounce.dto.ZeroBounceResponse;
import com.qbounce.enums.DomainType;
import com.qbounce.model.ContactRecord;
import com.qbounce.model.DomainCache;
import com.qbounce.model.EmailCache;
import com.qbounce.model.TenantConfig;
import com.qbounce.repository.TenantConfigRepository;
import com.qbounce.util.EmailUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Validates emails using a three-tier cache: email cache → domain cache → ZeroBounce API. */
@Service
@Slf4j
public class EmailValidationService {

  private final DomainCacheService domainCacheService;
  private final EmailCacheService emailCacheService;
  private final ZeroBounceService zeroBounceService;
  private final TenantConfigRepository tenantConfigRepository;
  private final ContactRecordService contactRecordService;
  private final int cacheValidityDays;

  public EmailValidationService(
      DomainCacheService domainCacheService,
      EmailCacheService emailCacheService,
      ZeroBounceService zeroBounceService,
      TenantConfigRepository tenantConfigRepository,
      ContactRecordService contactRecordService,
      @Value("${qbounce.cache.validity-days:30}") int cacheValidityDays) {
    this.domainCacheService = domainCacheService;
    this.emailCacheService = emailCacheService;
    this.zeroBounceService = zeroBounceService;
    this.tenantConfigRepository = tenantConfigRepository;
    this.contactRecordService = contactRecordService;
    this.cacheValidityDays = cacheValidityDays;
  }

  public EmailValidationResponse validateEmails(EmailValidationRequest request) {
    TenantConfig config =
        tenantConfigRepository
            .findByTenantId(request.getTenantId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Tenant configuration not found for: " + request.getTenantId()));

    List<ContactInput> contacts = request.getData();
    List<String> emails =
        contacts.stream().map(c -> c.getEmail().toLowerCase()).collect(Collectors.toList());

    LocalDateTime expiryDate = LocalDateTime.now().minusDays(cacheValidityDays);
    Map<String, EmailCache> emailCacheMap =
        emailCacheService.findValidEntries(emails, expiryDate).stream()
            .collect(Collectors.toMap(EmailCache::getEmail, e -> e));

    List<EmailValidationResult> results = new ArrayList<>();
    List<EmailCache> newCacheEntries = new ArrayList<>();
    List<ContactRecord> contactRecords = new ArrayList<>();

    for (ContactInput contact : contacts) {
      String email = contact.getEmail().toLowerCase();
      String domain = EmailUtils.extractDomain(email);
      Optional<DomainCache> domainCacheOpt = domainCacheService.findByDomain(domain);

      boolean isValid;
      String type;
      String reason;

      if (emailCacheMap.containsKey(email)) {
        EmailCache cache = emailCacheMap.get(email);
        isValid = Boolean.TRUE.equals(cache.getIsValid());
        type = cache.getType();
        reason = cache.getReason();
      } else if (domainCacheOpt.isPresent()
          && DomainType.TEMPORARY.name().equals(domainCacheOpt.get().getType().name())) {
        type = DomainType.TEMPORARY.name();
        isValid = false;
        reason = "domain_cache_temporary";
      } else {
        String cachedType = domainCacheOpt.map(d -> d.getType().name()).orElse(null);
        ZeroBounceResponse zbResponse = zeroBounceService.validateEmail(email);
        boolean zbError = zbResponse.getError() != null;
        isValid = !zbError && "valid".equalsIgnoreCase(zbResponse.getStatus());
        type = cachedType != null ? cachedType : determineType(zbResponse);
        reason = zbError ? zbResponse.getError() : zbResponse.getSubStatus();
        if (!zbError) {
          if (cachedType == null) {
            upsertDomainCache(domain, type);
          }
          newCacheEntries.add(
              EmailCache.builder()
                  .email(email)
                  .isValid(isValid)
                  .type(type)
                  .reason(reason)
                  .validatedAt(LocalDateTime.now())
                  .build());
        } else {
          log.warn("ZeroBounce error for {}, skipping cache: {}", email, zbResponse.getError());
        }
      }

      boolean accepted = isValid && tenantAllows(config, type);
      EmailValidationResult result =
          EmailValidationResult.builder()
              .email(email)
              .type(type)
              .accepted(accepted)
              .reason(reason)
              .build();

      results.add(result);
      contactRecords.add(buildContactRecord(contact, result, isValid));
    }

    if (!newCacheEntries.isEmpty()) {
      emailCacheService.saveAll(newCacheEntries);
    }
    contactRecordService.saveAll(contactRecords);

    return EmailValidationResponse.builder().results(results).build();
  }

  public Map<String, Object> getCacheStats() {
    LocalDateTime expiryDate = LocalDateTime.now().minusDays(cacheValidityDays);
    return Map.of(
        "totalEntries", emailCacheService.count(),
        "cacheValidityDays", cacheValidityDays,
        "oldestValidDate", expiryDate);
  }

  public long clearExpiredEntries() {
    LocalDateTime expiryDate = LocalDateTime.now().minusDays(cacheValidityDays);
    long countBefore = emailCacheService.count();
    emailCacheService.deleteExpired(expiryDate);
    long deleted = countBefore - emailCacheService.count();
    log.info("Cleared {} expired cache entries", deleted);
    return deleted;
  }

  private boolean tenantAllows(TenantConfig config, String type) {
    return switch (type) {
      case "PUBLIC" -> config.isAllowPublic();
      case "PRIVATE" -> config.isAllowPrivate();
      case "TEMPORARY" -> config.isAllowTemporary();
      case "ROLE_BASED" -> config.isAllowRoleBased();
      default -> config.isAllowUnknown();
    };
  }

  private String determineType(ZeroBounceResponse response) {
    if (Boolean.TRUE.equals(response.getFreeEmail())) {
      return DomainType.PUBLIC.name();
    }
    return DomainType.PRIVATE.name();
  }

  private void upsertDomainCache(String domain, String type) {
    switch (type) {
      case "PUBLIC" -> domainCacheService.upsertPublicDomain(domain, "api_learned");
      case "TEMPORARY" -> domainCacheService.upsertTemporaryDomain(domain, "api_learned");
      case "ROLE_BASED" -> domainCacheService.upsertRoleBasedDomain(domain, "api_learned");
      default -> domainCacheService.upsertPrivateDomain(domain, "api_learned");
    }
  }

  private ContactRecord buildContactRecord(
      ContactInput input, EmailValidationResult result, boolean isValid) {
    return ContactRecord.builder()
        .email(input.getEmail().toLowerCase())
        .firstName(input.getFirstName())
        .lastName(input.getLastName())
        .title(input.getTitle())
        .company(input.getCompany())
        .companyNameForEmails(input.getCompanyNameForEmails())
        .seniority(input.getSeniority())
        .departments(input.getDepartments())
        .contactOwner(input.getContactOwner())
        .accountOwner(input.getAccountOwner())
        .stage(input.getStage())
        .lists(input.getLists())
        .lastContacted(input.getLastContacted())
        .replied(input.getReplied())
        .demoed(input.getDemoed())
        .workDirectPhone(input.getWorkDirectPhone())
        .homePhone(input.getHomePhone())
        .mobilePhone(input.getMobilePhone())
        .corporatePhone(input.getCorporatePhone())
        .otherPhone(input.getOtherPhone())
        .numberOfEmployees(input.getNumberOfEmployees())
        .industry(input.getIndustry())
        .keywords(input.getKeywords())
        .companyPhone(input.getCompanyPhone())
        .companyAddress(input.getCompanyAddress())
        .companyCity(input.getCompanyCity())
        .companyState(input.getCompanyState())
        .companyCountry(input.getCompanyCountry())
        .annualRevenue(input.getAnnualRevenue())
        .personLinkedinUrl(input.getPersonLinkedinUrl())
        .website(input.getWebsite())
        .companyLinkedinUrl(input.getCompanyLinkedinUrl())
        .facebookUrl(input.getFacebookUrl())
        .twitterUrl(input.getTwitterUrl())
        .city(input.getCity())
        .state(input.getState())
        .country(input.getCountry())
        .totalFunding(input.getTotalFunding())
        .latestFunding(input.getLatestFunding())
        .latestFundingAmount(input.getLatestFundingAmount())
        .lastRaisedAt(input.getLastRaisedAt())
        .seoDescription(input.getSeoDescription())
        .technologies(input.getTechnologies())
        .numberOfRetailLocations(input.getNumberOfRetailLocations())
        .secondaryEmail(input.getSecondaryEmail())
        .secondaryEmailSource(input.getSecondaryEmailSource())
        .tertiaryEmail(input.getTertiaryEmail())
        .tertiaryEmailSource(input.getTertiaryEmailSource())
        .primaryIntentTopic(input.getPrimaryIntentTopic())
        .primaryIntentScore(input.getPrimaryIntentScore())
        .secondaryIntentTopic(input.getSecondaryIntentTopic())
        .secondaryIntentScore(input.getSecondaryIntentScore())
        .isValid(isValid)
        .type(result.getType())
        .reason(result.getReason())
        .accepted(result.isAccepted())
        .validatedAt(LocalDateTime.now())
        .validatedBy("qbounce")
        .createdAt(LocalDateTime.now())
        .build();
  }
}
