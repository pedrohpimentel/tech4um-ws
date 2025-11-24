package com.tech4um.projectWS.service;

import com.tech4um.projectWS.exception.ResourceNotFoundException;
import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com e-mail: " + email));
    }

    public Long getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para obter ID com e-mail: " + email));

        // Retorna o ID do usuário, que é um Long no JPA
        return user.getId();
    }
}