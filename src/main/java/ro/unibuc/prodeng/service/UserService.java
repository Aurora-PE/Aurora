package ro.unibuc.prodeng.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.BadCredentialsException;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.UpdateUserRequest;
import ro.unibuc.prodeng.response.LoginResponse;
import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.util.JwtUtil;
import ro.unibuc.prodeng.util.PasswordHasher;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserById(String id) {
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User " + id));
        return toResponse(user);
    }

    public List<UserResponse> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
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
            request.avatarUrl()
        );

        return toResponse(userRepository.save(user));
    }

    public UserResponse updateUser(String requesterId, String idToUpdate, UpdateUserRequest request) {
        if (!requesterId.equals(idToUpdate)) {
            throw new UnauthorizedException("You can only update your own profile.");
        }

        UserEntity existingUser = userRepository.findById(idToUpdate)
            .orElseThrow(() -> new EntityNotFoundException("User " + idToUpdate));

        UserEntity updatedUser = new UserEntity(
            existingUser.id(),
            existingUser.username(),
            existingUser.email(),
            existingUser.passwordHash(),
            request.bio() != null ? request.bio() : existingUser.bio(),
            request.avatarUrl() != null ? request.avatarUrl() : existingUser.avatarUrl(),
            existingUser.createdAt()
        );

        return toResponse(userRepository.save(updatedUser));
    }

    public void deleteUser(String requesterId, String idToDelete) {
        if (!requesterId.equals(idToDelete)) {
            throw new UnauthorizedException("You can only delete your own profile.");
        }

        if (!userRepository.existsById(idToDelete)) {
            throw new EntityNotFoundException("User");
        }
        userRepository.deleteById(idToDelete);
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

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(
            user.id(), 
            user.username(), 
            user.email(),
            user.bio(), 
            user.avatarUrl(), 
            user.createdAt()
        );
    }
}