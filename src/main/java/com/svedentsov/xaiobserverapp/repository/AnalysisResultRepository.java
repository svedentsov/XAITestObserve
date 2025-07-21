package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для доступа к данным {@link AnalysisResult}.
 * Предоставляет стандартные операции CRUD (Create, Read, Update, Delete)
 * для сущностей результатов анализа.
 */
@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
}
