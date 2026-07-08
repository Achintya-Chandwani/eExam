package com.eexam.repository;

import com.eexam.model.Answer;
import com.eexam.model.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByAttempt(ExamAttempt attempt);
    Optional<Answer> findByAttemptAndQuestionId(ExamAttempt attempt, Long questionId);
}
