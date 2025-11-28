package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.dto.chat.ChatRequest;
import com.tech4um.projectWS.exception.ResourceNotFoundException;
import com.tech4um.projectWS.model.Forum;
import com.tech4um.projectWS.model.Message;
import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.service.ForumService;
import com.tech4um.projectWS.service.MessageService;
import com.tech4um.projectWS.service.UserService;
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
    private final UserService userService;
    private final ForumService forumService;

    // ... Construtor (inalterado) ...
    public ChatController(SimpMessagingTemplate messagingTemplate,
                          MessageService messageService,
                          UserService userService,
                          ForumService forumService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.userService = userService;
        this.forumService = forumService;
    }


    // Mapeia a mensagem de entrada do cliente: /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest request, Principal principal) {

        // ** CRÍTICO **: Se o Principal for nulo, significa que seu StompJwtChannelInterceptor FALHOU
        // em bloquear a mensagem SEND não autenticada. No entanto, se o interceptor estiver correto,
        // o 'principal' NUNCA será nulo aqui.
        if (principal == null) {
            // Se esta linha for executada, a mensagem não deve ser processada.
            logger.error("Mensagem recebida sem Principal autenticado. Ignorando.");
            return;
        }

        String senderEmail = principal.getName();
        User recipient = null; // Variável para armazenar o destinatário (se for privado)

        // 1. Busca as ENTIDADES (Objetos JPA) necessárias
        User sender = userService.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Remetente não encontrado após autenticação."));

        Forum forum = forumService.getForumEntity(request.getForumId());

        // 2. Mapeia DTO para o Modelo interno (Message)
        Message message = new Message();
        message.setUser(sender);
        message.setForum(forum);
        message.setContent(request.getContent());

        // 3. Determina o Tipo de Mensagem e Busca o Destinatário (Otimizado)
        if (request.getRecipientEmail() != null && !request.getRecipientEmail().isBlank()) {
            message.setType(Message.MessageType.PRIVATE);

            // Busca o destinatário APENAS UMA VEZ
            recipient = userService.findByEmail(request.getRecipientEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Destinatário não encontrado."));

            // Define o ID do destinatário na mensagem
            message.setRecipientId(recipient.getId());

            logger.info("Mensagem PRIVADA de {} para {}", senderEmail, request.getRecipientEmail());
        } else {
            message.setType(Message.MessageType.PUBLIC);
            logger.info("Mensagem PÚBLICA no fórum {}", forum.getId());
        }

        // 4. Persiste a mensagem no MySQL (via JPA)
        Message savedMessage = messageService.save(message);

        // 5. Roteamento
        if (savedMessage.getType() == Message.MessageType.PUBLIC) {

            // Roteamento Público: /topic/forum.{forumId}
            String destination = "/topic/forum." + savedMessage.getForum().getId().toString();
            messagingTemplate.convertAndSend(destination, savedMessage);

        } else if (savedMessage.getType() == Message.MessageType.PRIVATE) {

            // Roteamento Privado:

            // 5a. Envia para o Remetente (Remetente se inscreve em /user/queue/private)
            messagingTemplate.convertAndSendToUser(
                    senderEmail,
                    "/private",
                    savedMessage
            );

            // 5b. Envia para o Destinatário (Destinatário se inscreve em /user/queue/private)
            // Reutilizamos o objeto 'recipient' obtido no passo 3.
            if (recipient != null) {
                messagingTemplate.convertAndSendToUser(
                        recipient.getEmail(), // Usa o e-mail do destinatário
                        "/private",
                        savedMessage
                );
            } else {
                // Embora improvável, esta exceção deve ser lançada se o recipient for nulo em uma mensagem PRIVATE.
                logger.error("Erro interno: Destinatário não encontrado para roteamento privado da mensagem ID: {}", savedMessage.getId());
            }
        }
    }
}