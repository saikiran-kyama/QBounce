package com.qbounce.model;

import com.qbounce.enums.DomainType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "domain_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainCache {

    @Id
    @Column(length = 255)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DomainType type;

    @Column(length = 20, nullable = false)
    private String source;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


}