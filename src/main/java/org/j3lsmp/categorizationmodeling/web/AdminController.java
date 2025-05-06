package org.j3lsmp.categorizationmodeling.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@GetMapping("/stats")
	public ResponseEntity<JsonNode> stats() {
		return ResponseEntity.ok(LiveStats.getUpdatedStats());
	}

	@GetMapping("/sessionData/{sessionId}")
	public ResponseEntity<Session> getSessionData(@PathVariable String sessionId) {
		return ResponseEntity.ok(Server.ALL_SESSIONS.get(sessionId));
	}
}