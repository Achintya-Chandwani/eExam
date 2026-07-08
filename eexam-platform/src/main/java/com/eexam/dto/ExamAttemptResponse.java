package com.eexam.dto;

import com.eexam.model.Exam;
import com.eexam.model.ExamAttempt;

import java.time.LocalDateTime;

public class ExamAttemptResponse {

    private Long id;
    private ExamBrief exam;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private boolean submitted;
    private Integer score;
    private Integer totalMarks;

    public static ExamAttemptResponse from(ExamAttempt attempt) {
        ExamAttemptResponse dto = new ExamAttemptResponse();
        dto.id = attempt.getId();
        dto.exam = ExamBrief.from(attempt.getExam());
        dto.startedAt = attempt.getStartedAt();
        dto.submittedAt = attempt.getSubmittedAt();
        dto.submitted = attempt.isSubmitted();
        dto.score = attempt.getScore();
        dto.totalMarks = attempt.getTotalMarks();
        return dto;
    }

    public Long getId() { return id; }
    public ExamBrief getExam() { return exam; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public boolean isSubmitted() { return submitted; }
    public Integer getScore() { return score; }
    public Integer getTotalMarks() { return totalMarks; }

    // Small nested view of the exam, resolved from the lazy proxy while the session is open
    public static class ExamBrief {
        private Long id;
        private String title;
        private Integer durationMinutes;
        private LocalDateTime scheduledStart;
        private LocalDateTime scheduledEnd;

        public static ExamBrief from(Exam exam) {
            ExamBrief brief = new ExamBrief();
            brief.id = exam.getId();
            brief.title = exam.getTitle();
            brief.durationMinutes = exam.getDurationMinutes();
            brief.scheduledStart = exam.getScheduledStart();
            brief.scheduledEnd = exam.getScheduledEnd();
            return brief;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public LocalDateTime getScheduledStart() { return scheduledStart; }
        public LocalDateTime getScheduledEnd() { return scheduledEnd; }
    }
}
