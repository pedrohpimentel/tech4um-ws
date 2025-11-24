package com.tech4um.projectWS.security;

import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

//Esta classe é usada pelo Spring Security para carregar os dados do usuário a partir do MongoDB.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 💡 DEBUG: Rastreia qual e-mail o token está pedindo
        System.out.println("DEBUG: Tentando buscar no MongoDB o e-mail: [" + email + "]");

        // 1. Busca o usuário no seu repositório (MongoDB)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    // DEBUG DE ERRO: Esta mensagem aparece se o 403 é causado por USUÁRIO INEXISTENTE
                    System.err.println("ERRO CRÍTICO 403 CAUSADO: Usuário não encontrado no DB para o e-mail: " + email);
                    return new UsernameNotFoundException("Usuário não encontrado com e-mail: " + email);
                });

        //  DEBUG: Confirma que a busca foi bem-sucedida e que o 403 não deveria ocorrer
        System.out.println("DEBUG: SUCESSO! Usuário encontrado no DB. Prosseguindo...");


        // 2. Converte seu modelo 'User' para o 'UserDetails' do Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList() // Se você não tem roles/perfis, use uma lista vazia
        );
    }
}