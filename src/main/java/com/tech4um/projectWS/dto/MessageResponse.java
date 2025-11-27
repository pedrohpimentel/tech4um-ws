package com.tech4um.projectWS.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/*
 * DTO de Resposta para exibir uma mensagem, incluindo metadados do autor.
 */
@Data
@Builder
public class MessageResponse {
    private Long id;
    private String content;
    private LocalDateTime sentAt;

    // Informações básicas do autor, essenciais para o Front-end
    private Long userId;
    private String username;

    private Long forumId;
}