package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.AnalysisFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для выполнения CRUD-операций с сущностями {@link AnalysisFeedback}.
 */
@Repository
public interface AnalysisFeedbackRepository extends JpaRepository<AnalysisFeedback, Long> {
}
