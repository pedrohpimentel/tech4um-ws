package com.tech4um.projectWS.service;

import com.tech4um.projectWS.dto.forum.ForumRequest;
import com.tech4um.projectWS.dto.forum.ForumResponse;
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
     * REMOVIDO: O método toForumResponse foi removido,
     * pois o ForumResponse.fromEntity(Forum) já faz o mapeamento completo (incluindo Autor).
     */

    /**
     * Busca a entidade Forum por ID.
     * Método auxiliar privado para uso interno do Service.
     */
    private Forum getForumEntityById(Long id) {
        return forumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fórum não encontrado com ID: " + id));
    }

    /**
     * Retorna a entidade Forum, usado para lógica de back-end (como no ChatController)
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
     * @param request O DTO ForumRequest com os dados do novo fórum.
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

        // Popula 'title' e 'name'
        newForum.setTitle(request.getTitle());
        newForum.setName(request.getTitle()); // 'name' é preenchido com o valor de 'title'
        newForum.setDescription(request.getDescription());
        newForum.setCreator(creator);
        newForum.setCreatedAt(LocalDateTime.now());

        // 4. Adiciona o criador como participante
        newForum.getParticipants().add(creator);

        // 5. Salva o fórum
        Forum savedForum = forumRepository.save(newForum);

        // 6. Converte usando o método estático que inclui o Autor (CORREÇÃO FINAL)
        return ForumResponse.fromEntity(savedForum);
    }

    /*
     * Lógica: Listar todos os fóruns, retornando uma lista de DTOs.
     */
    public List<ForumResponse> findAllForums(){
        return forumRepository.findAll().stream()
                // Usa o método estático para mapear, que inclui a conversão de data e Autor
                .map(ForumResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /*
     * Lógica: Encontrar por ID, retornando o DTO de resposta.
     */
    public ForumResponse findById(Long id){
        Forum forum = getForumEntityById(id);
        // Usa o método estático para mapear
        return ForumResponse.fromEntity(forum);
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
     * Lógica: Envio de Mensagem
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