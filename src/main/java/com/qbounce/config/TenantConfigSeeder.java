package com.qbounce.config;

import com.qbounce.model.TenantConfig;
import com.qbounce.repository.TenantConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantConfigSeeder implements CommandLineRunner {

    private final TenantConfigRepository repository;

    @Override
    public void run(String... args) {
        log.info("Checking TenantConfig initial data...");

        if (repository.count() == 0) {
            log.info("No demo tenants found. Seeding demo tenants...");

            // Tenant 1: Strict mode (only allows public/private domains, rejects temporary/role-based/unknown)
            TenantConfig strictTenant = TenantConfig.builder()
                    .tenantId("tenant-strict")
                    .allowPublic(true)
                    .allowPrivate(true)
                    .allowRoleBased(false)
                    .allowTemporary(false)
                    .allowUnknown(false)
                    .build();

            // Tenant 2: Lenient mode (allows almost everything except temporary)
            TenantConfig lenientTenant = TenantConfig.builder()
                    .tenantId("tenant-lenient")
                    .allowPublic(true)
                    .allowPrivate(true)
                    .allowRoleBased(true)
                    .allowTemporary(false)
                    .allowUnknown(true)
                    .build();

            // Tenant 3: Standard mode
            TenantConfig standardTenant = TenantConfig.builder()
                    .tenantId("tenant-standard")
                    .allowPublic(true)
                    .allowPrivate(true)
                    .allowRoleBased(true)
                    .allowTemporary(false)
                    .allowUnknown(false)
                    .build();

            repository.save(strictTenant);
            repository.save(lenientTenant);
            repository.save(standardTenant);

            log.info("Seeded 3 demo tenant configurations: tenant-strict, tenant-lenient, tenant-standard.");
        } else {
            log.info("Tenant configurations already exist. Skipping demo seeder.");
        }
    }
}
