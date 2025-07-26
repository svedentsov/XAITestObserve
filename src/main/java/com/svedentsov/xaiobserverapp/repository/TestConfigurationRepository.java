package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestConfigurationRepository extends JpaRepository<TestConfiguration, Long> {
    Optional<TestConfiguration> findByUniqueName(String uniqueName);
}
