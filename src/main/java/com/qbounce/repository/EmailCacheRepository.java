package com.qbounce.repository;

import com.qbounce.model.EmailCache;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for email cache operations. */
@Repository
public interface EmailCacheRepository extends JpaRepository<EmailCache, String> {

  /**
   * Find a cached email validation result that is still valid (not expired).
   *
   * @param email The email address to look up
   * @param expiryDate The date before which the cache entry is considered expired
   * @return Optional containing the cached result if found and not expired
   */
  @Query("SELECT e FROM EmailCache e WHERE e.email = :email AND e.validatedAt >= :expiryDate")
  Optional<EmailCache> findValidCacheEntry(
      @Param("email") String email, @Param("expiryDate") LocalDateTime expiryDate);

  /**
   * Find all cached emails that match the given email addresses and are still valid.
   *
   * @param emails List of email addresses to look up
   * @param expiryDate The date before which cache entries are considered expired
   * @return List of valid cached entries
   */
  @Query("SELECT e FROM EmailCache e WHERE e.email IN :emails AND e.validatedAt >= :expiryDate")
  List<EmailCache> findValidCacheEntries(
      @Param("emails") List<String> emails, @Param("expiryDate") LocalDateTime expiryDate);

  /**
   * Delete expired cache entries.
   *
   * @param expiryDate The date before which cache entries should be deleted
   */
  @Query("DELETE FROM EmailCache e WHERE e.validatedAt < :expiryDate")
  void deleteExpiredEntries(@Param("expiryDate") LocalDateTime expiryDate);
}
