package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.Forum;
import org.springframework.data.mongodb.repository.MongoRepository;

//Iremos tratar da implementação dos métodos, quando criamos as classes do service.
public class ForumRepository extends MongoRepository<Forum,String> {
}
