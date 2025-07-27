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
    @Column(unique = true, length = 512)
    private String uniqueName;
}
