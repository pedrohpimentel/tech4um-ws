package com.tech4um.projectWS.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Requisição para a rota /api/auth/reset-password
@Data
public class ResetPasswordRequest {
    @NotBlank(message = "O token de redefinição é obrigatório.")
    private String token; // O token único enviado por e-mail

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
    private String newPassword;
}
