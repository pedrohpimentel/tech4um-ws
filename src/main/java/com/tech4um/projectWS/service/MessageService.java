package com.tech4um.projectWS.service;

import com.tech4um.projectWS.model.Message;
import com.tech4um.projectWS.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /*
     * Lógica para salvar a mensagem.
     * Requer que a entidade Message já tenha o objeto Forum setado.
     */
    public Message save(Message message) {
        // CORREÇÃO: Acessamos o ID pelo objeto Forum, não por um campo ID direto
        logger.debug("Salvando mensagem no fórum ID: {}", message.getForum().getId());
        return messageRepository.save(message);
    }

    /*
     * Busca o histórico de mensagens de um fórum.
     * CORREÇÃO: Usamos o método findByForumIdOrderBySentAtAsc, que corresponde
     * ao nome do método definido no seu MessageRepository.
     */
    public List<Message> findChatHistoryByForumId(Long forumId) {
        return messageRepository.findByForumIdOrderBySentAtAsc(forumId);
    }

}