package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.dto.LoginRequest;
import com.tech4um.projectWS.dto.LoginResponse;
import com.tech4um.projectWS.dto.RegisterRequest;
import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.UserRepository;
import com.tech4um.projectWS.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ALTERAÇÃO : Adiciona a anotação @CrossOrigin para permitir requisições de origem cruzada
// A rota de login usa POST.
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthController {

    // Adiciona logger para ver erros no console
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    // Rota de LOGIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            // Tenta autenticar o usuário com as credenciais fornecidas
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            // 💡 AJUSTE CRÍTICO: Loga a exceção exata (BadCredentials ou UsernameNotFound)
            logger.error("Falha na autenticação para o e-mail {}: {}", loginRequest.getEmail(), e.getMessage());

            // Retorna 401 Unauthorized se a autenticação falhar
            return new ResponseEntity(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Gera o token JWT
        String jwt = tokenProvider.generateToken(authentication);

        // Retorna o token e os detalhes básicos do usuário
        // O cast para User agora funciona, pois sua entidade implementa UserDetails
        User user = (User) authentication.getPrincipal();

        LoginResponse response = LoginResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .username(user.getUsername())
                .build();

        return ResponseEntity.ok(response);
    }

    // Rota de REGISTRO (Mantida, pois está correta)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // TRATAMENTO DE ERRO: 409 Conflict
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return new ResponseEntity<>("O e-mail já está em uso!", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());

        // Criptografa a senha com BCrypt!
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        userRepository.save(user);

        return new ResponseEntity<>("Usuário registrado com sucesso!", HttpStatus.CREATED);
    }
}