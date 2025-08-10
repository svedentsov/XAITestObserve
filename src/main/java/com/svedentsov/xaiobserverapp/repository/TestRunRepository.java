package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.model.TestRun.TestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Репозиторий для выполнения CRUD-операций и сложных запросов с сущностями {@link TestRun}.
 * Использование EntityGraph'ов позволяет жадно загружать связанные сущности одним запросом,
 * что решает проблему N+1 и значительно повышает производительность.
 * JPQL-запросы, возвращающие проекции (`new map(...)`), являются высокопроизводительным подходом,
 * так как данные агрегируются в БД и возвращаются в виде легковесных объектов, а не полных сущностей.
 */
@Repository
public interface TestRunRepository extends JpaRepository<TestRun, String> {

    /**
     * Возвращает страницу тестовых запусков. Жадная загрузка оптимизирована для отображения списка.
     *
     * @param pageable параметры пагинации и сортировки.
     * @return Страница с тестовыми запусками.
     */
    @Override
    @EntityGraph(attributePaths = {"configuration", "testTags"})
    Page<TestRun> findAll(Pageable pageable);

    /**
     * Находит тестовый запуск по ID с жадной загрузкой всех связанных данных,
     * необходимых для детального отображения.
     *
     * @param id Уникальный идентификатор запуска.
     * @return Optional, содержащий TestRun.
     */
    @Override
    @EntityGraph(attributePaths = {"configuration", "analysisResults", "executionPath", "environmentDetails", "artifacts", "testTags", "customMetadata"})
    Optional<TestRun> findById(String id);

    /**
     * Подсчитывает количество запусков с заданным статусом.
     *
     * @param status Статус для подсчета.
     * @return Количество запусков.
     */
    @Query("SELECT count(tr) FROM TestRun tr WHERE tr.status = :status")
    long countByStatus(@Param("status") TestStatus status);

    /**
     * Находит самые нестабильные тесты (с наибольшим количеством падений).
     *
     * @param limit Ограничение на количество возвращаемых тестов.
     * @return Список карт, где каждая карта представляет тест и количество его сбоев.
     */
    @Query("SELECT new map(CONCAT(tr.testClass, '.', tr.testMethod) as testName, COUNT(tr.id) as failureCount) " +
            "FROM TestRun tr WHERE tr.status = 'FAILED' " +
            "GROUP BY testName ORDER BY failureCount DESC")
    List<Map<String, Object>> findTopFailingTests(Pageable limit);

    /**
     * Собирает данные для построения дневного тренда Pass Rate.
     *
     * @param sinceDate Дата, с которой начинать сбор статистики.
     * @return Список карт с датой, количеством успешных и общим количеством запусков.
     */
    @Query("SELECT new map(CAST(tr.timestamp AS DATE) as runDate, " +
            "SUM(CASE WHEN tr.status = 'PASSED' THEN 1 ELSE 0 END) as passedCount, " +
            "COUNT(tr.id) as totalCount) " +
            "FROM TestRun tr " +
            "WHERE tr.timestamp >= :sinceDate " +
            "GROUP BY runDate ORDER BY runDate ASC")
    List<Map<String, Object>> findDailyTrendData(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Находит самые медленные тесты по средней продолжительности выполнения.
     *
     * @param limit Ограничение на количество возвращаемых тестов.
     * @return Список карт с именем теста и его средней продолжительностью.
     */
    @Query("SELECT new map(CONCAT(tr.testClass, '.', tr.testMethod) as testName, " +
            "AVG(tr.durationMillis) as avgDuration) " +
            "FROM TestRun tr " +
            "GROUP BY testName " +
            "ORDER BY avgDuration DESC")
    List<Map<String, Object>> findTopSlowestTests(Pageable limit);

    /**
     * Подсчитывает количество запусков для каждого тестового набора.
     *
     * @return Список карт с именем набора и количеством запусков.
     */
    @Query("SELECT new map(tc.testSuite as name, COUNT(tr.id) as count) " +
            "FROM TestRun tr JOIN tr.configuration tc GROUP BY tc.testSuite ORDER BY count DESC")
    List<Map<String, Object>> countRunsBySuite();

    /**
     * Подсчитывает количество запусков для каждого окружения.
     *
     * @return Список карт с именем окружения и количеством запусков.
     */
    @Query("SELECT new map(tc.environment as name, COUNT(tr.id) as count) " +
            "FROM TestRun tr JOIN tr.configuration tc GROUP BY tc.environment ORDER BY count DESC")
    List<Map<String, Object>> countRunsByEnvironment();

    /**
     * Находит самые частые типы исключений в проваленных тестах.
     *
     * @param limit Ограничение на количество возвращаемых типов исключений.
     * @return Список карт с типом исключения и частотой его возникновения.
     */
    @Query("SELECT new map(tr.exceptionType as name, COUNT(tr.id) as count) " +
            "FROM TestRun tr WHERE tr.status = 'FAILED' AND tr.exceptionType IS NOT NULL " +
            "GROUP BY tr.exceptionType ORDER BY count DESC")
    List<Map<String, Object>> findTopExceptionTypes(Pageable limit);

    /**
     * Вычисляет среднюю продолжительность всех тестовых запусков.
     *
     * @return Optional с результатом, может быть пустым, если запусков нет.
     */
    @Query("SELECT AVG(tr.durationMillis) FROM TestRun tr")
    Optional<Double> findAverageTestDuration();

    /**
     * Подсчитывает количество уникальных тестов (по комбинации класса и метода).
     *
     * @return Общее количество уникальных тестов.
     */
    @Query("SELECT COUNT(DISTINCT CONCAT(tr.testClass, '.', tr.testMethod)) FROM TestRun tr")
    long countDistinctTests();
}
