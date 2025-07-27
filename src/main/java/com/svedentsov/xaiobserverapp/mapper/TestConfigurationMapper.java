package com.svedentsov.xaiobserverapp.mapper;

import com.svedentsov.xaiobserverapp.dto.TestConfigurationDTO;
import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TestConfigurationMapper {
    TestConfigurationDTO toDto(TestConfiguration entity);
    TestConfiguration toEntity(TestConfigurationDTO dto);
}
