package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.dto.ForumRequest;
import com.tech4um.projectWS.dto.ForumResponse;
import com.tech4um.projectWS.dto.MessageRequest;
import com.tech4um.projectWS.dto.MessageResponse;
import com.tech4um.projectWS.exception.ResourceNotFoundException;
import com.tech4um.projectWS.model.Forum;
import com.tech4um.projectWS.model.Message;
import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.ForumRepository;
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
    private final UserRepository userRepository;
    private final ForumRepository forumRepository;

    public ForumController(ForumService forumService, UserRepository userRepository, ForumRepository forumRepository){
        this.forumService = forumService;
        this.userRepository = userRepository;
        this.forumRepository = forumRepository;
    }

    // =========================================================================
    // CRUD DE FÓRUM (Contêiner)
    // =========================================================================

    /*
     * POST /api/forums
     * Cria um novo fórum.
     */
    @PostMapping
    public ResponseEntity<ForumResponse> createForum(@Valid @RequestBody ForumRequest request,
                                                     @AuthenticationPrincipal UserDetails currentUser) {

        // 1. Encontra o ID do usuário logado via token JWT
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado."));

        // 2. O Service recebe o DTO e já retorna o DTO de Resposta (ForumResponse)
        ForumResponse response = forumService.createForum(request, user.getId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /*
     * GET /api/forums
     * Lista todos os fóruns (PÚBLICO).
     */
    @GetMapping
    public ResponseEntity<List<ForumResponse>> getAllForums(){
        List<ForumResponse> responses = forumService.findAllForums();

        return ResponseEntity.ok(responses);
    }

    /*
     * GET /api/forums/{id}
     * Busca um fórum específico pelo seu ID (PÚBLICO).
     * ESTE É O MÉTODO QUE FALTAVA.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ForumResponse> getForumById(@PathVariable Long id) {
        // Assume-se que forumService.findById() retorna o ForumResponse e lida com o ResourceNotFoundException
        ForumResponse forum = forumService.findById(id);
        return ResponseEntity.ok(forum);
    }


    /*
     * DELETE /api/forums/{id}
     * Exclui um fórum. Apenas Criador ou ADMIN pode executar.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForum(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails currentUser){

        // 1. Busca a Entidade Forum
        Forum forumToDelete = forumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fórum não encontrado com ID: " + id));

        // 2. Verifica o usuário logado
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado."));

        // 3. Checa se o usuário logado é o CRIADOR
        boolean isCreator = forumToDelete.getCreator().getId().equals(user.getId());

        // 4. Checa se o usuário é ADMIN (usando a autoridade do Spring Security)
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isCreator && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        }

        // Se autorizado, delega a exclusão ao Service
        forumService.deleteForum(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // MENSAGENS (Messages)
    // =========================================================================

    /*
     * POST /api/forums/{forumId}/messages
     * Envia uma nova mensagem no fórum.
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

        // 1. Encontra o fórum (apenas para validar existência, embora o Service também faça)
        forumService.findById(forumId);

        List<Message> messages = forumService.getMessagesByForum(forumId);

        // 2. Mapeia a lista de Modelos para uma lista de DTOs de Resposta
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