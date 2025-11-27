package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.dto.ForumRequest;
import com.tech4um.projectWS.dto.ForumResponse;
import com.tech4um.projectWS.dto.MessageRequest;
import com.tech4um.projectWS.dto.MessageResponse;
import com.tech4um.projectWS.exception.ResourceNotFoundException;
import com.tech4um.projectWS.model.Forum;
import com.tech4um.projectWS.model.Message;
import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.UserRepository;
import com.tech4um.projectWS.service.ForumService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forums")
public class ForumController {

    private final ForumService forumService;
    private final UserRepository userRepository; // Adicionado para obter o usuário logado

    public ForumController(ForumService forumService, UserRepository userRepository){
        this.forumService = forumService;
        this.userRepository = userRepository;
    }

    // =========================================================================
    // CRUD DE FÓRUM (Contêiner)
    // =========================================================================

    /*
     * POST /api/forums
     * Cria um novo fórum, definindo o usuário logado como CRIADOR e PARTICIPANTE.
     * Requer Autenticação (JWT).
     */
    @PostMapping
    public ResponseEntity<ForumResponse> createForum(@Valid @RequestBody ForumRequest request,
                                                     @AuthenticationPrincipal UserDetails currentUser) {

        // 1. Encontra o ID do usuário logado via token JWT
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado."));

        // 2. Mapeia DTO para o Modelo (A entidade Forum agora espera 'title')
        Forum forum = new Forum();
        // Assumindo que ForumRequest.name foi alterado para ForumRequest.title
        forum.setTitle(request.getName());
        forum.setDescription(request.getDescription());

        // 3. O Service lida com a atribuição de Criador e Participantes
        Forum createdForum = forumService.createForum(forum, user.getId());

        // 4. Mapeia o modelo para o DTO de resposta (usando o novo campo 'title')
        ForumResponse response = ForumResponse.builder()
                .id(createdForum.getId())
                .name(createdForum.getTitle()) // Usando getTitle()
                .description(createdForum.getDescription())
                .createdAt(createdForum.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC)) // Convertendo de volta para Long (se necessário)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /*
     * GET /api/forums
     * Lista todos os fóruns (PÚBLICO).
     */
    @GetMapping
    public ResponseEntity<List<ForumResponse>> getAllForums(){
        List<Forum> forums = forumService.findAllForums();

        // Mapeia a lista de Modelos para uma lista de DTOs
        List<ForumResponse> responses = forums.stream()
                .map(forum -> ForumResponse.builder()
                        .id(forum.getId())
                        .name(forum.getTitle()) // Usando getTitle()
                        .description(forum.getDescription())
                        .createdAt(forum.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC))
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /*
     * DELETE /api/forums/{id}
     * Exclui um fórum. Apenas Criador ou ADMIN pode executar.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForum(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails currentUser){

        Forum forumToDelete = forumService.findById(id);

        // Verifica o usuário logado
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado."));

        // 1. Checa se o usuário logado é o CRIADOR
        boolean isCreator = forumToDelete.getCreator().getId().equals(user.getId());

        // 2. Checa se o usuário é ADMIN (se sua classe UserDetails prover essa informação)
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isCreator && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        }

        forumService.deleteForum(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // MENSAGENS (Messages)
    // =========================================================================

    /*
     * POST /api/forums/{forumId}/messages
     * Envia uma nova mensagem no fórum. O usuário logado é o emissor e vira participante.
     * Requer Autenticação (JWT).
     */
    @PostMapping("/{forumId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable Long forumId,
                                                       @Valid @RequestBody MessageRequest request,
                                                       @AuthenticationPrincipal UserDetails currentUser) {

        // 1. Encontra o ID do usuário logado
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado."));

        // 2. Mapeia DTO para o Modelo da Mensagem
        Message message = new Message();
        message.setContent(request.getContent());

        // 3. O Service salva, associa, e gerencia a participação
        Message savedMessage = forumService.saveMessage(forumId, user.getId(), message);

        // 4. Mapeia Modelo para DTO de Resposta
        MessageResponse response = MessageResponse.builder()
                .id(savedMessage.getId())
                .content(savedMessage.getContent())
                .sentAt(savedMessage.getSentAt())
                .userId(user.getId())
                .username(user.getUsername())
                .forumId(forumId)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /*
     * GET /api/forums/{forumId}/messages
     * Lista todas as mensagens de um fórum (PÚBLICO).
     */
    @GetMapping("/{forumId}/messages")
    public ResponseEntity<List<MessageResponse>> getForumMessages(@PathVariable Long forumId) {

        // Garante que o fórum existe antes de buscar as mensagens
        forumService.findById(forumId);

        List<Message> messages = forumService.getMessagesByForum(forumId);

        // Mapeia a lista de Modelos para uma lista de DTOs de Resposta
        List<MessageResponse> responses = messages.stream()
                .map(message -> MessageResponse.builder()
                        .id(message.getId())
                        .content(message.getContent())
                        .sentAt(message.getSentAt())
                        .userId(message.getUser().getId())
                        .username(message.getUser().getUsername())
                        .forumId(forumId)
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}