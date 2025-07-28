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

@Mapper(componentModel = "spring", uses = {AnalysisResultMapper.class, TestConfigurationMapper.class})
public interface TestRunMapper {

    @Mapping(target = "id", source = "testRunId")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "longToLocalDateTime")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "longToLocalDateTime")
    @Mapping(target = "timestamp", source = "endTime", qualifiedByName = "longToLocalDateTime")
    @Mapping(target = "status", expression = "java(TestRun.TestStatus.valueOf(dto.getStatus().toUpperCase()))")
    @Mapping(target = "environment", source = "environmentDetails.name")
    @Mapping(target = "configuration", ignore = true)
    @Mapping(target = "analysisResults", ignore = true)
    TestRun toEntity(FailureEventDTO dto);

    @Mapping(source = "configuration", target = "configuration")
    @Mapping(source = "analysisResults", target = "analysisResults")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "durationMillis", target = "durationMillis")
    @Mapping(source = "exceptionMessage", target = "exceptionMessage")
    @Mapping(source = "environmentDetails", target = "environmentDetails")
    @Mapping(source = "artifacts", target = "artifacts")
    @Mapping(source = "testTags", target = "testTags")
    @Mapping(source = "customMetadata", target = "customMetadata")
    TestRunDetailDTO toDetailDto(TestRun entity);

    @Named("longToLocalDateTime")
    default LocalDateTime longToLocalDateTime(long epochMilli) {
        return epochMilli > 0 ? LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault()) : null;
    }
}
