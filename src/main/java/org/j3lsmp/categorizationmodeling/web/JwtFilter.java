package org.j3lsmp.categorizationmodeling.web;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A filter used to ensure that secure content (session stats) are only accessible by authorized users
 */
@Component
public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	
	/**
	 * Basic filter constructor
	 * @param jwtUtil the JWT utilities class to use
	 */
	public JwtFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Simply checks the users token, and returns 403 if they aren't allowed to access what they're trying to access.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
		
		if ("websocket".equalsIgnoreCase(request.getHeader("upgrade"))) {
			chain.doFilter(request, response);
			return;
		}
		
		String header = request.getHeader("Authorization");
		
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			try {
				Claims claims = jwtUtil.extractClaims(token);
				String username = claims.getSubject();
				
				var roles = jwtUtil.extractRoles(token).stream()
						.map(SimpleGrantedAuthority::new)
						.toList();
				
				var auth = new UsernamePasswordAuthenticationToken(username, null, roles);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}
		
		chain.doFilter(request, response);
	}
}