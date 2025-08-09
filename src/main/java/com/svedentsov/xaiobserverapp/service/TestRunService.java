package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.mapper.TestRunMapper;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.repository.TestRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Сервис для выполнения операций с сущностями {@link TestRun}.
 * Предоставляет методы для получения, удаления и пагинации тестовых запусков.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestRunService {

    private final TestRunRepository testRunRepository;
    private final CacheManager cacheManager;
    private final TestRunMapper testRunMapper;

    /**
     * Получает страницу с тестовыми запусками.
     *
     * @param pageable объект с параметрами пагинации и сортировки.
     * @return {@link Page} с {@link TestRunDetailDTO}.
     */
    @Transactional(readOnly = true)
    public Page<TestRunDetailDTO> getAllTestRunsPaginated(Pageable pageable) {
        return testRunRepository.findAll(pageable).map(testRunMapper::toDetailDto);
    }

    /**
     * Получает детальную информацию о тестовом запуске по его ID.
     *
     * @param id Уникальный идентификатор тестового запуска.
     * @return {@link Optional} с сущностью {@link TestRun}, если она найдена.
     */
    @Transactional(readOnly = true)
    public Optional<TestRun> getTestRunById(String id) {
        return testRunRepository.findById(id);
    }

    /**
     * Удаляет все тестовые запуски из базы данных.
     * Также очищает связанный кэш статистики.
     */
    @Transactional
    public void deleteAllTestRuns() {
        testRunRepository.deleteAll();
        log.info("All TestRun entities have been deleted.");
        Optional.ofNullable(cacheManager.getCache("statistics")).ifPresent(cache -> {
            cache.clear();
            log.info("Statistics cache has been cleared.");
        });
    }
}
