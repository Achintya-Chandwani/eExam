package com.eexam.dto;

import com.eexam.model.Question;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PracticeQuestionView {
    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private Integer marks;
    private Long examId;
    private String examTitle;

    public static PracticeQuestionView from(Question q) {
        return new PracticeQuestionView(
                q.getId(), q.getQuestionText(), q.getOptionA(), q.getOptionB(),
                q.getOptionC(), q.getOptionD(), q.getCorrectOption(), q.getMarks(),
                q.getExam().getId(), q.getExam().getTitle()
        );
    }
}
