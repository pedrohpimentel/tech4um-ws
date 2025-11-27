package com.tech4um.projectWS.dto.forum;

import com.tech4um.projectWS.dto.author.AuthorDTO;
import com.tech4um.projectWS.model.Forum;
import lombok.Builder;
import lombok.Data;

import java.time.ZoneId; // NECESSÁRIO para conversão de LocalDateTime
import java.util.Date;

@Data
@Builder
public class ForumResponse {

    private Long id;
    private String title;
    private String description;
    private Long createdAt; // Unix Timestamp (em milissegundos é o padrão)
    private AuthorDTO author;

    /*
     * Construtor estático (via builder) para mapear a Entidade Forum para o DTO de Resposta.
     *
     * @param forum A Entidade Forum salva no banco de dados.
     * @return Uma instância de ForumResponse.
     */
    public static ForumResponse fromEntity(Forum forum) {
        if (forum == null) {
            return null;
        }

        // --- CORREÇÃO DE ERRO NA LINHA 33 ---
        // Converte LocalDateTime (do Model) para Long (Unix Timestamp em milissegundos)
        Long timestamp = null;
        if (forum.getCreatedAt() != null) {
            // Conversão correta: LocalDateTime -> ZonedDateTime (com timezone) -> Instant -> Milissegundos
            timestamp = forum.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        }

        return ForumResponse.builder()
                .id(forum.getId())
                .title(forum.getTitle())
                .description(forum.getDescription())
                .createdAt(timestamp)
                // A linha 36 está correta, pois getCreator() existe no seu Model
                .author(new AuthorDTO(forum.getCreator()))
                .build();
    }
}