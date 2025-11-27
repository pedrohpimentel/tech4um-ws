package com.tech4um.projectWS.dto.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Requisição para a rota /api/auth/forgot-password
@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail deve ser válido.")
    private String email;
}
