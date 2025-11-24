package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// O tipo da Chave Primária deve ser Long (o novo tipo do Message.id)
public interface MessageRepository extends JpaRepository<Message, Long> {

    // O tipo do parâmetro forumId deve ser Long para corresponder ao novo Message.forumId
    List<Message> findByForumIdOrderByTimestampAsc(Long forumId);
}