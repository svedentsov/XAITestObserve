package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;

public interface FailureAnalyzer {
    boolean canAnalyze(FailureEventDTO event);

    AnalysisResult analyze(FailureEventDTO event);
}
