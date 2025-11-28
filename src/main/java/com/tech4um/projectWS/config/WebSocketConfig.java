package com.tech4um.projectWS.config;

import com.tech4um.projectWS.security.StompJwtChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration; // Novo import necessário
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor; // Novo import (usando Lombok, já que você o possui)

@Configuration
@EnableWebSocketMessageBroker // Habilita o WebSocket Message Broker
@RequiredArgsConstructor // Cria um construtor com todos os campos 'final' (para injetar o Interceptor)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 1. Injeta o Interceptor STOMP que criamos
    private final StompJwtChannelInterceptor stompJwtChannelInterceptor;

    // Define o endpoint de conexão (handshake) e configura o CORS
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // O cliente se conectará a 'ws://localhost:8080/ws'
        registry.addEndpoint("/ws")
                // Permite conexões de todos os domínios (ajustar em produção)
                .setAllowedOriginPatterns("*");
    }

    // Configura o Message Broker
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        // Prefixo para o destino onde o servidor envia a mensagem ao cliente (Ex: /topic/forum.123)
        registry.enableSimpleBroker("/topic", "/user");

        // Prefixo para o destino onde o cliente envia mensagens para o servidor (Ex: /app/chat.send)
        registry.setApplicationDestinationPrefixes("/app");

        // Prefixo para mensagens privadas
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 2. CRÍTICO: Registra o interceptor no canal de mensagens de entrada do cliente.
     * Isso faz com que o StompJwtChannelInterceptor seja executado antes do ChatController
     * para verificar e autenticar o token JWT em cada comando CONNECT ou SEND.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtChannelInterceptor);
    }
}