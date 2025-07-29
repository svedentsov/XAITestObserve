package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.mapper.TestRunMapper;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.repository.TestRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для выполнения основных CRUD-операций с тестовыми запусками.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestRunService {

    private final TestRunRepository testRunRepository;
    private final CacheManager cacheManager;
    private final TestRunMapper testRunMapper;

    /**
     * Возвращает все тестовые запуски, отсортированные по времени в порядке убывания.
     *
     * @return Список сущностей {@link TestRun}.
     */
    @Transactional(readOnly = true)
    public List<TestRun> getAllTestRunsOrderedByTimestampDesc() {
        return testRunRepository.findAllByOrderByTimestampDesc();
    }

    /**
     * Находит тестовый запуск по его ID.
     *
     * @param id Уникальный идентификатор запуска.
     * @return Optional с найденной сущностью {@link TestRun}.
     */
    @Transactional(readOnly = true)
    public Optional<TestRun> getTestRunById(String id) {
        return testRunRepository.findById(id);
    }

    /**
     * Удаляет все тестовые запуски и очищает связанные кэши.
     */
    @Transactional
    public void deleteAllTestRuns() {
        testRunRepository.deleteAll();
        log.info("All TestRun entities have been deleted.");
        // Очистка кэша статистики
        Optional.ofNullable(cacheManager.getCache("statistics")).ifPresent(cache -> {
            cache.clear();
            log.info("Statistics cache has been cleared.");
        });
    }

    /**
     * Возвращает список всех тестовых запусков за сегодняшний день.
     *
     * @return Список DTO {@link TestRunDetailDTO}.
     */
    @Transactional(readOnly = true)
    public List<TestRunDetailDTO> getTestRunsForToday() {
        LocalDate today = LocalDate.now();
        List<TestRun> runs = testRunRepository.findByTimestampBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        return runs.stream()
                .map(testRunMapper::toDetailDto)
                .collect(Collectors.toList());
    }
}
