package at.aau.serg.scotlandyard.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import static org.mockito.Mockito.*;

class StompWebSocketConfigTest {

    @Test
    void testRegisterStompEndpoints() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration endpointRegistration = mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws-stomp")).thenReturn(endpointRegistration);

        StompWebSocketConfig config = new StompWebSocketConfig();
        config.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws-stomp");
        verify(endpointRegistration).setAllowedOrigins("*");
    }

    @Test
    void testConfigureMessageBroker() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        StompWebSocketConfig config = new StompWebSocketConfig();
        config.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/topic", "/queue");
        verify(registry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void testConfigureWebSocketTransport() {
        StompWebSocketConfig config = new StompWebSocketConfig();
        WebSocketTransportRegistration registration = mock(WebSocketTransportRegistration.class);

        when(registration.setSendTimeLimit(15 * 1000)).thenReturn(registration);
        when(registration.setSendBufferSizeLimit(512 * 1024)).thenReturn(registration);

        config.configureWebSocketTransport(registration);

        verify(registration).setSendTimeLimit(15 * 1000);
        verify(registration).setSendBufferSizeLimit(512 * 1024);
    }

}
