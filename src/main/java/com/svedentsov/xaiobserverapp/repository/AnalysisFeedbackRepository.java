package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.AnalysisFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для доступа к данным {@link AnalysisFeedback}.
 * Предоставляет стандартные операции CRUD (Create, Read, Update, Delete)
 * для сущностей обратной связи по анализу.
 */
@Repository
public interface AnalysisFeedbackRepository extends JpaRepository<AnalysisFeedback, Long> {
}
