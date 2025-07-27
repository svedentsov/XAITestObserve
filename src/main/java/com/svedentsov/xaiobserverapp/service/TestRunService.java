package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.repository.TestRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestRunService {
    private final TestRunRepository testRunRepository;
    private final CacheManager cacheManager;

    @Transactional(readOnly = true)
    public List<TestRun> getAllTestRunsOrderedByTimestampDesc() {
        return testRunRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public Optional<TestRun> getTestRunById(String id) {
        return testRunRepository.findById(id);
    }

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
