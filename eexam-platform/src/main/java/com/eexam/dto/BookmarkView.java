package com.eexam.dto;

import com.eexam.model.Bookmark;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookmarkView {
    private Long bookmarkId;
    private PracticeQuestionView question;

    public static BookmarkView from(Bookmark b) {
        return new BookmarkView(b.getId(), PracticeQuestionView.from(b.getQuestion()));
    }
}
