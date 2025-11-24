package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.Forum;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ForumRepository extends MongoRepository<Forum,String> {

    //Método customizado para checar se o nome já existe
    Optional<Forum> findByName(String name);
}
