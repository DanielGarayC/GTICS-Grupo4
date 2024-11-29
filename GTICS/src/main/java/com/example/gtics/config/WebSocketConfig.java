package com.example.gtics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Configuración del broker de mensajes
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Se habilita el broker en memoria con los prefijos '/topic' para los mensajes dirigidos a todos
        config.enableSimpleBroker("/topic");
        // Definimos '/app' como el prefijo para las rutas de destino de los mensajes
        config.setApplicationDestinationPrefixes("/app");
    }

    // Registro del punto final (endpoint) de WebSocket
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Se registra el endpoint '/chat-websocket' para que los clientes puedan conectarse a este punto
        registry.addEndpoint("/chat-websocket")
                .setAllowedOrigins("https://expressdeals.online","http://expressdeals.online")
                .withSockJS();
    }
}
