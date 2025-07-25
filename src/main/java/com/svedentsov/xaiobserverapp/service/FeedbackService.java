package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.AnalysisFeedbackDTO;
import com.svedentsov.xaiobserverapp.exception.ResourceNotFoundException;
import com.svedentsov.xaiobserverapp.model.AnalysisFeedback;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.repository.AnalysisFeedbackRepository;
import com.svedentsov.xaiobserverapp.repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final AnalysisResultRepository analysisResultRepository;
    private final AnalysisFeedbackRepository analysisFeedbackRepository;

    @Transactional
    public AnalysisFeedback processFeedback(String analysisId, AnalysisFeedbackDTO feedbackDTO) {
        log.info("Processing feedback for analysis ID {}: isCorrect={}", analysisId, feedbackDTO.getIsAiSuggestionCorrect());

        AnalysisResult analysisResult = analysisResultRepository.findById(Long.valueOf(analysisId))
                .orElseThrow(() -> new ResourceNotFoundException("Analysis with ID " + analysisId + " not found"));

        analysisResult.setUserConfirmedCorrect(feedbackDTO.getIsAiSuggestionCorrect());

        AnalysisFeedback feedback = new AnalysisFeedback();
        feedback.setAnalysisResult(analysisResult);
        feedback.setIsAiSuggestionCorrect(feedbackDTO.getIsAiSuggestionCorrect());
        feedback.setUserProvidedReason(feedbackDTO.getUserProvidedReason());
        feedback.setUserProvidedSolution(feedbackDTO.getUserProvidedSolution());
        feedback.setComments(feedbackDTO.getComments());
        feedback.setUserId(StringUtils.hasText(feedbackDTO.getUserId()) ? feedbackDTO.getUserId() : "anonymous");
        feedback.setFeedbackTimestamp(LocalDateTime.now());

        return analysisFeedbackRepository.save(feedback);
    }
}
