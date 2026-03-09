package ro.unibuc.prodeng.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.FollowEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.FollowRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.response.UserSummaryResponse;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public void followUser(String requesterId, String targetUserId) {
        if (targetUserId.equals(requesterId)) {
            throw new IllegalArgumentException("You cannot follow yourself.");
        }
        if (!userRepository.existsById(targetUserId)) {
            throw new EntityNotFoundException("User "+ targetUserId);
        }
        if (followRepository.existsByFollowerIdAndFollowingId(requesterId, targetUserId)) {
            throw new IllegalArgumentException("You are already following this user.");
        }

        followRepository.save(new FollowEntity(requesterId, targetUserId));
    }

    public void unfollowUser(String requesterId, String targetUserId) {
        followRepository.deleteByFollowerIdAndFollowingId(requesterId, targetUserId);
    }

    public List<UserSummaryResponse> getFollowers(String userId) {
        return followRepository.findByFollowingId(userId).stream()
            .map(follow -> userRepository.findById(follow.followerId())
                    .orElseThrow(() -> new EntityNotFoundException("User " + follow.followerId())))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<UserResponse> getFollowing(String userId) {
        return followRepository.findByFollowerId(userId).stream()
            .map(follow -> userRepository.findById(follow.followingId())
                    .orElseThrow(() -> new EntityNotFoundException("User " + follow.followingId())))
            .map(UserService::toResponse)
            .collect(Collectors.toList());
    }

    private UserSummaryResponse toResponse(UserEntity user){
        return new UserSummaryResponse(
            user.id(),
            user.username(),
            user.avatarUrl()
        );
    }
}