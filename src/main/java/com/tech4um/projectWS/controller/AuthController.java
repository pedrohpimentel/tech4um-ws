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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
        // Tenta autenticar o usuário com as credenciais fornecidas
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Gera o token JWT
        String jwt = tokenProvider.generateToken(authentication);

        // Retorna o token e os detalhes básicos do usuário
        User user = (User) authentication.getPrincipal();

        LoginResponse response = LoginResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .username(user.getUsername())
                .build();

        return ResponseEntity.ok(response);
    }

    // Rota de REGISTRO
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