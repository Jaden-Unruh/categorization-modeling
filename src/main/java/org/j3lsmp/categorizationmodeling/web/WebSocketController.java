package org.j3lsmp.categorizationmodeling.web;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.JsonNode;

@Controller
public class WebSocketController {
	
	@MessageMapping("/admin/stats")
	@SendTo("/topic/adminStats")
	public JsonNode broadCastStats(JsonNode node) {
		return node;
	}
}
