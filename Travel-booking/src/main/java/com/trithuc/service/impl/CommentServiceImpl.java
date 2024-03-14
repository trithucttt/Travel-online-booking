package com.trithuc.service.impl;

import com.trithuc.model.Comment;
import com.trithuc.model.PostTour;
import com.trithuc.model.User;
import com.trithuc.repository.CommentRepository;
import com.trithuc.repository.PostTourRepository;
import com.trithuc.repository.UserRepository;
import com.trithuc.service.CommentService;
import com.trithuc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostTourRepository postTourRepository;
    @Autowired
    private SimpMessageSendingOperations messageTemplate;

    @Override
    @Transactional
    public Comment createComment(Long postId, Long tourId, String content, String token) {
        String username = userService.Authentication(token);
        User currentUser = userRepository.findByUsername(username);
        PostTour postTour = postTourRepository.findByPostIdAndTourId(postId, tourId).orElse(null);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }
        if (postTour != null) {
            Comment comment = new Comment();
            comment.setUser(currentUser);
            comment.setContent(content);
            comment.setStart_time(LocalDateTime.now());
            comment.setPostTour(postTour);
            return commentRepository.save(comment);
        } else {
             throw new RuntimeException("Post id or tour id is incorrect");
        }
    }


    @Override
    @Transactional
    public Comment addReplyToComment(String token, Long commentId, String content) {

        String username = userService.Authentication(token);
        User currentUser = userRepository.findByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }
        Comment parentComment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        Comment repComment = new Comment();
        repComment.setUser(currentUser);
        repComment.setContent(content);
        repComment.setStart_time(LocalDateTime.now());
        repComment.setParent(parentComment);
        repComment.setPostTour(parentComment.getPostTour());
        parentComment.getReplies().add(repComment);
       return commentRepository.save(repComment);

    }

    @Override
    public void sendCommentUpdate(Comment comment) {
        messageTemplate.convertAndSend("/topic/comments" + comment.getPostTour().getId(), comment);
    }

    @Override
    public void sendReplyUpdate(Comment repCm) {
        messageTemplate.convertAndSend("/topic/comments" + repCm.getParent().getId() + "/replies", repCm);
    }
}
