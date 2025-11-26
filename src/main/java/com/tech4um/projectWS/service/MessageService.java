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

    public Message save(Message message) {
        logger.debug("Salvando mensagem no fórum ID: {}", message.getForumId());
        return messageRepository.save(message);
    }

    //Busca o histórico de mensagens de um fórum
    // Mudar o tipo do parâmetro de String para Long
    public List<Message> findChatHistoryByForumId(Long forumId) {
        return messageRepository.findByForumIdOrderByTimestampAsc(forumId);
    }

}