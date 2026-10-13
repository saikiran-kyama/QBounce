package com.qbounce.config;

import com.qbounce.enums.DomainType;
import com.qbounce.model.DomainCache;
import com.qbounce.repository.DomainCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainCacheSeeder implements CommandLineRunner {

    private final DomainCacheRepository repository;

    @Override
    public void run(String... args) throws Exception {

        long added = 0;

        added += loadFile("domains/public_domains.txt", DomainType.PUBLIC);
        added += loadFile("domains/temporary_domains.txt", DomainType.TEMPORARY);

        log.info("Domain cache initial seeding complete. {} domain(s) loaded.", added);
    }

    private long loadFile(
            String filePath,
            DomainType type
    ) throws Exception {

        ClassPathResource resource = new ClassPathResource(filePath);
        long count = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String domain = line.trim().toLowerCase();

                if (domain.isEmpty()) {
                    continue;
                }

                // Only insert if the domain doesn't already exist
                if (!repository.existsById(domain)) {
                    repository.save(
                            DomainCache.builder()
                                    .domain(domain)
                                    .type(type)
                                    .source("seeded")
                                    .createdAt(LocalDateTime.now())
                                    .build()
                    );
                    count++;
                }
            }
        }

        log.debug("Loaded {} file: {} new domain(s) added", filePath, count);
        return count;
    }
}