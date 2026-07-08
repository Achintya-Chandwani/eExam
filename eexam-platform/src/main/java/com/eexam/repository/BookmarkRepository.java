package com.eexam.repository;

import com.eexam.model.Bookmark;
import com.eexam.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByStudent(User student);
    Optional<Bookmark> findByStudentAndQuestionId(User student, Long questionId);
    void deleteByStudentAndQuestionId(User student, Long questionId);
}
