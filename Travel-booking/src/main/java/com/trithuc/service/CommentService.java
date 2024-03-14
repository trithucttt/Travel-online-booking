package com.trithuc.service;

import com.trithuc.model.Comment;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

public interface CommentService {


    @Transactional
    Comment createComment(Long postId, Long tourId, String content, String token);

    @Transactional
    Comment addReplyToComment(String token, Long commentId, String content);

    void sendCommentUpdate(Comment comment);

    void sendReplyUpdate(Comment repCm);
}
