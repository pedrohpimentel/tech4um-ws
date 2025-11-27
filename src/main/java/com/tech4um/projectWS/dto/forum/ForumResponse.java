package com.tech4um.projectWS.dto.forum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForumResponse {

    private Long id;
    private String title; // Adicionei 'title' para refletir a Entidade, o original tinha 'name'
    private String name; // Mantido 'name' para garantir que os dados retornados sejam completos
    private String description;
    private Long createdAt; // Unix Timestamp
}