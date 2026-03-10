package ro.unibuc.prodeng.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.LikeTargetTypeEnum;
import ro.unibuc.prodeng.model.PostEntity;
import ro.unibuc.prodeng.repository.CommentRepository;
import ro.unibuc.prodeng.repository.LikeRepository;
import ro.unibuc.prodeng.repository.PostRepository;
import ro.unibuc.prodeng.request.CreatePostRequest;
import ro.unibuc.prodeng.request.UpdatePostRequest;
import ro.unibuc.prodeng.response.PostResponse;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

     public PostService(
        PostRepository postRepository,
        CommentRepository commentRepository,
        LikeRepository likeRepository
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;    
    }
    
    public PostResponse createPost(CreatePostRequest request) {
        PostEntity post = new PostEntity(
            null,
            request.authorId(),
            request.content(),
            request.imageUrl(),
            request.visibility(),
            0,
            LocalDateTime.now()
        );

        PostEntity saved = postRepository.save(post);
        return toResponse(saved);
    }

    public PostResponse getPost(String id) {
        PostEntity post = postRepository.findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Post " + id
                )
            );
        return toResponse(post);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public PostResponse updatePost(
        String id, UpdatePostRequest request
    ) {
        PostEntity existing = postRepository.findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Post " + id
                )
            );

        PostEntity updated = new PostEntity(
            existing.id(),
            existing.authorId(),
            request.content(),
            request.imageUrl(),
            request.visibility(),
            existing.likesCount(),
            existing.localDateTime()
        );

        PostEntity saved = postRepository.save(updated);
        return toResponse(saved);
    }

    public void deletePost(String id) {
        PostEntity post = postRepository.findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Post " + id
                )
            );
        likeRepository.deleteAllByTargetIdAndTargetType(
            id, LikeTargetTypeEnum.POST
        );

        commentRepository.deleteAllByPostId(id);
        postRepository.delete(post);
    }

    public PostResponse toResponse(PostEntity post) {
        return new PostResponse(
            post.id(),
            post.authorId(),
            post.content(),
            post.imageUrl(),
            post.visibility(),
            post.likesCount(),
            post.localDateTime()
        );
    }
}
