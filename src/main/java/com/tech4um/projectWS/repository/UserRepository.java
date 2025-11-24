package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

//O tipo da chave primária (ID) de User é agora Long.
public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}