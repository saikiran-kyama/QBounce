package com.qbounce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents a single person's contact data submitted for email validation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInput {

  // ── Required ──────────────────────────────────────────────────
  private String email;

  // ── Identity ──────────────────────────────────────────────────
  private String firstName;
  private String lastName;
  private String title;
  private String company;
  private String companyNameForEmails;
  private String seniority;
  private String departments;

  // ── Ownership ─────────────────────────────────────────────────
  private String contactOwner;
  private String accountOwner;
  private String stage;
  private String lists;
  private LocalDateTime lastContacted;

  // ── Engagement ────────────────────────────────────────────────
  private Boolean replied;
  private Boolean demoed;

  // ── Phones ────────────────────────────────────────────────────
  private String workDirectPhone;
  private String homePhone;
  private String mobilePhone;
  private String corporatePhone;
  private String otherPhone;

  // ── Company Info ──────────────────────────────────────────────
  @JsonProperty("numberOfEmployees")
  private Integer numberOfEmployees;
  private String industry;
  private String keywords;
  private String companyPhone;
  private String companyAddress;
  private String companyCity;
  private String companyState;
  private String companyCountry;
  private Long annualRevenue;

  // ── Social ────────────────────────────────────────────────────
  private String personLinkedinUrl;
  private String website;
  private String companyLinkedinUrl;
  private String facebookUrl;
  private String twitterUrl;

  // ── Location ──────────────────────────────────────────────────
  private String city;
  private String state;
  private String country;

  // ── Funding ───────────────────────────────────────────────────
  private Long totalFunding;
  private String latestFunding;
  private Long latestFundingAmount;
  private LocalDate lastRaisedAt;

  // ── Tech / SEO ────────────────────────────────────────────────
  private String seoDescription;
  private String technologies;
  private Integer numberOfRetailLocations;



  // ── Secondary / Tertiary Emails ───────────────────────────────
  private String secondaryEmail;
  private String secondaryEmailSource;
  private String tertiaryEmail;
  private String tertiaryEmailSource;

  // ── Intent Signals ────────────────────────────────────────────
  private String primaryIntentTopic;
  private BigDecimal primaryIntentScore;
  private String secondaryIntentTopic;
  private BigDecimal secondaryIntentScore;
}
