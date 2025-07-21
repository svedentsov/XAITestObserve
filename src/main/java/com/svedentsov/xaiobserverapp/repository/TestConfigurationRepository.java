package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для доступа к данным {@link TestConfiguration}.
 * Предоставляет стандартные операции CRUD и специфический метод
 * для поиска конфигурации по уникальному имени.
 */
@Repository
public interface TestConfigurationRepository extends JpaRepository<TestConfiguration, Long> {
    /**
     * Находит тестовую конфигурацию по ее уникальному имени.
     *
     * @param uniqueName Уникальное имя конфигурации, сгенерированное из версии приложения, среды и тестового набора.
     * @return {@link Optional}, содержащий найденную конфигурацию, или пустой {@link Optional}, если не найдена.
     */
    Optional<TestConfiguration> findByUniqueName(String uniqueName);
}
