package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.Forum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ForumRepository extends JpaRepository<Forum,Long> {

    //Método customizado para checar se o nome já existe
    Optional<Forum> findByName(String name);
}
