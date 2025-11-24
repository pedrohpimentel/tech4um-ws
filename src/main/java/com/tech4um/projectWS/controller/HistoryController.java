package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.model.Message;
import com.tech4um.projectWS.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final MessageService messageService;

    public HistoryController(MessageService messageService) {
        this.messageService = messageService;
    }

    // GET /api/history/forum/{forumId}
    // Retorna todas as mensagens públicas de um fórum específico
    @GetMapping("/forum/{forumId}")
    public ResponseEntity<List<Message>> getForumHistory(@PathVariable String forumId) {
        List<Message> messages = messageService.findChatHistoryByForumId(forumId);

        // Retorna a lista de mensagens (já ordenada por timestamp no service)
        return ResponseEntity.ok(messages);
    }
}
