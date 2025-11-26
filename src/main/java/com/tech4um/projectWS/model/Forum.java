package com.tech4um.projectWS.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity // Marca a classe como uma tabela no banco de dados relacional
@Table(name = "forums") // Nome da tabela no MySQL
public class Forum {

    // CHAVE PRIMÁRIA JPA: Usamos Long e auto-geração para o MySQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento gerenciado pelo MySQL
    private Long id; // O tipo muda de String (MongoDB) para Long (JPA)

    // COLUNAS JPA: Define a restrição de unicidade

    // name deve ser único (replicando @Indexed(unique = true))
    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    // Coluna para armazenar o timestamp (Long)
    // O JPA/Hibernate cuidará de mapear isso corretamente para o tipo numérico no MySQL
    private Long createdAt = System.currentTimeMillis();

}