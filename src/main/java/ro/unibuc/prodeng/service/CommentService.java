package ro.unibuc.prodeng.service;


import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.CommentEntity;
import ro.unibuc.prodeng.model.LikeTargetTypeEnum;
import ro.unibuc.prodeng.model.PostEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.CommentRepository;
import ro.unibuc.prodeng.repository.LikeRepository;
import ro.unibuc.prodeng.repository.PostRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateCommentRequest;
import ro.unibuc.prodeng.response.CommentResponse;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentService(
        CommentRepository commentRepository,
        PostRepository postRepository,
        LikeRepository likeRepository,
        UserRepository userRepository,
        NotificationService notificationService
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public CommentResponse createComment(
        String postId, CreateCommentRequest request
    ) {
        postRepository.findById(postId)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Post " + postId
                )
            );

        CommentEntity comment = new CommentEntity(
            null,
            postId,
            request.authorId(),
            request.content(),
            0,
            LocalDateTime.now()
        );

        CommentEntity saved = commentRepository.save(comment);

        UserEntity user = userRepository.findById(request.authorId()).get();

        PostEntity post = postRepository.findById(postId).get();

        notificationService.createNotification(post.authorId(), "User " + user.username() + " has commented on your post!", request.authorId());

        return toResponse(saved);
    }

    public List<CommentResponse> getCommentsByPostId(
        String postId
    ) {
        return commentRepository
            .findByPostIdOrderByCreatedAtDesc(postId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public void deleteComment(String commentId) {
        CommentEntity comment = commentRepository
            .findById(commentId)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Comment " + commentId
                )
            );

        likeRepository.deleteAllByTargetIdAndTargetType(
            commentId, LikeTargetTypeEnum.COMMENT
        );
        commentRepository.delete(comment);
    }

    public CommentResponse toResponse(CommentEntity comment) {
        return new CommentResponse(
            comment.id(),
            comment.postId(),
            comment.authorId(),
            comment.content(),
            comment.likesCount(),
            comment.createdAt()
        );
    }
}