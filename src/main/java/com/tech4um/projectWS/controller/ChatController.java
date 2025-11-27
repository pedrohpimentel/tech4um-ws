package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.dto.ChatRequest;
import com.tech4um.projectWS.exception.ResourceNotFoundException;
import com.tech4um.projectWS.model.Forum;
import com.tech4um.projectWS.model.Message;
import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.service.ForumService; // NOVO: Adicionado para buscar a entidade Forum
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
    private final ForumService forumService; //  NOVO: Injetar ForumService

    public ChatController(SimpMessagingTemplate messagingTemplate,
                          MessageService messageService,
                          UserService userService,
                          ForumService forumService) { // 💡 Adicionar ForumService
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.userService = userService;
        this.forumService = forumService; // Inicializar
    }

    // Mapeia a mensagem de entrada do cliente: /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest request, Principal principal) {

        String senderEmail = principal.getName();

        // 1. Busca as ENTIDADES (Objetos JPA) necessárias
        User sender = userService.findByEmail(senderEmail) // Assumindo que findByEmail retorna Optional<User> ou User
                .orElseThrow(() -> new ResourceNotFoundException("Remetente não encontrado."));

        Forum forum = forumService.findById(request.getForumId());


        // 2. Mapeia DTO para o Modelo interno (Message)
        Message message = new Message();
        message.setUser(sender);      // 🟢 CORRIGIDO: Usa setUser(User)
        message.setForum(forum);      // 🟢 CORRIGIDO: Usa setForum(Forum)
        message.setContent(request.getContent());

        // 3. Determina o Tipo de Mensagem
        if (request.getRecipientEmail() != null && !request.getRecipientEmail().isBlank()) {
            message.setType(Message.MessageType.PRIVATE);

            // Buscar o ID (Long) do destinatário também, se for privado
            User recipient = userService.findByEmail(request.getRecipientEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Destinatário não encontrado."));

            message.setRecipientId(recipient.getId()); // Usamos o ID aqui, pois é um campo não-JPA simples.

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
            // 🟢 CORRIGIDO: Acessa o ID pelo objeto Forum, resolvendo o erro de compilação
            String destination = "/topic/forum." + savedMessage.getForum().getId().toString();
            messagingTemplate.convertAndSend(destination, savedMessage);

        } else if (savedMessage.getType() == Message.MessageType.PRIVATE) {

            // Roteamento Privado para o remetente
            messagingTemplate.convertAndSendToUser(
                    senderEmail, // Roteamento STOMP usa o EMAIL do remetente
                    "/private",
                    savedMessage
            );

            // E também para o destinatário (usamos o e-mail para o roteamento STOMP)
            User recipient = userService.findById(savedMessage.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Erro de destinatário."));

            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail(),
                    "/private",
                    savedMessage
            );
        }
    }
}