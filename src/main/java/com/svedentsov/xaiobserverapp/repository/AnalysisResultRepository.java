package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для выполнения CRUD-операций с сущностями {@link AnalysisResult}.
 */
@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, String> {
    /**
     * Находит результат анализа по его ID.
     *
     * @param analysisId Уникальный идентификатор анализа.
     * @return Optional, содержащий сущность, если она найдена.
     */
    Optional<AnalysisResult> findById(String analysisId);
}
