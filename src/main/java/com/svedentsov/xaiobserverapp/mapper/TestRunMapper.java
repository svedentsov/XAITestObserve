package com.svedentsov.xaiobserverapp.mapper;

import com.svedentsov.xaiobserverapp.dto.EnvironmentDetailsDTO;
import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.TestArtifactsDTO;
import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.model.EmbeddableEnvironmentDetails;
import com.svedentsov.xaiobserverapp.model.EmbeddableTestArtifacts;
import com.svedentsov.xaiobserverapp.model.TestRun;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MapStruct маппер для преобразования между DTO событий {@link FailureEventDTO},
 * сущностью {@link TestRun} и DTO детальной информации {@link TestRunDetailDTO}.
 */
@Mapper(componentModel = "spring", uses = {AnalysisResultMapper.class, TestConfigurationMapper.class})
public interface TestRunMapper {
    /**
     * Преобразует {@link FailureEventDTO} в сущность {@link TestRun}.
     *
     * @param dto DTO события завершения теста.
     * @return Сущность TestRun, готовая к сохранению.
     */
    @Mapping(target = "id", source = "testRunId")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "longToLocalDateTime")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "longToLocalDateTime")
    @Mapping(target = "timestamp", source = "endTime", qualifiedByName = "longToLocalDateTime")
    @Mapping(target = "status", expression = "java(com.svedentsov.xaiobserverapp.model.TestRun.TestStatus.fromString(dto.status()))")
    @Mapping(target = "failedStep", source = "failedStep")
    @Mapping(target = "executionPath", source = "executionPath")
    @Mapping(target = "testTags", source = "testTags")
    @Mapping(target = "environmentDetails", source = "environmentDetails")
    @Mapping(target = "artifacts", source = "artifacts")
    @Mapping(target = "customMetadata", source = "customMetadata")
    @Mapping(target = "configuration", ignore = true)
    @Mapping(target = "analysisResults", ignore = true)
    TestRun toEntity(FailureEventDTO dto);

    /**
     * Преобразует сущность {@link TestRun} в {@link TestRunDetailDTO} для отправки через API.
     *
     * @param entity Сущность тестового запуска из базы данных.
     * @return Детальное DTO.
     */
    @Mapping(source = "configuration", target = "configuration")
    TestRunDetailDTO toDetailDto(TestRun entity);

    /**
     * Преобразует {@link EnvironmentDetailsDTO} во встраиваемый объект {@link EmbeddableEnvironmentDetails}.
     *
     * @param dto DTO с деталями окружения.
     * @return Встраиваемый объект для сущности.
     */
    EmbeddableEnvironmentDetails toEmbeddable(EnvironmentDetailsDTO dto);

    /**
     * Преобразует {@link TestArtifactsDTO} во встраиваемый объект {@link EmbeddableTestArtifacts}.
     *
     * @param dto DTO с артефактами.
     * @return Встраиваемый объект для сущности.
     */
    EmbeddableTestArtifacts toEmbeddable(TestArtifactsDTO dto);

    /**
     * Пользовательский метод для преобразования времени в миллисекундах (Unix epoch) в {@link LocalDateTime}.
     *
     * @param epochMilli Время в миллисекундах.
     * @return Объект LocalDateTime или null, если входное значение некорректно.
     */
    @Named("longToLocalDateTime")
    default LocalDateTime longToLocalDateTime(long epochMilli) {
        return epochMilli > 0 ? LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault()) : null;
    }
}
