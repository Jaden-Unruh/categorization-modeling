package org.j3lsmp.categorizationmodeling.web;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.lang.Collections;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	
	private static final String SECRET = "a-super-secure-secret-that-will-be-changed-on-launch-but-is-over-256-bits";
	private final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
	private final long EXPIRATION = 1000 * 60 * 60; //1 hour
	
	public String generateToken(String username, List<String> roles) {
		return Jwts.builder()
				.subject(username)
				.claim("roles", roles)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION))
				.signWith(KEY)
				.compact();
	}
	
	public Jws<Claims> validateToken(String token) {
		return Jwts.parser()
				.verifyWith(KEY)
				.build()
				.parseSignedClaims(token);
	}
	
	public Claims extractClaims(String token) {
		return validateToken(token).getPayload();
	}
	
	public String extractUsername(String token) {
		return extractClaims(token).getSubject();
	}
	
	public List<String> extractRoles(String token) {
		Object roles = extractClaims(token).get("roles");
		if (roles instanceof List<?>)
			return ((List<?>) roles).stream().map(String::valueOf).toList();
		return Collections.emptyList();
	}
	
	public boolean isTokenValid(String token) {
		try {
			Date expiration = extractClaims(token).getExpiration();
			
			return expiration == null || expiration.after(new Date());
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}