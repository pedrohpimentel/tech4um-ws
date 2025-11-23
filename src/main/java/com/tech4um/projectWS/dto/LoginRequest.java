package com.tech4um.projectWS.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Email(message = "O formato do e-mail é inválido.")
    @NotBlank (message = "o e-mail é obrigatório.")
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    private String password;
}
