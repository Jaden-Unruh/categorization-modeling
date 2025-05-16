package org.j3lsmp.categorizationmodeling.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Websocket configuration changes. Recall, this doesn't work yet.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
	private final CmWebSocketHandler webSocketHandler;
	
	/**
	 * Basic constructor
	 * @param jwtHandshakeInterceptor the handshake intercpetor to use
	 * @param webSocketHandler the socket handler to use
	 */
	public WebSocketConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor, CmWebSocketHandler webSocketHandler) {
		this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
		this.webSocketHandler = webSocketHandler;
	}
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(webSocketHandler, "/ws")
			.addInterceptors(jwtHandshakeInterceptor)
			.setAllowedOrigins("*"); // TODO: tighten in prod
	}
}
