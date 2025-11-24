package com.tech4um.projectWS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // Importe esta anotação
import lombok.Data;

@Data
public class ChatRequest {

    // Usamos @NotNull para garantir que o ID não seja nulo, já que não é String.
    @NotNull(message = "O ID do fórum é obrigatório.")
    private Long forumId; // Mude o tipo para Long

    @NotBlank(message = "O conteúdo da mensagem é obrigatório.")
    private String content;

    // Opcional: E-mail do destinatário. Se presente, a mensagem é PRIVADA.
    private String recipientEmail;

}