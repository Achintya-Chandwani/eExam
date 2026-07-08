package com.eexam.dto;

import com.eexam.model.Exam;

import java.time.LocalDateTime;

public class ExamListResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private int questionCount;

    public static ExamListResponse from(Exam exam) {
        ExamListResponse dto = new ExamListResponse();
        dto.id = exam.getId();
        dto.title = exam.getTitle();
        dto.description = exam.getDescription();
        dto.durationMinutes = exam.getDurationMinutes();
        dto.scheduledStart = exam.getScheduledStart();
        dto.scheduledEnd = exam.getScheduledEnd();
        dto.questionCount = exam.getQuestions().size();
        return dto;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public LocalDateTime getScheduledStart() { return scheduledStart; }
    public LocalDateTime getScheduledEnd() { return scheduledEnd; }
    public int getQuestionCount() { return questionCount; }
}
