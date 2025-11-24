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

@Data
@Entity // Marca a classe como uma tabela no banco de dados relacional
@Table(name = "messages") // Nome da tabela no MySQL
public class Message {

    // CHAVE PRIMÁRIA JPA: Usamos Long e auto-geração
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento gerenciado pelo MySQL
    private Long id; // O tipo muda de String (MongoDB) para Long (JPA)

    // Chaves estrangeiras (IDs de outras entidades)
    // Usamos Long para corresponder ao novo tipo Long do ID de Forum/User
    // Se for String, mantenha String. Assumindo que Forum.id agora é Long.
    private Long forumId;

    private Long senderId;

    // Conteúdo (pode precisar de @Lob se for muito longo, mas String é suficiente por padrão)
    @Column(length = 2000) // Exemplo: define um limite razoável para a coluna
    private String content;

    // ENUM JPA: Garante que o ENUM seja persistido como String no MySQL
    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.PUBLIC;

    private Long recipientId; // Usado apenas se TYPE for PRIVATE!

    private Long timestamp = System.currentTimeMillis();

    public enum MessageType {
        PUBLIC,
        PRIVATE
    }
}