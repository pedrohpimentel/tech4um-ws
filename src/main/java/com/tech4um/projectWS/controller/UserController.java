package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional; // Import necessário
import java.util.stream.Collectors;

/*
 * Controlador REST para endpoints de usuários (protegidos).
 * Mapeia a rota base /api/users.
 * O acesso a esta rota é protegido pelo SecurityConfig, que exige ROLE_USER ou ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * Endpoint para obter todos os usuários. Rota: GET /api/users
     * Requer JWT no cabeçalho e Role: ROLE_USER ou ROLE_ADMIN.
     * @return Lista de usuários (o campo de senha é removido por segurança).
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();

        // Boas Práticas de Segurança: Remove a senha de cada objeto User antes de retornar.
        List<User> usersWithoutPassword = users.stream()
                .peek(user -> user.setPassword(null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(usersWithoutPassword);
    }

    /*
     * Endpoint para obter um usuário específico por ID. Rota: GET /api/users/{id}
     *
     * @param id O ID do usuário a ser buscado (extraído da URL).
     * @return O usuário correspondente (sem senha) ou 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /*
     * NOVO ENDPOINT: Atualiza um usuário existente por ID. Rota: PUT /api/users/{id}
     *
     * @param id O ID do usuário a ser atualizado.
     * @param userDetails Os novos dados do usuário (recebidos via corpo JSON).
     * @return O usuário atualizado (sem senha) ou 404 Not Found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();

            // Atualiza os campos:
            if (userDetails.getUsername() != null) {
                existingUser.setUsername(userDetails.getUsername());
            }
            if (userDetails.getEmail() != null) {
                existingUser.setEmail(userDetails.getEmail());
            }

            User updatedUser = userRepository.save(existingUser);

            // Remove a senha e retorna 200 OK
            updatedUser.setPassword(null);
            return ResponseEntity.ok(updatedUser);
        } else {
            // Retorna 404 Not Found se o usuário não existir
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * NOVO ENDPOINT: Exclui um usuário por ID. Rota: DELETE /api/users/{id}
     * Implementa uma regra de segurança para garantir que apenas o proprietário da conta
     * ou um ADMIN possa executar a exclusão.
     *
     * @param id O ID do usuário a ser excluído.
     * @param currentUser O objeto do usuário logado (fornecido pelo Spring Security).
     * @return 204 No Content se excluído, 404 Not Found ou 403 Forbidden.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails currentUser) {

        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User userToDelete = userOptional.get();

            // 1. Verificação de Autorização: O usuário logado é o proprietário da conta?
            boolean isOwner = userToDelete.getEmail().equals(currentUser.getUsername());

            // 2. Verificação de Admin (simples)
            boolean isAdmin = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // Se não for o dono da conta E não for Admin, nega a operação.
            if (!isOwner && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Retorna 403 Forbidden
            }

            // Executa a exclusão
            userRepository.delete(userToDelete);

            // Retorna 204 No Content
            return ResponseEntity.noContent().build();
        } else {
            // Retorna 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
}