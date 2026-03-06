package ro.unibuc.prodeng.repository;

import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.UserEntity;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    List<UserEntity> findByUsernameContainingIgnoreCase(String username);
}
