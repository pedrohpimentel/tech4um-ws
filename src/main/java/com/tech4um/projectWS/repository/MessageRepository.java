package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

//Iremos tratar da implementação dos métodos, quando criamos as classes do service.
public class MessageRepository extends MongoRepository<Message,String> {
    List<Message> findByForumIdOrderByTimestampAsc(String forumId);
}
