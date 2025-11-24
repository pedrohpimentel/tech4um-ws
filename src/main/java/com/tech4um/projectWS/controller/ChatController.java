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

    private final SimpMessagingTemplate  messagingTemplate;
    private final MessageService messageService;

    // SimpMessagingTemplate é a ferramenta que usamos para enviar mensagens
    // de volta para o broker ou para usuários específicos.
    public ChatController(SimpMessagingTemplate messagingTemplate, MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
}
    // Mapeia a mensagem de entrada do cliente: /app/chat.send
    @MessageMapping("/chat.send")
    // @Payload é o corpo da mensagem (o objeto Message que o cliente envia)
    public void sendMessage(@Payload Message message, Principal principal) {

        // 1. Loga a mensagem recebida
        logger.info("Mensagem recebida de {}: {}", principal.getName(), message.getContent());

        // 2. Define o ID do remetente (garantindo que o remetente seja quem está logado)
        message.setSenderId(principal.getName());

        // 3. Persiste a mensagem no MongoDB
        Message savedMessage = messageService.save(message);

        // 4. Roteamento (Apenas para mensagens públicas neste dia)
        if (savedMessage.getType() == Message.MessageType.PUBLIC) {

            String destination = "/topic/forum." + savedMessage.getForumId();
            logger.info("Enviando mensagem pública para o tópico: {}", destination);

            // Envia a mensagem salva (com ID e Timestamp) para todos que estão inscritos no tópico do fórum
            messagingTemplate.convertAndSend(destination, savedMessage);
        }
    }
}
