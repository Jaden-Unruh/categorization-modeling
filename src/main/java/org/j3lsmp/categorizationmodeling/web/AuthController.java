package org.j3lsmp.categorizationmodeling.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for authorization, handles backend of sign-in
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private final AuthenticationManager authManager;
	private final JwtUtil jwtUtil;
	
	/**
	 * Basic authorization controller constructor
	 * @param authManager the authentication manager to use
	 * @param jwtUtil utilities class for authentication
	 */
	public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil) {
		this.authManager = authManager;
		this.jwtUtil = jwtUtil;
	}
	
	/**
	 * Handles post request to /login, returns a JWT token if successful or a 403 if failed
	 * @param request a login request
	 * @return ResponseEntity, with token or 403 error
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		try {
			Authentication auth = authManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.username(), request.password())
				);
			
			List<String> roles = auth.getAuthorities().stream()
					.map(GrantedAuthority::getAuthority)
					.toList();
			
			String jwt = jwtUtil.generateToken(request.username(), roles);
			return ResponseEntity.ok(Map.of("token", jwt));
		} catch (AuthenticationException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}
	}
	
	/**
	 * Holds a labeled username and password
	 * @param username the username
	 * @param password the password
	 */
	public record LoginRequest(String username, String password) {}
}
