package com.qbounce.service;

import com.qbounce.enums.DomainType;
import com.qbounce.model.DomainCache;
import com.qbounce.repository.DomainCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainCacheService {

    private final DomainCacheRepository repository;

    @Transactional(readOnly = true)
    public Optional<DomainCache> findByDomain(String domain) {
        return repository.findById(domain.toLowerCase().trim());
    }

    @Transactional
    public void upsertPublicDomain(String domain, String source) {
        log.info("Domain cache upsert: domain={} type=PUBLIC source={}", domain, source);
        upsert(domain, DomainType.PUBLIC, source);
    }

    @Transactional
    public void upsertTemporaryDomain(String domain, String source) {
        log.info("Domain cache upsert: domain={} type=TEMPORARY source={}", domain, source);
        upsert(domain, DomainType.TEMPORARY, source);
    }

    @Transactional
    public void upsertRoleBasedDomain(String domain, String source) {
        log.info("Domain cache upsert: domain={} type=ROLE_BASED source={}", domain, source);
        upsert(domain, DomainType.ROLE_BASED, source);
    }

    @Transactional
    public void upsertPrivateDomain(String domain, String source) {
        log.info("Domain cache upsert: domain={} type=PRIVATE source={}", domain, source);
        upsert(domain, DomainType.PRIVATE, source);
    }

    private void upsert(String domain, DomainType type, String source) {
        String normalizedDomain = domain.toLowerCase().trim();
        if (!repository.existsById(normalizedDomain)) {
            DomainCache cache = DomainCache.builder()
                    .domain(normalizedDomain)
                    .type(type)
                    .source(source)
                    .createdAt(LocalDateTime.now())
                    .build();
            repository.save(cache);
            log.info("Domain cache saved: domain={} type={}", normalizedDomain, type);
        } else {
            log.debug("Domain cache already exists for domain={}, skipping insert", normalizedDomain);
        }
    }
}