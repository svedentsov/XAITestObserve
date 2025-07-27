package com.svedentsov.xaiobserverapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "analysis_result")
public class AnalysisResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_run_id", nullable = false)
    private TestRun testRun;
    private String analysisType;
    @Column(length = 2000)
    private String suggestedReason;
    @Column(length = 4000)
    private String solution;
    private Double aiConfidence;
    private LocalDateTime analysisTimestamp;
    @Column(length = 4000)
    private String rawData;
    private Boolean userConfirmedCorrect;
    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AnalysisFeedback> feedback;
}
