package com.tech4um.projectWS.service;

import com.tech4um.projectWS.exception.ResourceNotFoundException;
import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Tempo de validade do token em minutos
    private static final int EXPIRATION_MINUTES = 30;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Métodos de Busca para o ChatController e Login ---

    /*
     * Busca um usuário pelo e-mail, retornando um Optional.
     * Necessário para o ChatController (sender/recipient) e AuthController.
     */
    public Optional<User> findByEmail(String email) {
        // Renomeado de findUserByEmail para findByEmail para melhor padronização
        return userRepository.findByEmail(email);
    }

    /*
     * Busca um usuário pelo ID, retornando um Optional.
     * Necessário para o ChatController (recuperar o destinatário pelo ID).
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /*
     * Busca o ID do usuário pelo e-mail (Mantido para compatibilidade, mas findByEmail é preferível).
     */
    public Long getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para obter ID com e-mail: " + email));

        return user.getId();
    }

    // --- Lógica de Redefinição de Senha ---

    /*
     * 1. Geração de Token de Redefinição
     * @param email O e-mail do usuário
     * @return O token gerado (String) ou null se o usuário não for encontrado.
     */
    public String createPasswordResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Retorna nulo para evitar vazamento de informações.
            return null;
        }

        User user = userOptional.get();
        // Gera um token único
        String token = UUID.randomUUID().toString();

        // Armazena o token e a data/hora de expiração (30 min) na entidade User.
        user.setResetPasswordToken(token);
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));

        userRepository.save(user);

        // Simulação do envio de e-mail (Token real que o front-end usará)
        System.out.println("DEBUG: Token de Redefinição Gerado para " + email + ": " + token);

        return token;
    }

    /*
     * 2. Execução da Redefinição de Senha
     * @param token O token de redefinição
     * @param newPassword A nova senha em texto simples
     * @return true se a senha foi redefinida, false se o token é inválido ou expirou.
     */
    public boolean resetPassword(String token, String newPassword) {
        // 1. Encontra o usuário pelo token
        Optional<User> userOptional = userRepository.findByResetPasswordToken(token);

        if (userOptional.isEmpty()) {
            return false; // Token não existe
        }

        User user = userOptional.get();

        // 2. Verifica a expiração
        if (user.getTokenExpiryDate() == null || user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            // Limpa o token expirado
            user.setResetPasswordToken(null);
            user.setTokenExpiryDate(null);
            userRepository.save(user);
            return false; // Token expirado
        }

        // 3. Criptografa e salva a nova senha
        user.setPassword(passwordEncoder.encode(newPassword));

        // 4. Limpa o token e a data/hora para evitar reuso
        user.setResetPasswordToken(null);
        user.setTokenExpiryDate(null);

        userRepository.save(user);

        return true; // Senha redefinida com sucesso
    }
}