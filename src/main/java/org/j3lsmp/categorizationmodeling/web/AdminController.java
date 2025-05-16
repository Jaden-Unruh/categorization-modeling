package org.j3lsmp.categorizationmodeling.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Rest controller for admin api - serves confidential data to admin page, protected by password authentication
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
	
	/**
	 * Serves all current user statistics and information
	 * @return ResponseEntity with Json file of all information
	 */
	@GetMapping("/stats")
	public ResponseEntity<JsonNode> stats() {
		return ResponseEntity.ok(LiveStats.getUpdatedStats());
	}

	/**
	 * Serves information about a specific session given its id
	 * @param sessionId the sessionID to fetch for
	 * @return ResponseEntity with Json file of session information
	 */
	@GetMapping("/sessionData/{sessionId}")
	public ResponseEntity<Session> getSessionData(@PathVariable String sessionId) {
		return ResponseEntity.ok(Server.ALL_SESSIONS.get(sessionId));
	}
}