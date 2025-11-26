package com.tech4um.projectWS.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/* Esta classe representa a entidade (tabela) 'users' no MySQL,
e implementa UserDetails para o Spring Security. */
@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    //  Papel do usuário (Para Autorização Baseada em Papéis)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER; // Padrão definido como USER

    //  Token de redefinição de senha (Para Esqueci Minha Senha)
    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    //  Data de expiração do token de redefinição
    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate;

    // --- Implementação UserDetails ATUALIZADA ---

    // Retorna o papel do usuário (ROLE_USER ou ROLE_ADMIN) para o Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Converte o Role (Enum) para o formato exigido pelo Spring Security (ex: "ROLE_USER")
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername(){
        // CRÍTICO: Se você usa o e-mail como login, o getUsername deve retornar o e-mail
        return email;
    }

    // Simplificações dos métodos UserDetails (Mantidas)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Enum para definir os papéis possíveis
    public enum Role {
        USER,
        ADMIN
    }
}