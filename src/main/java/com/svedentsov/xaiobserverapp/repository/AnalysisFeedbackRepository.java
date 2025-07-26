package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.AnalysisFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisFeedbackRepository extends JpaRepository<AnalysisFeedback, Long> {
}
