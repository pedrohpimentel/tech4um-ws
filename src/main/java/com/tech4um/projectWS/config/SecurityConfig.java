package com.tech4um.projectWS.config;

import com.tech4um.projectWS.security.JwtAuthenticationEntryPoint;
import com.tech4um.projectWS.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Import necessário
import org.springframework.http.HttpMethod; // Import necessário para referenciar GET, POST, etc.

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true) // Habilita o @Secured nas classes/métodos
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, JwtAuthenticationEntryPoint unauthorizedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Configuração do CORS (Mantida)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configura o EntryPoint para retornar 401 Unauthorized em falhas de autenticação
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // CRUCIAL: Sem sessão
                .authorizeHttpRequests(auth -> auth
                        // Rotas de autenticação são públicas
                        .requestMatchers("/api/auth/**").permitAll()

                        // CORREÇÃO: Permite acesso público a todos os métodos GET nos caminhos /api/forums
                        // Isso inclui: GET /api/forums, GET /api/forums/{id}, GET /api/forums/{id}/messages
                        .requestMatchers(HttpMethod.GET, "/api/forums", "/api/forums/**").permitAll()

                        // CRÍTICO: Rota /api/users requer ROLE_USER ou ROLE_ADMIN
                        .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")

                        // Rotas WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // Todas as outras rotas exigem autenticação
                        .anyRequest().authenticated())
                // Adiciona o filtro JWT antes do filtro de autenticação padrão do Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}