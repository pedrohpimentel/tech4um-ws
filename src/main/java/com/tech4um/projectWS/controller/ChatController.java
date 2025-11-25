package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.dto.ChatRequest;
import com.tech4um.projectWS.model.Message;
import com.tech4um.projectWS.service.MessageService;
import com.tech4um.projectWS.service.UserService; // 💡 NOVO: Importar UserService
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
    private final UserService userService; // 💡 NOVO: Injetar UserService

    public ChatController(SimpMessagingTemplate messagingTemplate,
                          MessageService messageService,
                          UserService userService) { // 💡 Adicionar UserService ao construtor
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.userService = userService;
    }

    // Mapeia a mensagem de entrada do cliente: /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest request, Principal principal) {

        String senderEmail = principal.getName();

        // Buscar o ID do usuário (Long) a partir do email
        // Este método DEVE ser implementado no UserService para retornar o Long id.
        Long senderId = userService.getUserIdByEmail(senderEmail);

        // Mapeia DTO para o Modelo interno (Message)
        Message message = new Message();
        message.setSenderId(senderId);
        message.setForumId(request.getForumId());
        message.setContent(request.getContent());

        // Determina o Tipo de Mensagem
        if (request.getRecipientEmail() != null && !request.getRecipientEmail().isBlank()) {
            message.setType(Message.MessageType.PRIVATE);

            // Buscar o ID (Long) do destinatário também, se for privado
            Long recipientId = userService.getUserIdByEmail(request.getRecipientEmail());
            message.setRecipientId(recipientId);

            logger.info("Mensagem PRIVADA de {} para {}", senderEmail, request.getRecipientEmail());
        } else {
            message.setType(Message.MessageType.PUBLIC);
            logger.info("Mensagem PÚBLICA no fórum {}", request.getForumId());
        }

        // Persiste a mensagem no MySQL (via JPA)
        Message savedMessage = messageService.save(message);

        // Roteamento (A lógica de roteamento usa IDs de String no front-end,
        // mas o back-end está usando Long. Cuidado com a conversão no front-end.)
        if (savedMessage.getType() == Message.MessageType.PUBLIC) {

            // Roteamento Público: /topic/forum.{forumId}
            // Nota: getForumId() é Long, mas a string de destino STOMP precisa de String
            String destination = "/topic/forum." + savedMessage.getForumId().toString();
            messagingTemplate.convertAndSend(destination, savedMessage);

        } else if (savedMessage.getType() == Message.MessageType.PRIVATE) {

            // Roteamento Privado: O método convertAndSendToUser requer uma String como usuário.
            // O Spring Security espera o EMAIL do usuário aqui, não o ID Long.
            messagingTemplate.convertAndSendToUser(
                    senderEmail, // Usamos o e-mail do remetente (Principal) para o roteamento STOMP
                    "/private",
                    savedMessage
            );
        }
    }
}