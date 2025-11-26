package com.tech4um.projectWS.controller;

import com.tech4um.projectWS.dto.ForgotPasswordRequest;
import com.tech4um.projectWS.dto.LoginRequest;
import com.tech4um.projectWS.dto.LoginResponse;
import com.tech4um.projectWS.dto.RegisterRequest;
import com.tech4um.projectWS.dto.ResetPasswordRequest;
import com.tech4um.projectWS.model.User;
import com.tech4um.projectWS.repository.UserRepository;
import com.tech4um.projectWS.security.JwtTokenProvider;
import com.tech4um.projectWS.service.UserService;
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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    //  Injeção do UserService
    private final UserService userService;

    // Incluindo o UserService
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    // Rota de LOGIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            logger.error("Falha na autenticação para o e-mail {}: {}", loginRequest.getEmail(), e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();

        LoginResponse response = LoginResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .username(user.getUsername())
                .build();

        return ResponseEntity.ok(response);
    }

    // Rota de REGISTRO (ATUALIZADA com o campo Role)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return new ResponseEntity<>("O e-mail já está em uso!", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        // CRÍTICO: Define o papel do usuário. Se o DTO não enviar, o padrão é USER.
        user.setRole(registerRequest.getRole());

        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);

        return new ResponseEntity<>("Usuário registrado com sucesso!", HttpStatus.CREATED);
    }

    // Geração de Token de Redefinição
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // A lógica de geração de token e checagem de e-mail está no UserService
        String token = userService.createPasswordResetToken(request.getEmail());

        // Retorna sucesso para evitar vazamento de dados, mesmo que o e-mail não exista.
        return ResponseEntity.ok("Se o e-mail estiver cadastrado, um link de redefinição será enviado.");
    }

    // Execução da Redefinição de Senha
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // A lógica de validação de token, expiração e atualização de senha está no UserService
        boolean success = userService.resetPassword(request.getToken(), request.getNewPassword());

        if (success) {
            return ResponseEntity.ok("Senha redefinida com sucesso. Faça login com a nova senha.");
        } else {
            // Token inválido ou expirado
            return new ResponseEntity<>("O token de redefinição é inválido ou expirou.", HttpStatus.BAD_REQUEST);
        }
    }
}