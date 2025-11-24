package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.dto.ForumRequest;
import com.tech4um.projectWS.dto.ForumResponse;
import com.tech4um.projectWS.model.Forum;
import com.tech4um.projectWS.service.ForumService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forums")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService){
        this.forumService = forumService;
    }

    // POST /api/forums
    // Cria um novo fórum
    @PostMapping
    public ResponseEntity<ForumResponse> createForum(@Valid @RequestBody ForumRequest request){
        //Mapeia DTO para o Modelo (Apenas no Controller/Service)
        Forum forum = new Forum();
        forum.setName(request.getName());
        forum.setDescription(request.getDescription());

        Forum createdForum = forumService.createForum(forum);

        //Mapeia o modelo para o DTO de resposta
        ForumResponse response = ForumResponse.builder()
                .id(createdForum.getId())
                .name(createdForum.getName())
                .description(createdForum.getDescription())
                .createdAt(createdForum.getCreatedAt())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET /api/forums
    // Lista todos os fóruns
    @GetMapping
    public ResponseEntity<List<ForumResponse>> getAllForums(){
        List<Forum> forums = forumService.findAllForums();

        //Mapeia a lista de Modelos para uma lista de DTOs
        List<ForumResponse> responses = forums.stream()
                .map(forum -> ForumResponse.builder()
                        .id(forum.getId())
                        .name(forum.getName())
                        .description(forum.getDescription())
                        .createdAt(forum.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // DELETE /api/forums/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForum(@PathVariable String id){
        forumService.deleteForum(id);
        return ResponseEntity.noContent().build();
    }
}
