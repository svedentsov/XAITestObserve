package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    Optional<AnalysisResult> findById(String analysisId);
}
