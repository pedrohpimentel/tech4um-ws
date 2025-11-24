package com.tech4um.projectWS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForumRequest {

    @NotBlank(message = "O nome do fórum é obrigatório.")
    private String name;

    private String description;
}
