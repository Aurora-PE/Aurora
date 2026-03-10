package ro.unibuc.prodeng.service;


import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.DuplicateActionException;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import ro.unibuc.prodeng.model.*;
import ro.unibuc.prodeng.repository.CommentRepository;
import ro.unibuc.prodeng.repository.LikeRepository;
import ro.unibuc.prodeng.repository.PostRepository;
import ro.unibuc.prodeng.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LikeService(
        LikeRepository likeRepository,
        PostRepository postRepository,
        CommentRepository commentRepository,
        UserRepository userRepository,
        NotificationService notificationService
    ) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public void likePost(String postId, String userId) {
        PostEntity post = postRepository.findById(postId)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Post " + postId
                )
            );

        if (likeRepository
            .findByUserIdAndTargetIdAndTargetType(
                userId, postId, LikeTargetTypeEnum.POST
            ).isPresent()) {
            throw new DuplicateActionException(
                "User already liked this post"
            );
        }

        Like like = new Like(
            null, userId, postId,
            LikeTargetTypeEnum.POST, LocalDateTime.now()
        );
        likeRepository.save(like);

        PostEntity updated = new PostEntity(
            post.id(), post.authorId(), post.content(),
            post.imageUrl(), post.visibility(),
            post.likesCount() + 1, post.localDateTime()
        );
        postRepository.save(updated);

        UserEntity user = userRepository.findById(userId).get();

        notificationService.createNotification(post.authorId(), "User " + user.username() + " has liked your post!", userId);
    }

    public void unlikePost(String postId, String userId) {
        PostEntity post = postRepository.findById(postId)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Post " + postId
                )
            );

        Like like = likeRepository
            .findByUserIdAndTargetIdAndTargetType(
                userId, postId, LikeTargetTypeEnum.POST
            )
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Like "
                )
            );

        likeRepository.delete(like);

        PostEntity updated = new PostEntity(
            post.id(), post.authorId(), post.content(),
            post.imageUrl(), post.visibility(),
            Math.max(0, post.likesCount() - 1),
            post.localDateTime()
        );
        postRepository.save(updated);
    }

    public void likeComment(String commentId, String userId) {
        CommentEntity comment = commentRepository.findById(commentId)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Comment " + commentId
                )
            );

        if (likeRepository
            .findByUserIdAndTargetIdAndTargetType(
                userId, commentId, LikeTargetTypeEnum.COMMENT
            ).isPresent()) {
            throw new DuplicateActionException(
                "User already liked this comment"
            );
        }

        Like like = new Like(
            null, userId, commentId,
            LikeTargetTypeEnum.COMMENT, LocalDateTime.now()
        );
        likeRepository.save(like);

        CommentEntity updated = new CommentEntity(
            comment.id(), comment.postId(),
            comment.authorId(), comment.content(),
            comment.likesCount() + 1, comment.createdAt()
        );
        commentRepository.save(updated);

        UserEntity user = userRepository.findById(userId).get();

        notificationService.createNotification(comment.authorId(), "User " + user.username() + " has liked your comment!", userId);
    }
}