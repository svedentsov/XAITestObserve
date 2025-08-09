package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для выполнения CRUD-операций с сущностями {@link TestConfiguration}.
 */
@Repository
public interface TestConfigurationRepository extends JpaRepository<TestConfiguration, Long> {
    /**
     * Находит конфигурацию по ее уникальному имени.
     *
     * @param uniqueName Уникальное имя конфигурации.
     * @return Optional, содержащий сущность, если она найдена.
     */
    Optional<TestConfiguration> findByUniqueName(String uniqueName);
}
