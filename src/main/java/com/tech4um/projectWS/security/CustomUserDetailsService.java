package com.tech4um.projectWS.security;

import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Esta classe é usada pelo Spring Security para carregar os dados do usuário a partir do MySQL (JPA).
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Busca o usuário no repositório (JPA/MySQL)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    // Esta exceção é capturada pelo Spring Security para retornar 401/403
                    return new UsernameNotFoundException("Usuário não encontrado com e-mail: " + email);
                });

        // AJUSTE: Retorna diretamente o objeto User, pois ele agora implementa UserDetails
        // Isso elimina o ClassCastException no AuthController.
        return user;
    }
}