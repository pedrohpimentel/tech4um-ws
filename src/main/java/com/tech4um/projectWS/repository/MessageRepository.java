package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /*
     * Busca todas as mensagens de um Fórum, ordenadas pela data de envio crescente.
     * Assume que a entidade Message possui o campo 'sentAt'.
     * @param forumId O ID do Fórum.
     * @return Uma lista de Mensagens.
     */
    List<Message> findByForumIdOrderBySentAtAsc(Long forumId);
}