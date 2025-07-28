package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.TestRun;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRunRepository extends JpaRepository<TestRun, String> {
    @EntityGraph(attributePaths = "configuration")
    List<TestRun> findAllByOrderByTimestampDesc();

    @Override
    @EntityGraph(attributePaths = {"configuration", "analysisResults", "analysisResults.feedback"})
    Optional<TestRun> findById(String id);

    @Query("SELECT CONCAT(tr.testClass, '.', tr.testMethod) as testName, COUNT(tr.id) as failureCount " +
            "FROM TestRun tr WHERE tr.status = 'FAILED' " +
            "GROUP BY testName ORDER BY failureCount DESC")
    List<Object[]> findTopFailingTests();

    @EntityGraph(attributePaths = {"configuration", "analysisResults"})
    @Query("SELECT tr FROM TestRun tr WHERE tr.timestamp >= :startOfDay AND tr.timestamp < :endOfDay")
    List<TestRun> findByTimestampBetween(@Param("startOfDay") java.time.LocalDateTime startOfDay, @Param("endOfDay") java.time.LocalDateTime endOfDay);
}
