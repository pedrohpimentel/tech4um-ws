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
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        // Loga no início para confirmar se o Interceptor está sendo invocado
        System.out.println("### STOMP INTERCEPTOR DEBUG: Invocado para o comando: " + command);

        // A autenticação é obrigatória para os comandos CONNECT e SEND
        if (StompCommand.CONNECT.equals(command) || StompCommand.SEND.equals(command)) {

            List<String> authHeaders = accessor.getNativeHeader("Authorization");
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

                        // Define o Principal na sessão STOMP.
                        accessor.setUser(authToken);
                        isAuthenticated = true;
                        System.out.println("DEBUG STOMP: Usuário autenticado via Interceptor: " + userEmail);
                    } else {
                        System.err.println("JWT ERROR STOMP: Token inválido ou expirado. Bloqueando...");
                    }
                } catch (Exception e) {
                    System.err.println("JWT FATAL ERROR STOMP: Falha ao processar o token: " + e.getMessage());
                }
            } else {
                System.out.println("AVISO STOMP: Comando " + command + " sem cabeçalho Authorization.");
            }

            // --- CORREÇÃO CRÍTICA DE SEGURANÇA ---
            // Se o comando for CONNECT ou SEND E a autenticação falhar (isAuthenticated é falso),
            // BLOQUEIA a mensagem retornando null.
            if (!isAuthenticated) {
                System.err.println("BLOQUEIO STOMP: Falha na autenticação para o comando " + command + ". Retornando null.");
                return null;
            }
        }

        // Se o comando foi CONNECT ou SEND e passou na autenticação, ou se o comando não precisa de autenticação (SUBSCRIBE),
        // a mensagem é liberada.
        return message;
    }

    /**
     * Extrai o token JWT da lista de cabeçalhos Authorization.
     */
    private String extractToken(List<String> authHeaders) {
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String fullTokenHeader = authHeaders.get(0);
            if (fullTokenHeader.startsWith(BEARER_PREFIX)) {
                return fullTokenHeader.substring(BEARER_PREFIX.length());
            } else {
                System.err.println("JWT ERROR STOMP: Cabeçalho Authorization presente, mas sem prefixo 'Bearer'.");
            }
        }
        return null;
    }
}