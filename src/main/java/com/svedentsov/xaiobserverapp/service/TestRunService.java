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

@Slf4j
@Service
@RequiredArgsConstructor
public class TestRunService {
    private final TestRunRepository testRunRepository;
    private final CacheManager cacheManager;
    private final TestRunMapper testRunMapper;

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

    @Transactional(readOnly = true)
    public List<TestRunDetailDTO> getTestRunsForToday() {
        LocalDate today = LocalDate.now();
        List<TestRun> runs = testRunRepository.findByTimestampBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        return runs.stream()
                .map(testRunMapper::toDetailDto)
                .collect(Collectors.toList());
    }
}
