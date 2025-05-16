package org.j3lsmp.categorizationmodeling.web;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Websocket controller. Be advised, I still haven't figured out websockets, and this doesn't work.
 */
@Controller
public class CmWebSocketController {
	
	/**
	 * Maps /admin/stats requests to the internal /topic/adminStats websocket
	 * @param node a jsonnode
	 * @return the same node
	 */
	@MessageMapping("/admin/stats")
	@SendTo("/topic/adminStats")
	public JsonNode broadCastStats(JsonNode node) {
		return node;
	}
}
