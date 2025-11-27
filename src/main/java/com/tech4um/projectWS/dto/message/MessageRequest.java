package com.tech4um.projectWS.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * DTO de Requisição para o envio de uma nova mensagem em um fórum.
 * O ID do fórum e do usuário são injetados pelo Controller/Service e não vêm no corpo.
 */
@Data
public class MessageRequest {

    @NotBlank(message = "O conteúdo da mensagem é obrigatório.")
    private String content;

    // Outros campos como 'type' poderiam ser adicionados se o Front-end controlasse isso
}