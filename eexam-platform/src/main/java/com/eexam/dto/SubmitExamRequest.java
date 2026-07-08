package com.eexam.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitExamRequest {
    private List<AnswerSubmission> answers;

    @Data
    public static class AnswerSubmission {
        private Long questionId;
        private String selectedOption; // A, B, C, D
    }
}
