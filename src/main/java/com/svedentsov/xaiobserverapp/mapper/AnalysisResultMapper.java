package com.svedentsov.xaiobserverapp.mapper;

import com.svedentsov.xaiobserverapp.dto.AnalysisResultDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnalysisResultMapper {
    AnalysisResultDTO toDto(AnalysisResult entity);
    AnalysisResult toEntity(AnalysisResultDTO dto);
}
