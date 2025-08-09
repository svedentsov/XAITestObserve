package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.TestRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для выполнения CRUD-операций и сложных запросов с сущностями {@link TestRun}.
 */
@Repository
public interface TestRunRepository extends JpaRepository<TestRun, String> {

    /**
     * Находит все тестовые запуски с пагинацией.
     * Использует {@code @EntityGraph} для эффективной загрузки связанной сущности {@code configuration}
     * одним запросом, избегая проблемы N+1.
     *
     * @param pageable объект с информацией о пагинации и сортировке.
     * @return страница с тестовыми запусками.
     */
    @EntityGraph(attributePaths = "configuration")
    Page<TestRun> findAll(Pageable pageable);

    /**
     * Находит тестовый запуск по его ID.
     * Использует {@code @EntityGraph} для жадной загрузки связанных сущностей
     * {@code configuration}, {@code analysisResults} и {@code analysisResults.feedback} одним запросом.
     *
     * @param id уникальный идентификатор тестового запуска.
     * @return Optional, содержащий сущность, если она найдена.
     */
    @Override
    @EntityGraph(attributePaths = {"configuration", "analysisResults", "analysisResults.feedback"})
    Optional<TestRun> findById(String id);

    /**
     * Находит тесты с наибольшим количеством падений.
     * Возвращает список массивов объектов, где первый элемент - полное имя теста,
     * а второй - количество его падений.
     *
     * @return список с результатами агрегации.
     */
    @Query("SELECT CONCAT(tr.testClass, '.', tr.testMethod) as testName, COUNT(tr.id) as failureCount " +
            "FROM TestRun tr WHERE tr.status = 'FAILED' " +
            "GROUP BY testName ORDER BY failureCount DESC")
    List<Object[]> findTopFailingTests();

    /**
     * Находит все тестовые запуски в заданном временном интервале.
     * Используется для расчета дневной статистики.
     *
     * @param startOfDay начало временного интервала.
     * @param endOfDay   конец временного интервала.
     * @return список тестовых запусков.
     */
    @EntityGraph(attributePaths = {"configuration", "analysisResults"})
    @Query("SELECT tr FROM TestRun tr WHERE tr.timestamp >= :startOfDay AND tr.timestamp < :endOfDay")
    List<TestRun> findByTimestampBetween(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}
