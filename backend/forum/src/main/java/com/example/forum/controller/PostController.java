package com.example.forum.controller;

import com.example.forum.dto.PostRequest;
import com.example.forum.dto.PostResponse;
import com.example.forum.exceptions.ExceptionHandling;
import com.example.forum.model.HttpResponse;
import com.example.forum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(value = "/post")
public class PostController extends ExceptionHandling {
    private static final String POST_DELETED_SUCCESSFULLY = "Post deleted successfully!";
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create/")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest postRequest) {
        postService.save(postRequest);
        return new ResponseEntity<>(CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return new ResponseEntity<>(posts, OK);
    }

    @GetMapping("/ get/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable("id") Long id) {
        PostResponse postResponse = postService.getPost(id);
        return new ResponseEntity<>(postResponse, OK);
    }

    @GetMapping("/by-topic/{topicName}")
    public ResponseEntity<List<PostResponse>> getPostsByTopic(@PathVariable("topicName") String topicName){
        List<PostResponse> posts = postService.getPostsByTopic(topicName);
        return new ResponseEntity<>(posts, OK);
    }

    @GetMapping("/by-user/{username}")
    public ResponseEntity<List<PostResponse>> getPostsByUser(@PathVariable("username") String username){
        List<PostResponse> posts = postService.getPostsByUser(username);
        return new ResponseEntity<>(posts, OK);
    }

    @DeleteMapping("/post-delete/delete/{id}")
    public ResponseEntity<HttpResponse> deletePost(@PathVariable("id") Long id){
        postService.deletePost(id);
        return response(NO_CONTENT, POST_DELETED_SUCCESSFULLY);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        HttpResponse body = new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase(), message);
        return new ResponseEntity<>(body, httpStatus);
    }
}
