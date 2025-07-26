package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class TestConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String appVersion;
    @Column(nullable = false)
    private String environment;
    private String testSuite;
    @Column(unique = true)
    private String uniqueName;

    @PrePersist
    @PreUpdate
    private void generateUniqueName() {
        this.uniqueName = String.format("%s-%s-%s", appVersion, environment, testSuite).toLowerCase();
    }
}
