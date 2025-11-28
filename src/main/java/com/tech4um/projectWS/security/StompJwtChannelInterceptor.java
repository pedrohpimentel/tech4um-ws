package com.tech4um.projectWS.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private static final String BEARER_PREFIX = "Bearer ";

    public StompJwtChannelInterceptor(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = null;
        try {
            accessor = StompHeaderAccessor.wrap(message);
            StompCommand command = accessor.getCommand();

            // 1. LOG DE INVOCACÃO DO INTERCEPTOR
            System.out.println("### STOMP INTERCEPTOR DEBUG: Invocado para o comando: " + command);

            if (StompCommand.CONNECT.equals(command) || StompCommand.SEND.equals(command)) {

                List<String> authHeaders = accessor.getNativeHeader("Authorization");

                // 2. LOG DE CABEÇALHO BRUTO
                System.out.println("DEBUG STOMP: Cabeçalho Authorization recebido: " + (authHeaders != null ? authHeaders.get(0) : "NULO"));

                String token = extractToken(authHeaders);

                boolean isAuthenticated = false;

                if (token != null) {
                    try {
                        if (tokenProvider.validateToken(token)) {
                            String userEmail = tokenProvider.getUserIdFromJWT(token);
                            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                            accessor.setUser(authToken);
                            isAuthenticated = true;
                            System.out.println("DEBUG STOMP: Usuário autenticado via Interceptor: " + userEmail);
                        } else {
                            System.err.println("JWT ERROR STOMP: Token inválido ou expirado. Bloqueando...");
                        }
                    } catch (Exception e) {
                        // 3. LOG DE ERRO DE PROCESSAMENTO DE TOKEN (Chave Secreta, Formato JWT, etc.)
                        System.err.println("JWT FATAL ERROR STOMP: Falha ao processar o token, CAUSA: " + e.getClass().getName() + " - " + e.getMessage());
                    }
                } else {
                    System.out.println("AVISO STOMP: Comando " + command + " sem token válido para processamento.");
                }

                // Bloqueia a mensagem se a autenticação falhou
                if (!isAuthenticated) {
                    System.err.println("BLOQUEIO STOMP: Falha na autenticação para o comando " + command + ". Retornando null.");
                    return null;
                }
            }
        } catch (Exception e) {
            // 4. LOG DE ERRO GERAL (Se o Interceptor travar antes de processar o comando)
            System.err.println("ERRO INESPERADO NO INTERCEPTOR: " + e.getMessage());
            e.printStackTrace();
            return null; // Retorna null para bloquear a mensagem em caso de erro grave
        }

        return message;
    }

    /**
     * Extrai o token JWT da lista de cabeçalhos Authorization.
     */
    private String extractToken(List<String> authHeaders) {
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String fullTokenHeader = authHeaders.get(0);
            if (fullTokenHeader != null && fullTokenHeader.startsWith(BEARER_PREFIX)) {
                return fullTokenHeader.substring(BEARER_PREFIX.length());
            } else {
                System.err.println("JWT ERROR STOMP: Cabeçalho Authorization presente, mas sem prefixo 'Bearer'. Cabeçalho: " + fullTokenHeader);
            }
        }
        return null;
    }
}