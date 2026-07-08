package com.eexam.dto;

import com.eexam.model.Question;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentQuestionView {
    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer marks;

    public static StudentQuestionView from(Question q) {
        return new StudentQuestionView(
                q.getId(), q.getQuestionText(), q.getOptionA(), q.getOptionB(),
                q.getOptionC(), q.getOptionD(), q.getMarks()
        );
    }
}
