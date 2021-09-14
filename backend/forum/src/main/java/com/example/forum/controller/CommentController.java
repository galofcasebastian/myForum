package com.example.forum.controller;

import com.example.forum.dto.CommentDto;
import com.example.forum.exceptions.ExceptionHandling;
import com.example.forum.model.HttpResponse;
import com.example.forum.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(value = "/comment")
public class CommentController extends ExceptionHandling {
    private static final String COMMENT_DELETED_SUCCESSFULLY = "Comment deleted successfully!";
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/create/")
    public ResponseEntity<CommentDto> createComment(@RequestBody CommentDto commentDto){
        commentService.save(commentDto);
        return new ResponseEntity<>(commentDto, CREATED);
    }

    @GetMapping("/by-post/{postId}")
    public ResponseEntity<List<CommentDto>> getAllCommentsForPost(@PathVariable("postId") Long postId){
        List<CommentDto> comments = commentService.getAllCommentsForPost(postId);
        return new ResponseEntity<>(comments, OK);
    }

    @GetMapping("/by-user/{username}")
    public ResponseEntity<List<CommentDto>> getAllCommentsForUser(@PathVariable("username") String username){
        List<CommentDto> comments = commentService.getAllCommentsForUser(username);
        return new ResponseEntity<>(comments, OK);
    }

    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<HttpResponse> deleteComment(@PathVariable("commentId") Long commentId){
        commentService.deleteComment(commentId);
        return response(NO_CONTENT, COMMENT_DELETED_SUCCESSFULLY);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        HttpResponse body = new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase(), message);
        return new ResponseEntity<>(body, httpStatus);
    }
}
