package com.tech4um.projectWS.service;

import com.tech4um.projectWS.exception.ConflictException;
import com.tech4um.projectWS.exception.ResourceNotFoundException;
import com.tech4um.projectWS.model.Forum;
import com.tech4um.projectWS.model.Message;
import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.ForumRepository;
import com.tech4um.projectWS.repository.MessageRepository;
import com.tech4um.projectWS.repository.UserRepository; // Importação assumida
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ForumService {

    //Logger para rastreamento
    private static final Logger logger = LoggerFactory.getLogger(ForumService.class);

    public final ForumRepository forumRepository;
    private final UserRepository userRepository; // Adicionado para buscar criadores/usuários
    private final MessageRepository messageRepository; // Adicionado para lidar com mensagens

    public ForumService(ForumRepository forumRepository,
                        UserRepository userRepository,
                        MessageRepository messageRepository) {
        this.forumRepository = forumRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    // =========================================================================
    // LÓGICA DO FÓRUM (Criação e Gerenciamento)
    // =========================================================================

    /*
     * Lógica: Criar novo Fórum
     * AGORA: Recebe o Fórum e o ID do usuário logado para definir o Criador/Participante.
     */
    @Transactional // Garante que todas as operações de DB sejam atômicas
    public Forum createForum(Forum forum, Long creatorId) {
        logger.info("Tentando criar novo fórum: {}", forum.getTitle()); // Alterado para Title

        // 1. Busca o usuário que está criando
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário criador não encontrado."));

        // 2. Regra de Negócio: Fóruns devem ter títulos únicos
        // NOTE: Mudei o método de findByName para findByTitle
        Optional<Forum> existingForum = forumRepository.findByTitle(forum.getTitle());
        if (existingForum.isPresent()){
            throw new ConflictException("Já existe um fórum com o título: " + forum.getTitle());
        }

        // 3. Define o criador e adiciona o criador como participante
        forum.setCreator(creator);
        forum.getParticipants().add(creator);

        // 4. Salva o fórum
        return forumRepository.save(forum);
    }

    // Lógica: Listar todos
    public List<Forum> findAllForums(){
        return forumRepository.findAll();
    }

    // Lógica: Encontrar por ID
    public Forum findById(Long id){
        return forumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fórum não encontrado com ID: " + id));
    }

    // Lógica: Excluir
    public void deleteForum(Long id){
        // Usamos findById que já lida com o ResourceNotFoundException
        Forum forum = findById(id);

        // Como a entidade Forum tem CascadeType.ALL para Messages, as mensagens serão excluídas também.
        forumRepository.delete(forum);
        logger.info("Fórum com ID {} excluído com sucesso.", id);
    }

    // =========================================================================
    // LÓGICA DE MENSAGENS E PARTICIPANTES
    // =========================================================================

    /*
     * Lógica: Envio de Mensagem
     * 1. Associa a mensagem ao Fórum e ao Usuário.
     * 2. Garante que o usuário que enviou a mensagem seja um participante.
     */
    @Transactional
    public Message saveMessage(Long forumId, Long userId, Message message) {
        // 1. Busca o Fórum
        Forum forum = findById(forumId);

        // 2. Busca o Usuário (Sender)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário emissor não encontrado."));

        // 3. Associa a mensagem
        message.setForum(forum);
        message.setUser(user);
        message.setSentAt(LocalDateTime.now()); // Garante o timestamp correto

        // 4. Adiciona o usuário como participante se ainda não estiver
        if (!forum.getParticipants().contains(user)) {
            forum.getParticipants().add(user);
            forumRepository.save(forum); // Atualiza a lista de participantes
            logger.info("Usuário {} adicionado como participante do fórum ID {}", userId, forumId);
        }

        // 5. Salva a mensagem
        return messageRepository.save(message);
    }

    /*
     * Lógica: Listar todas as mensagens de um Fórum
     */
    public List<Message> getMessagesByForum(Long forumId) {
        // A busca é feita diretamente pelo método do MessageRepository
        return messageRepository.findByForumIdOrderBySentAtAsc(forumId);
    }
}