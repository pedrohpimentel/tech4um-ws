package com.tech4um.projectWS.dto.author;

import com.tech4um.projectWS.model.User; // Importação essencial da Entidade User

/*
 * DTO para representar o autor de um fórum ou mensagem na resposta JSON.
 * Contém apenas ID e Username, protegendo dados sensíveis do User.
 */
public class AuthorDTO {

    private Long id;
    private String username;

    // Construtor vazio (default) é necessário para a desserialização JSON em alguns frameworks
    public AuthorDTO() {
    }

    /*
     * Construtor a partir da Entidade User.
     * Implementa a lógica para extrair o nome de exibição (display name)
     * a partir do email do usuário (username) para exibição no front-end.
     * * @param user A entidade User a ser mapeada.
     */
    public AuthorDTO(User user) {
        if (user != null) {
            this.id = user.getId();

            // LÓGICA DE EXTRAÇÃO DO NOME DE EXIBIÇÃO:
            // O username do banco é o email (ex: "testador.front@tech4um.com").
            String email = user.getUsername();
            String displayName;

            // Encontra o índice do '@'
            int atIndex = email.indexOf('@');

            if (atIndex > 0) {
                // Se houver um '@', pega a parte local (antes do '@')
                displayName = email.substring(0, atIndex);
            } else {
                // Caso contrário (formato inesperado), usa o username completo
                displayName = email;
            }

            this.username = displayName;
        }
    }

    // Getters e Setters (necessários para a serialização JSON e mapeamento)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}