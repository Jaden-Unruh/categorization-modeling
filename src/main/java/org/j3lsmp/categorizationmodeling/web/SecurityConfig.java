package org.j3lsmp.categorizationmodeling.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Config class for web security
 */
@Configuration
public class SecurityConfig {
	
	private final JwtFilter jwtFilter;
	
	/**
	 * Basic constructor
	 * @param jwtFilter JWT filter to use
	 */
	public SecurityConfig(JwtFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}
	
	/**
	 * Defines which pages should require authentication, and which roles can access them
	 * @param http to operate on
	 * @return configured
	 * @throws Exception if anything goes wrong
	 */
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/ws*", "/ws/**").permitAll()
						.anyRequest().permitAll()
					)
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}
	
	/**
	 * Gets the current authentication manager
	 * @param http to operate on
	 * @return authentication manager
	 * @throws Exception if anything goes wrong
	 */
	@Bean
	AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		return http.getSharedObject(AuthenticationManagerBuilder.class).build();
	}
	
	/**
	 * Sets up the users. In deployment, this will be populated by data not shown in the github
	 * @return the user details
	 */
	@Bean
	UserDetailsService userDetailsService() {
		PasswordEncoder encoder = passwordEncoder();
		UserDetails admin = User.builder()
				.username("Jaden")
				.password(encoder.encode("examplePassword")) //TODO change on launch
				.roles("ADMIN")
				.build();
		return new InMemoryUserDetailsManager(admin);
	}
	
	/**
	 * Gets the password encoder
	 * @return A BCryptPasswordEncoder
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
