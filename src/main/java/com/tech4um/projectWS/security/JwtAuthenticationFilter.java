package com.tech4um.projectWS.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

// Esta classe é o "porteiro" que intercepta as requisições HTTP e valida o token no cabeçalho.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService){
        this.tokenProvider = tokenProvider;
        this.userDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // 1. Obtém o email (userId) do token
                String userEmail = tokenProvider.getUserIdFromJWT(jwt);

                // 2. Carrega os detalhes do usuário
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // 💡 LOG DE DEBUG CRÍTICO: Mostra as permissões carregadas do objeto User
                System.out.println("DEBUG (Authorities): Permissões carregadas do usuário: " + userDetails.getAuthorities());

                // 3. Cria o objeto de Autenticação
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Injeta a autenticação no contexto
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 💡 LOG DE DEBUG CRÍTICO: Confirma que o token foi injetado como autenticado
                System.out.println("DEBUG (Context): Token injetado com status isAuthenticated(): " + authentication.isAuthenticated());
            }
        } catch (Exception ex) {
            // Logar o erro (ex: JWT inválido, expirado)
            System.err.println("JWT ERROR (Auth Filter): Falha ao autenticar token: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // Método para extrair o JWT do header
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Log atualizado para melhor leitura e para garantir que o token está chegando
        System.out.println("Authorization: " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer "
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Se a rota começar com /api/auth/, pule este filtro
        return request.getServletPath().startsWith("/api/auth");
    }

}