package com.qbounce.service;

import com.qbounce.model.EmailCache;
import com.qbounce.repository.EmailCacheRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles all email_cache database operations with explicit read/write datasource routing.
 *
 * <ul>
 *   <li>{@code @Transactional(readOnly = true)} → READ datasource
 *   <li>{@code @Transactional} → WRITE datasource
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailCacheService {

  private final EmailCacheRepository emailCacheRepository;

  /** Looks up valid (non-expired) cache entries — uses READ datasource. */
  @Transactional(readOnly = true)
  public List<EmailCache> findValidEntries(List<String> emails, LocalDateTime expiryDate) {
    return emailCacheRepository.findValidCacheEntries(emails, expiryDate);
  }

  /** Persists new cache entries — uses WRITE datasource. */
  @Transactional
  public void saveAll(List<EmailCache> entries) {
    emailCacheRepository.saveAll(entries);
    log.debug("Saved {} new cache entries", entries.size());
  }

  /** Returns total cache row count — uses READ datasource. */
  @Transactional(readOnly = true)
  public long count() {
    return emailCacheRepository.count();
  }

  /** Deletes expired cache entries — uses WRITE datasource. */
  @Transactional
  public void deleteExpired(LocalDateTime expiryDate) {
    emailCacheRepository.deleteExpiredEntries(expiryDate);
  }
}
