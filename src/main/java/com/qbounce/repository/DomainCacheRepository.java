package com.qbounce.repository;

import com.qbounce.model.DomainCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainCacheRepository
        extends JpaRepository<DomainCache, String> {
}