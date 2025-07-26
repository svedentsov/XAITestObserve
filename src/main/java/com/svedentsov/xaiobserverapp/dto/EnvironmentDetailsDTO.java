package com.svedentsov.xaiobserverapp.dto;

import lombok.Data;

@Data
public class EnvironmentDetailsDTO {
    private String name;
    private String osType;
    private String osVersion;
    private String browserType;
    private String browserVersion;
    private String screenResolution;
    private String deviceType;
    private String deviceName;
    private String driverVersion;
    private String appBaseUrl;
}
