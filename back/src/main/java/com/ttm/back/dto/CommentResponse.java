package com.ttm.back.dto;

import com.ttm.back.model.TaskComment;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private UserResponse author;
    private String comment;
    private LocalDateTime createdAt;

    public static CommentResponse fromEntity(TaskComment comment, UserResponse author) {
        CommentResponse response = new CommentResponse();
        response.id = comment.getId();
        response.author = author;
        response.comment = comment.getComment();
        response.createdAt = comment.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public UserResponse getAuthor() {
        return author;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
