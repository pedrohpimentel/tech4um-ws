package com.tech4um.projectWS.config;

import com.tech4um.projectWS.security.StompJwtChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker // Habilita o WebSocket Message Broker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 1. Injeta o Interceptor STOMP que criamos (para autenticação JWT das mensagens)
    private final StompJwtChannelInterceptor stompJwtChannelInterceptor;

    // Define o endpoint de conexão (handshake) e configura o CORS
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // O cliente se conectará a 'ws://localhost:8080/ws'
        registry.addEndpoint("/ws")
                // Permite conexões de todos os domínios (ajustar em produção)
                .setAllowedOriginPatterns("*")
                // CRÍTICO: Adiciona o fallback para SockJS, que é essencial para evitar
                // problemas de conexão em ambientes com proxies ou firewalls restritivos.
                .withSockJS();
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
     * CRÍTICO: Registra o interceptor no canal de mensagens de entrada do cliente.
     * Isso faz com que o StompJwtChannelInterceptor seja executado para verificar e
     * autenticar o token JWT na primeira mensagem CONNECT e em cada comando SEND.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtChannelInterceptor);
    }
}