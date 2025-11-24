package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.model.Message;
import com.tech4um.projectWS.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    public ChatController(SimpMessagingTemplate messagingTemplate, MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    // Mapeia a mensagem de entrada do cliente: /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Message message, Principal principal) {

        // Garante que o remetente seja quem está autenticado
        message.setSenderId(principal.getName());

        // Salva a mensagem no MongoDB
        Message savedMessage = messageService.save(message);

        // --- Lógica de Roteamento ---
        if (savedMessage.getType() == Message.MessageType.PUBLIC) {

            // Roteamento Público: /topic/forum.{forumId}
            String destination = "/topic/forum." + savedMessage.getForumId();
            logger.info("Enviando PUBLIC para: {}", destination);

            messagingTemplate.convertAndSend(destination, savedMessage);

        } else if (savedMessage.getType() == Message.MessageType.PRIVATE) {

            // Roteamento Privado: /user/{recipientId}/private
            // O SimpMessagingTemplate adiciona automaticamente o prefixo /user/
            String destination = savedMessage.getRecipientId();
            logger.info("Enviando PRIVATE para o usuário: {}", destination);

            // O segundo argumento é o tópico personalizado (Ex: /private).
            // A rota final será: /user/{recipientId}/private
            messagingTemplate.convertAndSendToUser(
                    destination,
                    "/private",
                    savedMessage
            );
        }
    }
}
