package com.svedentsov.xaiobserverapp.mapper;

import com.svedentsov.xaiobserverapp.dto.TestConfigurationDTO;
import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import org.mapstruct.Mapper;

/**
 * MapStruct маппер для преобразования между сущностью {@link TestConfiguration} и {@link TestConfigurationDTO}.
 */
@Mapper(componentModel = "spring")
public interface TestConfigurationMapper {

    /**
     * Преобразует сущность TestConfiguration в DTO.
     *
     * @param entity Сущность для преобразования.
     * @return DTO-представление.
     */
    TestConfigurationDTO toDto(TestConfiguration entity);

    /**
     * Преобразует DTO в сущность TestConfiguration.
     *
     * @param dto DTO для преобразования.
     * @return Сущность.
     */
    TestConfiguration toEntity(TestConfigurationDTO dto);
}
