package com.tech4um.projectWS.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Habilita o WebSocket Message Broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Define o endpoint de conexão (handshake) e configura o CORS
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // O cliente se conectará a 'ws://localhost:8080/ws'
        registry.addEndpoint("/ws")
                // Permite conexões de todos os domínios (ajustar em produção)
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback para navegadores antigos
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

}
