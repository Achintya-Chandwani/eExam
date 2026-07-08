package com.eexam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PerformanceReportDto {
    private Long examId;
    private String examTitle;
    private int totalMarks;
    private int totalAttempts;
    private double averageScore;
    private double highestScore;
    private double lowestScore;
    private List<StudentResult> studentResults;

    @Data
    @AllArgsConstructor
    public static class StudentResult {
        private String studentUsername;
        private String studentFullName;
        private Integer score;
        private Integer totalMarks;
        private String submittedAt;
    }
}
