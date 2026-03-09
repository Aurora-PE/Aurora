package ro.unibuc.prodeng.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.BadCredentialsException;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.GroupEntity;
import ro.unibuc.prodeng.model.GroupMemberEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.FollowRepository;
import ro.unibuc.prodeng.repository.GroupInvitationRepository;
import ro.unibuc.prodeng.repository.GroupMemberRepository;
import ro.unibuc.prodeng.repository.GroupRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.UpdateUserRequest;
import ro.unibuc.prodeng.response.LoginResponse;
import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.response.UserSummaryResponse;
import ro.unibuc.prodeng.util.JwtUtil;
import ro.unibuc.prodeng.util.PasswordHasher;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

   private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository groupInvitationRepository;

    public UserService(UserRepository userRepository, 
                       FollowRepository followRepository,
                       GroupRepository groupRepository,
                       GroupMemberRepository groupMemberRepository,
                       GroupInvitationRepository groupInvitationRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupInvitationRepository = groupInvitationRepository;
    }

    public UserResponse getUserById(String requesterId, String targetId) {
        UserEntity targetUser = userRepository.findById(targetId)
            .orElseThrow(() -> new EntityNotFoundException("User " + targetId));

        if (targetId.equals(requesterId) || !targetUser.isPrivate()) {
            return toResponse(targetUser);
        }

        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(requesterId, targetId);
        if (!isFollowing) {
            throw new UnauthorizedException("This account is private. You must follow them to view their profile.");
        }

        return toResponse(targetUser);
    }

    public List<UserSummaryResponse> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username).stream()
            .map(user -> new UserSummaryResponse(user.id(), user.username(), user.avatarUrl()))
            .collect(Collectors.toList());
    }

    public UserResponse updateUser(String requesterId, UpdateUserRequest request) {
        UserEntity existingUser = userRepository.findById(requesterId)
            .orElseThrow(() -> new EntityNotFoundException("User " + requesterId));

        UserEntity updatedUser = new UserEntity(
            existingUser.id(),
            existingUser.username(),
            existingUser.email(),
            existingUser.passwordHash(),
            request.bio() != null ? request.bio() : existingUser.bio(),
            request.avatarUrl() != null ? request.avatarUrl() : existingUser.avatarUrl(),
            existingUser.createdAt(),
            request.isPrivate() != null ? request.isPrivate() : existingUser.isPrivate()
        );

        return toResponse(userRepository.save(updatedUser));
    }

    public void deleteUser(String requesterId) {
        if (!userRepository.existsById(requesterId)) {
            throw new EntityNotFoundException("User");
        }
        followRepository.deleteByFollowerId(requesterId);
        followRepository.deleteByFollowingId(requesterId);
        groupInvitationRepository.deleteByInviterId(requesterId);
        groupInvitationRepository.deleteByInviteeId(requesterId);
        List<GroupEntity> ownedGroups = groupRepository.findByCreatorId(requesterId);
        for (GroupEntity group : ownedGroups) {
            List<GroupMemberEntity> members = groupMemberRepository.findByGroupId(group.id());
            
            java.util.Optional<GroupMemberEntity> newOwnerOpt = members.stream()
                .filter(m -> !m.userId().equals(requesterId))
                .min(java.util.Comparator.comparing(GroupMemberEntity::role)); 

            if (newOwnerOpt.isPresent()) {
                GroupEntity updatedGroup = new GroupEntity(
                    group.id(), group.name(), group.description(), newOwnerOpt.get().userId(), group.createdAt()
                );
                groupRepository.save(updatedGroup);
            } else {
                groupInvitationRepository.deleteByGroupId(group.id());
                groupMemberRepository.deleteByGroupId(group.id());
                groupRepository.deleteById(group.id());
            }
        }
        groupMemberRepository.deleteByUserId(requesterId);

        userRepository.deleteById(requesterId);
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already taken.");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already registered.");
        }

        String hashedPassword = PasswordHasher.hashPassword(request.password());

        UserEntity user = new UserEntity(
            request.username(),
            request.email(),
            hashedPassword,
            request.bio(),
            request.avatarUrl(),
            request.isPrivate()
        );

        return toResponse(userRepository.save(user));
    }

    public LoginResponse login(String email, String rawPassword) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User"));

        String hashedInput = PasswordHasher.hashPassword(rawPassword);
        if (!hashedInput.equals(user.passwordHash())) {
            throw new BadCredentialsException("Invalid password");
        }

        String token = JwtUtil.generateToken(user.id());
        
        return new LoginResponse(token, toResponse(user));
    }

    public static UserResponse toResponse(UserEntity user) {
        return new UserResponse(
            user.id(), 
            user.username(), 
            user.email(),
            user.bio(), 
            user.avatarUrl(),
            user.isPrivate(), 
            user.createdAt()
        );
    }
}