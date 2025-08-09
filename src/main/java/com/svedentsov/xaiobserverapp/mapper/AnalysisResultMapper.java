package com.svedentsov.xaiobserverapp.mapper;

import com.svedentsov.xaiobserverapp.dto.AnalysisResultDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct маппер для преобразования между сущностью {@link AnalysisResult} и {@link AnalysisResultDTO}.
 */
@Mapper(componentModel = "spring")
public interface AnalysisResultMapper {
    /**
     * Преобразует сущность {@link AnalysisResult} в {@link AnalysisResultDTO}.
     *
     * @param entity Сущность для преобразования.
     * @return DTO-представление.
     */
    @Mapping(target = "explanationData", source = "explanationData")
    AnalysisResultDTO toDto(AnalysisResult entity);
}
