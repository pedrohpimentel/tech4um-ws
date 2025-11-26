package com.tech4um.projectWS.dto;

import com.tech4um.projectWS.model.User.Role; // Importação da Role do User

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    // NOVO CAMPO: Papel do usuário. Define USER como padrão se não for enviado.
    private Role role = Role.USER;
}