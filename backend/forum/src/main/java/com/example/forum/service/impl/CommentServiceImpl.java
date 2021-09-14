package com.example.forum.service.impl;

import com.example.forum.dto.CommentDto;
import com.example.forum.exceptions.domain.ForumException;
import com.example.forum.mapper.CommentMapper;
import com.example.forum.model.Comment;
import com.example.forum.model.NotificationEmail;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.CommentService;
import com.example.forum.service.MailService;
import com.example.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {
    private static String POST_URL = "http://localhost:4200/view-post/";
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Autowired
    public CommentServiceImpl(PostRepository postRepository, CommentMapper commentMapper, UserService userService, CommentRepository commentRepository,
                              UserRepository userRepository, MailService mailService) {
        this.postRepository = postRepository;
        this.commentMapper = commentMapper;
        this.userService = userService;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @Override
    public void save(CommentDto commentDto) {
        Post post = postRepository.findPostById(commentDto.getPostId());
        Comment comment = commentMapper.mapDtoToComment(commentDto, post, userService.getCurrentUser());
        commentRepository.save(comment);

        POST_URL += post.getId();
        if ((!userService.getCurrentUser().getUsername().equals(post.getUser().getUsername())) && (post.isSendNotification())) {
            mailService.sendMail(new NotificationEmail("New comment on one of your posts", post.getUser().getEmail(), "Hi, " +
                    post.getUser().getUsername() + "\n" + "We hope you are doing well\n\n" +
                    "Maybe you are interested in checking what's new on one of your posts\n" + userService.getCurrentUser().getUsername() +
                    " commented on your following post: " + POST_URL + "\n\nWe wish you all the best,\nSebi Prod SRL support team"));
        }
    }

    @Override
    public List<CommentDto> getAllCommentsForPost(Long postId) {
        Post post = postRepository.findPostById(postId);
        return commentRepository.findByPost(post).stream().map(commentMapper::mapCommentToDto).collect(toList());
    }

    @Override
    public List<CommentDto> getAllCommentsForUser(String username) {
        User user = userRepository.findUserByUsername(username);
        return commentRepository.findAllByUser(user).stream().map(commentMapper::mapCommentToDto).collect(toList());
    }

    @Override
    public void deleteComment(Long id) {
        User user = userService.getCurrentUser();
        int ok = 0;
        if (user.getRole().equals("ROLE_ADMIN") || user.getRole().equals("ROLE_USER")) {

            List<Comment> comments = commentRepository.findAllByUser(user);
            Comment comment = commentRepository.findCommentById(id);
            for (Comment c : comments) {
                if (c.equals(comment)) {
                    ok = 1;
                    break;
                }
            }
            if (ok == 1) {
                commentRepository.deleteById(id);
            } else {
                throw new ForumException("You do not have permission to take this action");
            }
        }
    }
}
