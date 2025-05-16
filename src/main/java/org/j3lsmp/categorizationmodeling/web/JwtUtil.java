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

/**
 * Utilities class for JWT tokens
 */
@Component
public class JwtUtil {
	
	private static final String SECRET = "a-super-secure-secret-that-will-be-changed-on-launch-but-is-over-256-bits";
	private final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
	private final long EXPIRATION = 1000 * 60 * 60; //1 hour
	
	/**
	 * Generates a token with the specified traits
	 * @param username the token's username
	 * @param roles the token's roles
	 * @return the generated token
	 */
	public String generateToken(String username, List<String> roles) {
		return Jwts.builder()
				.subject(username)
				.claim("roles", roles)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION))
				.signWith(KEY)
				.compact();
	}
	
	/**
	 * Checks if the given token is valid
	 * @param token the token to check
	 * @return true if it's valid
	 */
	public Jws<Claims> validateToken(String token) {
		return Jwts.parser()
				.verifyWith(KEY)
				.build()
				.parseSignedClaims(token);
	}
	
	/**
	 * Pulls Claims from a given token
	 * @param token the token to pull from
	 * @return the token's Claims
	 */
	public Claims extractClaims(String token) {
		return validateToken(token).getPayload();
	}
	
	/**
	 * Pulls a username from a given token
	 * @param token the token to pull from
	 * @return the token's username
	 */
	public String extractUsername(String token) {
		return extractClaims(token).getSubject();
	}
	
	/**
	 * Pulls roles from a given token
	 * @param token the token to pull from
	 * @return the token's roles
	 */
	public List<String> extractRoles(String token) {
		Object roles = extractClaims(token).get("roles");
		if (roles instanceof List<?>)
			return ((List<?>) roles).stream().map(String::valueOf).toList();
		return Collections.emptyList();
	}
	
	/**
	 * Checks if a validated token is still valid - i.e. unexpired
	 * @param token the token to check
	 * @return if the token is both valid and unexpired
	 */
	public boolean isTokenValid(String token) {
		try {
			Date expiration = extractClaims(token).getExpiration();
			
			return expiration == null || expiration.after(new Date());
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}