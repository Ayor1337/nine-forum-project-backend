package com.ayor.config;

import com.ayor.interceptor.StompAuthInterceptor;
import com.ayor.interceptor.WebsocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final StompAuthInterceptor authInterceptor;
    private final WebsocketHandshakeInterceptor websocketHandshakeInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chatboard", "/chat", "/system")
                .setAllowedOrigins("*")
                .addInterceptors(websocketHandshakeInterceptor);

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
        config.enableSimpleBroker(
                "/broadcast",
                "/transfer",
                "/notif",
                "/verify");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }


}
