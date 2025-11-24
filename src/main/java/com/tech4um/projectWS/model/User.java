package com.tech4um.projectWS.model;

/* Esta classe representa a entidade (tabela) 'users' no MySQL,
e implementa UserDetails para o Spring Security. */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@Data
@Entity // Marca a classe como uma tabela no banco de dados relacional (JPA)
@Table(name = "users")
public class User implements UserDetails { // Necessário para o Spring Security

    // CHAVE PRIMÁRIA JPA: Usamos Long e auto-geração
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento gerenciado pelo MySQL
    private Long id; // ID agora é Long

    // COLUNAS JPA: Define campos com restrições (ex: UNIQUE)

    // username deve ser único
    @Column(unique = true, nullable = false)
    private String username;

    // email deve ser único (muito importante para o login)
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Métodos UserDetails (Mantidos, pois são requisitos do Spring Security)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    // getUsername retorna o e-mail, conforme o padrão de autenticação
    @Override
    public String getUsername(){
        return email;
    }

    // Simplificações dos métodos UserDetails
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
}