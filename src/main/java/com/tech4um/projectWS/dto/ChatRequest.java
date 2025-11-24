package com.tech4um.projectWS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "O ID do fórum é obrigatório.")
    private String forumId;

    @NotBlank(message = "O conteúdo da mensagem é obrigatório.")
    private String content;

    // Opcional: E-mail do destinatário. Se presente, a mensagem é PRIVADA.
    private String recipientEmail;

}
