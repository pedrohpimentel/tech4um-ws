package com.tech4um.projectWS.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data // O Lombok Data é aceitável aqui, pois o relacionamento é simples
@Entity // Marca a classe como uma tabela no banco de dados relacional
@Table(name = "messages") // Nome da tabela no MySQL
public class Message {

    // CHAVE PRIMÁRIA JPA: Usamos Long e auto-geração
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relação N:1 com o Fórum (a qual fórum a mensagem pertence)
    // Substitui 'private Long forumId;'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forum_id", nullable = false)
    private Forum forum;

    // Relação N:1 com o Usuário (quem enviou a mensagem)
    // Substitui 'private Long senderId;'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Conteúdo da mensagem
    @Column(length = 2000, nullable = false)
    private String content;

    // ENUM JPA: Garante que o ENUM seja persistido como String no MySQL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.PUBLIC;

    // Campo de ID do destinatário (apenas usado se TYPE for PRIVATE)
    private Long recipientId;

    // Uso de LocalDateTime para timestamps (Melhor prática para JPA)
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public enum MessageType {
        PUBLIC,
        PRIVATE
    }

    // Nota: O Lombok (@Data) gerará automaticamente os construtores, getters e setters.
}