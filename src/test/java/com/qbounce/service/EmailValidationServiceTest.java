package com.qbounce.service;

import com.qbounce.dto.ContactInput;
import com.qbounce.dto.EmailValidationRequest;
import com.qbounce.dto.EmailValidationResponse;
import com.qbounce.dto.EmailValidationResult;
import com.qbounce.dto.ZeroBounceResponse;
import com.qbounce.enums.DomainType;
import com.qbounce.model.DomainCache;
import com.qbounce.model.EmailCache;
import com.qbounce.model.TenantConfig;
import com.qbounce.repository.TenantConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailValidationServiceTest {

    @Mock
    private DomainCacheService domainCacheService;

    @Mock
    private EmailCacheService emailCacheService;

    @Mock
    private ZeroBounceService zeroBounceService;

    @Mock
    private TenantConfigRepository tenantConfigRepository;

    @Mock
    private ContactRecordService contactRecordService;

    private EmailValidationService emailValidationService;

    @BeforeEach
    void setUp() {
        emailValidationService = new EmailValidationService(
                domainCacheService,
                emailCacheService,
                zeroBounceService,
                tenantConfigRepository,
                contactRecordService,
                30
        );
    }

    @Test
    void testValidateEmails_ThrowsException_WhenTenantConfigMissing() {
        // Arrange
        EmailValidationRequest request = new EmailValidationRequest();
        request.setTenantId("unknown-tenant");
        request.setData(List.of(ContactInput.builder().email("test@example.com").build()));

        when(tenantConfigRepository.findByTenantId("unknown-tenant")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailValidationService.validateEmails(request);
        });
        assertTrue(exception.getMessage().contains("Tenant configuration not found"));
    }

    @Test
    void testValidateEmails_DomainCacheHit_TemporaryDomain() {
        // Arrange
        String tenantId = "tenant-1";
        TenantConfig config = TenantConfig.builder().tenantId(tenantId).allowTemporary(false).build();
        
        EmailValidationRequest request = new EmailValidationRequest();
        request.setTenantId(tenantId);
        request.setData(List.of(ContactInput.builder().email("test@tempmail.com").build()));

        when(tenantConfigRepository.findByTenantId(tenantId)).thenReturn(Optional.of(config));
        when(emailCacheService.findValidEntries(any(), any())).thenReturn(Collections.emptyList());
        
        DomainCache domainCache = DomainCache.builder().domain("tempmail.com").type(DomainType.TEMPORARY).build();
        when(domainCacheService.findByDomain("tempmail.com")).thenReturn(Optional.of(domainCache));

        // Act
        EmailValidationResponse response = emailValidationService.validateEmails(request);

        // Assert
        assertEquals(1, response.getResults().size());
        EmailValidationResult result = response.getResults().get(0);
        assertEquals("test@tempmail.com", result.getEmail());
        assertEquals("TEMPORARY", result.getType());
        assertFalse(result.isAccepted()); // Because config.allowTemporary is false
        
        verify(zeroBounceService, never()).validateEmail(anyString());
    }

    @Test
    void testValidateEmails_EmailCacheHit() {
        // Arrange
        String tenantId = "tenant-1";
        TenantConfig config = TenantConfig.builder().tenantId(tenantId).allowPublic(true).build();
        
        EmailValidationRequest request = new EmailValidationRequest();
        request.setTenantId(tenantId);
        request.setData(List.of(ContactInput.builder().email("test@gmail.com").build()));

        when(tenantConfigRepository.findByTenantId(tenantId)).thenReturn(Optional.of(config));
        
        EmailCache cacheEntry = EmailCache.builder()
                .email("test@gmail.com")
                .isValid(true)
                .type(DomainType.PUBLIC.name())
                .reason("valid")
                .validatedAt(LocalDateTime.now())
                .build();
        when(emailCacheService.findValidEntries(any(), any())).thenReturn(List.of(cacheEntry));
        when(domainCacheService.findByDomain("gmail.com")).thenReturn(Optional.empty());

        // Act
        EmailValidationResponse response = emailValidationService.validateEmails(request);

        // Assert
        assertEquals(1, response.getResults().size());
        EmailValidationResult result = response.getResults().get(0);
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("PUBLIC", result.getType());
        assertTrue(result.isAccepted());
        
        verify(zeroBounceService, never()).validateEmail(anyString());
    }

    @Test
    void testValidateEmails_ZeroBounceApiCall_ValidPublicEmail() {
        // Arrange
        String tenantId = "tenant-1";
        TenantConfig config = TenantConfig.builder().tenantId(tenantId).allowPublic(true).build();
        
        EmailValidationRequest request = new EmailValidationRequest();
        request.setTenantId(tenantId);
        request.setData(List.of(ContactInput.builder().email("newuser@yahoo.com").build()));

        when(tenantConfigRepository.findByTenantId(tenantId)).thenReturn(Optional.of(config));
        when(emailCacheService.findValidEntries(any(), any())).thenReturn(Collections.emptyList());
        when(domainCacheService.findByDomain("yahoo.com")).thenReturn(Optional.empty());

        ZeroBounceResponse zbResponse = new ZeroBounceResponse();
        zbResponse.setStatus("valid");
        zbResponse.setSubStatus("");
        zbResponse.setFreeEmail(true); // Public email
        when(zeroBounceService.validateEmail("newuser@yahoo.com")).thenReturn(zbResponse);

        // Act
        EmailValidationResponse response = emailValidationService.validateEmails(request);

        // Assert
        assertEquals(1, response.getResults().size());
        EmailValidationResult result = response.getResults().get(0);
        assertEquals("newuser@yahoo.com", result.getEmail());
        assertEquals("PUBLIC", result.getType());
        assertTrue(result.isAccepted());
        
        verify(zeroBounceService, times(1)).validateEmail("newuser@yahoo.com");
        verify(domainCacheService, times(1)).upsertPublicDomain("yahoo.com", "api_learned");
        verify(emailCacheService, times(1)).saveAll(any());
        verify(contactRecordService, times(1)).saveAll(any());
    }
}
