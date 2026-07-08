package com.eexam.dto;

import com.eexam.model.Question;

public class QuestionResponse {

    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private Integer marks;

    public static QuestionResponse from(Question question) {
        QuestionResponse dto = new QuestionResponse();
        dto.id = question.getId();
        dto.questionText = question.getQuestionText();
        dto.optionA = question.getOptionA();
        dto.optionB = question.getOptionB();
        dto.optionC = question.getOptionC();
        dto.optionD = question.getOptionD();
        dto.correctOption = question.getCorrectOption();
        dto.marks = question.getMarks();
        return dto;
    }

    public Long getId() { return id; }
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectOption() { return correctOption; }
    public Integer getMarks() { return marks; }
}
