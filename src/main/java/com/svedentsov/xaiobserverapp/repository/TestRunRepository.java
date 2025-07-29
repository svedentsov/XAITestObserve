package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.TestRun;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для выполнения CRUD-операций и кастомных запросов с сущностями {@link TestRun}.
 */
@Repository
public interface TestRunRepository extends JpaRepository<TestRun, String> {

    /**
     * Находит все тестовые запуски, отсортированные по времени завершения в порядке убывания.
     * Использует EntityGraph для жадной загрузки связанных конфигураций.
     *
     * @return Список тестовых запусков.
     */
    @EntityGraph(attributePaths = "configuration")
    List<TestRun> findAllByOrderByTimestampDesc();

    /**
     * Переопределяет стандартный findById для жадной загрузки всех необходимых связей.
     *
     * @param id ID тестового запуска.
     * @return Optional с полностью инициализированной сущностью.
     */
    @Override
    @EntityGraph(attributePaths = {"configuration", "analysisResults", "analysisResults.feedback"})
    Optional<TestRun> findById(String id);

    /**
     * Находит топ нестабильных тестов, подсчитывая количество их падений.
     *
     * @return Список массивов Object[], где [0] - имя теста, [1] - количество падений.
     */
    @Query("SELECT CONCAT(tr.testClass, '.', tr.testMethod) as testName, COUNT(tr.id) as failureCount " +
            "FROM TestRun tr WHERE tr.status = 'FAILED' " +
            "GROUP BY testName ORDER BY failureCount DESC")
    List<Object[]> findTopFailingTests();

    /**
     * Находит все тестовые запуски за определенный временной интервал (обычно за день).
     *
     * @param startOfDay Начало временного интервала.
     * @param endOfDay   Конец временного интервала.
     * @return Список тестовых запусков.
     */
    @EntityGraph(attributePaths = {"configuration", "analysisResults"})
    @Query("SELECT tr FROM TestRun tr WHERE tr.timestamp >= :startOfDay AND tr.timestamp < :endOfDay")
    List<TestRun> findByTimestampBetween(@Param("startOfDay") java.time.LocalDateTime startOfDay, @Param("endOfDay") java.time.LocalDateTime endOfDay);
}
