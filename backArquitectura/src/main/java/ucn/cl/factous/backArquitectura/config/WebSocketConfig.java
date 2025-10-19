package ucn.cl.factous.backArquitectura.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker de mensajes simple en memoria para el prefijo "/topic"
        config.enableSimpleBroker("/topic");
        // Define el prefijo para los mensajes enviados desde el cliente al servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra el endpoint "/ws" para conexiones WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permite conexiones desde cualquier origen
                .withSockJS(); // Habilita SockJS como fallback
        
        // También registrar endpoint sin SockJS para conexiones WebSocket nativas
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // Sin SockJS para WebSocket nativo
    }
}