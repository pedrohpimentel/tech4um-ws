package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message,String> {
    List<Message> findByForumIdOrderByTimestampAsc(String forumId);
}
