package org.j3lsmp.categorizationmodeling.web;

import java.net.URI;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Websocket security. Still doesn't work as expected.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
	
	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;
	
	/**
	 * Basic constructor
	 * @param jwtUtil JWT utilities to use
	 * @param userDetailsService User Details to use
	 */
	public JwtHandshakeInterceptor(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
		System.out.println("Interceptor constructed");
	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		System.out.println("before handshake");
		try {
			URI uri = request.getURI();
			MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
			String token = queryParams.getFirst("token");
			
			System.out.println("Validating with token: " + token);
			
			if (token == null || !jwtUtil.isTokenValid(token))
				return false;
			
			String username = jwtUtil.extractUsername(token);
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(authentication);
			SecurityContextHolder.setContext(context);
			
			attributes.put("username", username);
			return true;
		} catch (Exception e) {
			System.err.println("Websocket authentication failed: " + e.getMessage());
			return false;
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		//do nothing
	}

}
