package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    public Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}
