package com.trithuc.controller;

import com.trithuc.model.Comment;
import com.trithuc.request.CommentRequest;
import com.trithuc.request.ReplyRequest;
import com.trithuc.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private SimpMessageSendingOperations messageSendingOperations;

    @PostMapping("add")
    public ResponseEntity<?> createComment(@RequestBody CommentRequest commentRequest,
                                                 @RequestHeader(name = "Authorization" )String token){
        Comment comment = commentService.createComment(commentRequest.getPostId(),commentRequest.getTourId(),commentRequest.getContent(),token);
       if(comment != null ){
           messageSendingOperations.convertAndSend("/topic/comments" + comment.getPostTour().getId(),comment);
       }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add comment");
    }

    @PostMapping("/reply")
    public ResponseEntity<?> addReply(@RequestBody ReplyRequest replyRequest,@RequestHeader(name = "Authorization" )String token) {
        Comment reply = commentService.addReplyToComment(token, replyRequest.getCommentId(), replyRequest.getContent());
        if (reply != null) {
            // Gửi thông tin reply mới tới tất cả clients đang lắng nghe
            messageSendingOperations.convertAndSend("/topic/comments/" + reply.getParent().getId() + "/replies", reply);
            return ResponseEntity.ok("Reply added successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add reply");
    }
}
