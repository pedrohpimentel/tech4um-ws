package com.tech4um.projectWS.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "forums")
public class Forum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Coluna para o Título (substitui 'name')
    @Column(unique = true, nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    // --- NOVOS CAMPOS PARA ATENDER AO REQUISITO ---

    // 1. Criador (Creator): Relação N:1 com a entidade User
    // O criador é um único User.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // 2. Participantes (Participants): Relação N:N (Muitos para Muitos) com User
    @ManyToMany
    @JoinTable(
            name = "forum_participants",
            joinColumns = @JoinColumn(name = "forum_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    // Relação 1:N com as mensagens (opicional, mas bom para exclusão em cascata)
    @OneToMany(mappedBy = "forum", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Message> messages;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    // Construtor padrão
    public Forum() {}

    // Getters e Setters (Necessários após remover @Data do Lombok)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public Set<User> getParticipants() { return participants; }
    public void addParticipant(User user) { this.participants.add(user); }
    public void setParticipants(Set<User> participants) { this.participants = participants; }

    public Set<Message> getMessages() { return messages; }
    public void setMessages(Set<Message> messages) { this.messages = messages; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}