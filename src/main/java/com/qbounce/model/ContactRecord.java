package com.qbounce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores all contact/person data submitted in the validation request,
 * together with the validation result for that person's email.
 * Linked to email_cache by the email field (logical FK, no DB constraint).
 */
@Entity
@Table(name = "contact_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactRecord {

  // ── Identity ──────────────────────────────────────────────────
  @Column(name = "first_name", length = 100)
  private String firstName;

  @Column(name = "middle_name", length = 100)
  private String middleName;

  @Column(name = "last_name", length = 100)
  private String lastName;

  // ── Primary Key (Email) ───────────────────────────────────────
  @Id
  @Column(name = "email", length = 255, nullable = false)
  private String email;

  // ── Contact / Phones ──────────────────────────────────────────
  @Column(name = "work_direct_phone", length = 50)
  private String workDirectPhone;

  @Column(name = "home_phone", length = 50)
  private String homePhone;

  @Column(name = "mobile_phone", length = 50)
  private String mobilePhone;

  @Column(name = "corporate_phone", length = 50)
  private String corporatePhone;

  @Column(name = "other_phone", length = 50)
  private String otherPhone;

  // ── Additional Identity ───────────────────────────────────────
  @Column(name = "title", length = 150)
  private String title;

  @Column(name = "company", length = 255)
  private String company;

  @Column(name = "company_name_for_emails", length = 255)
  private String companyNameForEmails;

  @Column(name = "seniority", length = 100)
  private String seniority;

  @Column(name = "departments", length = 255)
  private String departments;

  // ── Location ──────────────────────────────────────────────────
  @Column(name = "city", length = 100)
  private String city;

  @Column(name = "state", length = 100)
  private String state;

  @Column(name = "country", length = 100)
  private String country;

  // ── Ownership ─────────────────────────────────────────────────
  @Column(name = "contact_owner", length = 255)
  private String contactOwner;

  @Column(name = "account_owner", length = 255)
  private String accountOwner;

  @Column(name = "stage", length = 100)
  private String stage;

  @Column(name = "lists", columnDefinition = "TEXT")
  private String lists;

  @Column(name = "last_contacted")
  private LocalDateTime lastContacted;

  // ── Engagement ────────────────────────────────────────────────
  @Column(name = "replied")
  private Boolean replied;

  @Column(name = "demoed")
  private Boolean demoed;

  // ── Company Info ──────────────────────────────────────────────
  @Column(name = "number_of_employees")
  private Integer numberOfEmployees;

  @Column(name = "industry", length = 255)
  private String industry;

  @Column(name = "keywords", columnDefinition = "TEXT")
  private String keywords;

  @Column(name = "company_phone", length = 50)
  private String companyPhone;

  @Column(name = "company_address", length = 500)
  private String companyAddress;

  @Column(name = "company_city", length = 100)
  private String companyCity;

  @Column(name = "company_state", length = 100)
  private String companyState;

  @Column(name = "company_country", length = 100)
  private String companyCountry;

  @Column(name = "annual_revenue")
  private Long annualRevenue;

  // ── Social ────────────────────────────────────────────────────
  @Column(name = "person_linkedin_url", length = 500)
  private String personLinkedinUrl;

  @Column(name = "website", length = 500)
  private String website;

  @Column(name = "company_linkedin_url", length = 500)
  private String companyLinkedinUrl;

  @Column(name = "facebook_url", length = 500)
  private String facebookUrl;

  @Column(name = "twitter_url", length = 500)
  private String twitterUrl;

  // ── Funding ───────────────────────────────────────────────────
  @Column(name = "total_funding")
  private Long totalFunding;

  @Column(name = "latest_funding", length = 100)
  private String latestFunding;

  @Column(name = "latest_funding_amount")
  private Long latestFundingAmount;

  @Column(name = "last_raised_at")
  private LocalDate lastRaisedAt;

  // ── Tech / SEO ────────────────────────────────────────────────
  @Column(name = "seo_description", columnDefinition = "TEXT")
  private String seoDescription;

  @Column(name = "technologies", columnDefinition = "TEXT")
  private String technologies;

  @Column(name = "number_of_retail_locations")
  private Integer numberOfRetailLocations;

  // ── Secondary / Tertiary Emails ───────────────────────────────
  @Column(name = "secondary_email", length = 255)
  private String secondaryEmail;

  @Column(name = "secondary_email_source", length = 100)
  private String secondaryEmailSource;

  @Column(name = "tertiary_email", length = 255)
  private String tertiaryEmail;

  @Column(name = "tertiary_email_source", length = 100)
  private String tertiaryEmailSource;

  // ── Intent Signals ────────────────────────────────────────────
  @Column(name = "primary_intent_topic", length = 255)
  private String primaryIntentTopic;

  @Column(name = "primary_intent_score", precision = 5, scale = 2)
  private BigDecimal primaryIntentScore;

  @Column(name = "secondary_intent_topic", length = 255)
  private String secondaryIntentTopic;

  @Column(name = "secondary_intent_score", precision = 5, scale = 2)
  private BigDecimal secondaryIntentScore;

  // ── Validation Result (from email_cache) ──────────────────────
  @Column(name = "is_valid")
  private Boolean isValid;

  @Column(name = "type", length = 50)
  private String type;

  @Column(name = "reason", length = 255)
  private String reason;

  @Column(name = "status", length = 50)
  private String status;

  @Column(name = "sub_status", length = 50)
  private String subStatus;

  @Column(name = "free_email")
  private Boolean freeEmail;

  @Column(name = "accepted")
  private Boolean accepted;

  @Column(name = "validated_at")
  private LocalDateTime validatedAt;

  @Column(name = "validated_by", length = 50)
  private String validatedBy;

  // ── Audit ─────────────────────────────────────────────────────
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

@Column(name = "testingcol")
private String testingcol; 
}
