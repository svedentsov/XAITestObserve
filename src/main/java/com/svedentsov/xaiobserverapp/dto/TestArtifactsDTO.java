package com.svedentsov.xaiobserverapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class TestArtifactsDTO {
    private List<String> screenshotUrls;
    private String videoUrl;
    private List<String> appLogUrls;
    private String browserConsoleLogUrl;
    private String harFileUrl;
}
