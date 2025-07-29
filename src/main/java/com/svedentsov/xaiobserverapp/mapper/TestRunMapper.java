package com.svedentsov.xaiobserverapp.mapper;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.model.TestRun;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Основной MapStruct маппер для преобразований, связанных с тестовыми запусками.
 * Конвертирует {@link FailureEventDTO} в сущность {@link TestRun} и {@link TestRun} в {@link TestRunDetailDTO}.
 */
@Mapper(componentModel = "spring", uses = {AnalysisResultMapper.class, TestConfigurationMapper.class})
public interface TestRunMapper {

    /**
     * Преобразует входящее событие о завершении теста в сущность для сохранения в БД.
     *
     * @param dto DTO события {@link FailureEventDTO}.
     * @return Сущность {@link TestRun}.
     */
    @Mapping(target = "id", source = "testRunId")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "longToLocalDateTime")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "longToLocalDateTime")
    @Mapping(target = "timestamp", source = "endTime", qualifiedByName = "longToLocalDateTime")
    @Mapping(target = "status", expression = "java(TestRun.TestStatus.valueOf(dto.getStatus().toUpperCase()))")
    @Mapping(target = "environment", source = "environmentDetails.name")
    @Mapping(target = "testClass", source = "testClass")
    @Mapping(target = "testMethod", source = "testMethod")
    @Mapping(target = "durationMillis", source = "durationMillis")
    @Mapping(target = "exceptionType", source = "exceptionType")
    @Mapping(target = "exceptionMessage", source = "exceptionMessage")
    @Mapping(target = "stackTrace", source = "stackTrace")
    @Mapping(target = "failedStep", source = "failedStep")
    @Mapping(target = "executionPath", source = "executionPath")
    @Mapping(target = "appVersion", source = "appVersion")
    @Mapping(target = "testSuite", source = "testSuite")
    @Mapping(target = "testTags", source = "testTags")
    @Mapping(target = "environmentDetails", source = "environmentDetails")
    @Mapping(target = "artifacts", source = "artifacts")
    @Mapping(target = "customMetadata", source = "customMetadata")
    @Mapping(target = "configuration", ignore = true)
    @Mapping(target = "analysisResults", ignore = true)
    TestRun toEntity(FailureEventDTO dto);

    /**
     * Преобразует сущность тестового запуска в детальный DTO для ответа клиенту.
     *
     * @param entity Сущность {@link TestRun}.
     * @return Детальный DTO {@link TestRunDetailDTO}.
     */
    TestRunDetailDTO toDetailDto(TestRun entity);

    /**
     * Вспомогательный метод для конвертации времени из long (Unix epoch milliseconds) в LocalDateTime.
     *
     * @param epochMilli Время в миллисекундах.
     * @return Объект {@link LocalDateTime} или null, если входное значение некорректно.
     */
    @Named("longToLocalDateTime")
    default LocalDateTime longToLocalDateTime(long epochMilli) {
        return epochMilli > 0 ? LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault()) : null;
    }
}
