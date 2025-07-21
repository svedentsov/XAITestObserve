package com.svedentsov.xaiobserverapp.repository;

import com.svedentsov.xaiobserverapp.model.TestRun;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для доступа к данным {@link TestRun}.
 * Предоставляет стандартные операции CRUD и специфические запросы
 * для получения списков тестовых запусков, включая связанные сущности.
 */
@Repository
public interface TestRunRepository extends JpaRepository<TestRun, String> {
    /**
     * Находит все тестовые запуски, отсортированные по временной метке в убывающем порядке.
     * Использует {@link EntityGraph} для немедленной загрузки связанной {@code configuration}.
     *
     * @return Список объектов {@link TestRun}.
     */
    @EntityGraph(attributePaths = "configuration")
    List<TestRun> findAllByOrderByTimestampDesc();

    /**
     * Находит тестовый запуск по его уникальному идентификатору.
     * Переопределяет стандартный метод {@code findById} для использования {@link EntityGraph},
     * что позволяет немедленно загружать связанные сущности:
     * {@code configuration}, {@code analysisResults}, и {@code analysisResults.feedback}.
     * Это полезно для отображения полной информации о тестовом запуске на странице деталей.
     *
     * @param id Уникальный идентификатор тестового запуска.
     * @return {@link Optional}, содержащий найденный тестовый запуск, или пустой {@link Optional}, если не найден.
     */
    @Override
    @EntityGraph(attributePaths = {"configuration", "analysisResults", "analysisResults.feedback"})
    Optional<TestRun> findById(String id);

    /**
     * Выполняет SQL-запрос для определения наиболее часто падающих тестов.
     * Возвращает список объектов, каждый из которых представляет собой массив,
     * содержащий имя теста ({@code testClass.testMethod}) и количество его сбоев.
     * Результаты отсортированы по убыванию количества сбоев.
     *
     * @return Список объектов, содержащих имя теста и количество сбоев.
     */
    @Query("SELECT CONCAT(tr.testClass, '.', tr.testMethod) as testName, COUNT(tr.id) as failureCount " +
            "FROM TestRun tr WHERE tr.status = 'FAILED' " +
            "GROUP BY testName ORDER BY failureCount DESC")
    List<Object[]> findTopFailingTests();
}
