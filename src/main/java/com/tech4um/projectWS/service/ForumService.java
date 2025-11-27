package com.tech4um.projectWS.service;

import com.tech4um.projectWS.dto.ForumRequest;
import com.tech4um.projectWS.dto.ForumResponse;
import com.tech4um.projectWS.exception.ConflictException;
import com.tech4um.projectWS.exception.ResourceNotFoundException;
import com.tech4um.projectWS.model.Forum;
import com.tech4um.projectWS.model.Message;
import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.ForumRepository;
import com.tech4um.projectWS.repository.MessageRepository;
import com.tech4um.projectWS.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ForumService {

    private static final Logger logger = LoggerFactory.getLogger(ForumService.class);

    public final ForumRepository forumRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public ForumService(ForumRepository forumRepository,
                        UserRepository userRepository,
                        MessageRepository messageRepository) {
        this.forumRepository = forumRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    // =========================================================================
    // MÉTODOS DE CONVERSÃO E BUSCA DE ENTIDADES (Auxiliar)
    // =========================================================================

    /*
     * Converte a Entidade Forum para o DTO de Resposta ForumResponse.
     * @param forum A entidade JPA Forum.
     * @return O DTO ForumResponse.
     */
    private ForumResponse toForumResponse(Forum forum) {
        return ForumResponse.builder()
                .id(forum.getId())
                .title(forum.getTitle())
                .name(forum.getName())
                .description(forum.getDescription())
                // Converte LocalDateTime para Unix Timestamp (Long)
                .createdAt(forum.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                .build();
    }

    /**
     * Busca a entidade Forum por ID.
     * Método auxiliar privado para uso interno do Service.
     */
    private Forum getForumEntityById(Long id) {
        return forumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fórum não encontrado com ID: " + id));
    }

    /**
     * NOVO: Retorna a entidade Forum, usado para lógica de back-end (como no ChatController)
     * que precisa da entidade JPA completa para associação.
     * @param id O ID do fórum.
     * @return A entidade Forum.
     */
    public Forum getForumEntity(Long id) {
        return getForumEntityById(id);
    }


    // =========================================================================
    // LÓGICA DO FÓRUM (Criação e Gerenciamento)
    // =========================================================================

    /*
     * Lógica: Criar novo Fórum a partir do DTO de requisição.
     * CORREÇÃO: Garante que 'title' e 'name' sejam populados na entidade antes de salvar,
     * resolvendo o erro de NOT NULL.
     * * @param request O DTO ForumRequest com os dados do novo fórum.
     * @param creatorId O ID do usuário logado.
     * @return O DTO ForumResponse do fórum criado.
     */
    @Transactional
    public ForumResponse createForum(ForumRequest request, Long creatorId) {
        logger.info("Tentando criar novo fórum: {}", request.getTitle());

        // 1. Busca o usuário que está criando
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário criador não encontrado."));

        // 2. Regra de Negócio: Fóruns devem ter títulos únicos
        Optional<Forum> existingForum = forumRepository.findByTitle(request.getTitle());
        if (existingForum.isPresent()){
            throw new ConflictException("Já existe um fórum com o título: " + request.getTitle());
        }

        // 3. Converte DTO para Entidade Forum
        Forum newForum = new Forum();

        // CORREÇÃO CRÍTICA: Popula 'title' e 'name'
        newForum.setTitle(request.getTitle());
        newForum.setName(request.getTitle()); // Assumindo que 'name' e 'title' são o mesmo valor para este cenário

        newForum.setDescription(request.getDescription());
        newForum.setCreator(creator);
        newForum.setCreatedAt(LocalDateTime.now());

        // 4. Adiciona o criador como participante
        newForum.getParticipants().add(creator);

        // 5. Salva o fórum (O Hibernate agora terá os valores para 'title' e 'name')
        Forum savedForum = forumRepository.save(newForum);

        // 6. Converte e retorna o DTO de resposta
        return toForumResponse(savedForum);
    }

    /*
     * Lógica: Listar todos os fóruns, retornando uma lista de DTOs.
     */
    public List<ForumResponse> findAllForums(){
        return forumRepository.findAll().stream()
                .map(this::toForumResponse)
                .collect(Collectors.toList());
    }

    /*
     * Lógica: Encontrar por ID, retornando o DTO de resposta.
     */
    public ForumResponse findById(Long id){
        Forum forum = getForumEntityById(id);
        return toForumResponse(forum);
    }

    // Lógica: Excluir
    public void deleteForum(Long id){
        Forum forum = getForumEntityById(id);
        forumRepository.delete(forum);
        logger.info("Fórum com ID {} excluído com sucesso.", id);
    }

    // =========================================================================
    // LÓGICA DE MENSAGENS E PARTICIPANTES
    // =========================================================================

    /*
     * Lógica: Envio de Mensagem (mantida)
     */
    @Transactional
    public Message saveMessage(Long forumId, Long userId, Message message) {
        // 1. Busca o Fórum
        Forum forum = getForumEntityById(forumId);

        // 2. Busca o Usuário (Sender)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário emissor não encontrado."));

        // 3. Associa a mensagem
        message.setForum(forum);
        message.setUser(user);
        message.setSentAt(LocalDateTime.now());

        // 4. Adiciona o usuário como participante se ainda não estiver
        if (!forum.getParticipants().contains(user)) {
            forum.getParticipants().add(user);
            forumRepository.save(forum);
            logger.info("Usuário {} adicionado como participante do fórum ID {}", userId, forumId);
        }

        // 5. Salva a mensagem
        return messageRepository.save(message);
    }

    /*
     * Lógica: Listar todas as mensagens de um Fórum (mantida)
     */
    public List<Message> getMessagesByForum(Long forumId) {
        // A busca é feita diretamente pelo método do MessageRepository
        return messageRepository.findByForumIdOrderBySentAtAsc(forumId);
    }
}