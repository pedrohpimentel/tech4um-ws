package com.tech4um.projectWS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForumRequest {

    // O título é a parte principal do fórum e deve ser obrigatório.
    @NotBlank(message = "O título do fórum é obrigatório.")
    private String title;

    // IMPORTANTE: O campo 'name' foi removido deste DTO de requisição (Request DTO)
    // porque:
    // 1. Ele causava erro de validação (MethodArgumentNotValidException) quando o cliente não o enviava.
    // 2. O ForumService já preenche a entidade Forum.name com o valor de ForumRequest.title.
    // Assim, a responsabilidade de preencher 'name' fica no Service, simplificando a requisição do cliente.

    private String description;
}