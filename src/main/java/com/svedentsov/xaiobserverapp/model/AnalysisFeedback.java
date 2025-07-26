package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private AnalysisResult analysisResult;
    private String userId;
    private LocalDateTime feedbackTimestamp;
    private Boolean isAiSuggestionCorrect;
    @Column(length = 2000)
    private String userProvidedReason;
    @Column(length = 2000)
    private String userProvidedSolution;
    @Column(length = 1000)
    private String comments;
}
