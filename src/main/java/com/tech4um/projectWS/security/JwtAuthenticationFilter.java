package com.tech4um.projectWS.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher; // CRÍTICO: Novo import para correspondência de caminho
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// Esta classe é o "porteiro" que intercepta as requisições HTTP e valida o token no cabeçalho.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    // Instância do AntPathMatcher para verificação robusta de caminhos
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Lista de caminhos a serem ignorados pelo filtro JWT (não requerem autenticação HTTP)
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/auth/**", // Exclui todas as rotas de autenticação (login, registro)
            "/ws/**"        // CRÍTICO: Exclui o WebSocket Handshake (como /ws/info)
    );

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = customUserDetailsService;
    }

    /**
     * CRÍTICO: Implementação para ignorar o filtro JWT para caminhos específicos.
     * Usamos o AntPathMatcher na URI completa (getCaminhoDaRequisicao) para maior precisão.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Obtém o URI da requisição (ex: /ws/info, /api/auth/login)
        String requestUri = request.getRequestURI();

        // Itera sobre os caminhos excluídos
        for (String excludedPath : EXCLUDED_PATHS) {
            if (pathMatcher.match(excludedPath, requestUri)) {
                System.out.println("DEBUG (Filter): Ignorando filtro JWT para caminho: " + requestUri);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Obtém o email (que é o userId neste contexto) do token
                String userEmail = tokenProvider.getUserIdFromJWT(jwt);

                // Carrega os detalhes do usuário usando o método: loadUserByUsername
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                System.out.println("DEBUG (Authorities): Permissões carregadas do usuário: " + userDetails.getAuthorities());

                // Cria o objeto de Autenticação
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Injeta a autenticação no contexto
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("DEBUG (Context): Token injetado com status isAuthenticated(): " + authentication.isAuthenticated());
            }
        } catch (UsernameNotFoundException ex) {
            // Tratamento específico para quando o e-mail extraído do token não existe no banco
            System.err.println("JWT ERROR (Auth Filter): Usuário do token não encontrado no banco de dados: " + ex.getMessage());
        } catch (Exception ex) {
            // Logar o erro geral (ex: JWT inválido, expirado, problemas no tokenProvider)
            System.err.println("JWT ERROR (Auth Filter): Falha ao autenticar token JWT: " + ex.getMessage());
        }

        // Linha 58 (Chamada do filtro)
        filterChain.doFilter(request, response);
    }

    // Método para extrair o JWT do header
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        System.out.println("Authorization Header Recebido: " + (bearerToken != null ? "Sim" : "Não"));
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer "
        }
        return null;
    }
}