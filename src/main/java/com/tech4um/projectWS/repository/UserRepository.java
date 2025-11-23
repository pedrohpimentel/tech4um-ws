package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

//Iremos tratar da implementação dos métodos, quando criamos as classes do service.
public class UserRepository extends MongoRepository<User, String> {
    public Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

}
