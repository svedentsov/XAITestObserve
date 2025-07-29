package com.svedentsov.xaiobserverapp.mapper;

import com.svedentsov.xaiobserverapp.dto.AnalysisResultDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import org.mapstruct.Mapper;

/**
 * MapStruct маппер для преобразования между сущностью {@link AnalysisResult} и {@link AnalysisResultDTO}.
 */
@Mapper(componentModel = "spring")
public interface AnalysisResultMapper {

    /**
     * Преобразует сущность AnalysisResult в DTO.
     *
     * @param entity Сущность для преобразования.
     * @return DTO-представление.
     */
    AnalysisResultDTO toDto(AnalysisResult entity);

    /**
     * Преобразует DTO в сущность AnalysisResult.
     *
     * @param dto DTO для преобразования.
     * @return Сущность.
     */
    AnalysisResult toEntity(AnalysisResultDTO dto);
}
