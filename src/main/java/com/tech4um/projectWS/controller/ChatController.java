package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.dto.ChatRequest;
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
    // 💡 Agora aceita o nosso DTO de requisição, não o Modelo Message
    public void sendMessage(@Payload ChatRequest request, Principal principal) {

        // O principal.getName() é o EMAIL do usuário autenticado (definido no Dia 2)
        String senderEmail = principal.getName();

        // 1. Mapeia DTO para o Modelo interno (Message)
        Message message = new Message();
        message.setSenderId(senderEmail); // Remetente é o usuário autenticado
        message.setForumId(request.getForumId());
        message.setContent(request.getContent());

        // 2. Determina o Tipo de Mensagem
        if (request.getRecipientEmail() != null && !request.getRecipientEmail().isBlank()) {
            message.setType(Message.MessageType.PRIVATE);
            message.setRecipientId(request.getRecipientEmail());
            logger.info("Mensagem PRIVADA de {} para {}", senderEmail, request.getRecipientEmail());
        } else {
            message.setType(Message.MessageType.PUBLIC);
            logger.info("Mensagem PÚBLICA no fórum {}", request.getForumId());
        }

        // 3. Persiste a mensagem no MongoDB
        Message savedMessage = messageService.save(message);

        // 4. Roteamento (Lógica já estabelecida no Dia 4/5)
        if (savedMessage.getType() == Message.MessageType.PUBLIC) {

            // Roteamento Público: /topic/forum.{forumId}
            String destination = "/topic/forum." + savedMessage.getForumId();
            messagingTemplate.convertAndSend(destination, savedMessage);

        } else if (savedMessage.getType() == Message.MessageType.PRIVATE) {

            // Roteamento Privado: /user/{recipientId}/private
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getRecipientId(),
                    "/private",
                    savedMessage
            );
        }
    }
}
