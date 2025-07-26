package com.svedentsov.xaiobserverapp.mapper;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.model.TestRun;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring", uses = {AnalysisResultMapper.class, TestConfigurationMapper.class})
public interface TestRunMapper {
    @Mappings({
            @Mapping(target = "id", source = "testRunId"),
            @Mapping(target = "startTime", expression = "java(toLocalDateTime(dto.getStartTime()))"),
            @Mapping(target = "endTime", expression = "java(toLocalDateTime(dto.getEndTime()))"),
            @Mapping(target = "timestamp", expression = "java(toLocalDateTime(dto.getEndTime()))"),
            @Mapping(target = "status", expression = "java(TestRun.TestStatus.valueOf(dto.getStatus().toUpperCase()))"),
            @Mapping(target = "environment", source = "environmentDetails.name"),
            @Mapping(target = "analysisResults", ignore = true),
            @Mapping(target = "configuration", ignore = true)
    })
    TestRun toEntity(FailureEventDTO dto);

    TestRunDetailDTO toDetailDto(TestRun entity);

    default LocalDateTime toLocalDateTime(long epochMilli) {
        return epochMilli > 0 ? LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault()) : null;
    }
}
