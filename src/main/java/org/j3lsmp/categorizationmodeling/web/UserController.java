package org.j3lsmp.categorizationmodeling.web;

import java.util.HashMap;
import java.util.Map;

import org.j3lsmp.categorizationmodeling.FinalModel;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
	@GetMapping("/isValidSession/{sessionId}")
	public ResponseEntity<Boolean> isValidSession(@PathVariable String sessionId) {
		return ResponseEntity.ok(Server.ALL_SESSIONS.containsKey(sessionId));
	}

	@PostMapping("/startSession")
	public ResponseEntity<String> createSession(@RequestBody SessionRequest request) {
		String sessionId;

		do {
			sessionId = Integer.toString((int) (Math.random() * 9e5 + 1e5));
		} while (Server.ALL_SESSIONS.containsKey(sessionId));

		Session session = new Session(sessionId, request.firstName(), request.lastName(), request.email(),
				request.pronouns());
		FinalModel model = new FinalModel();

		Server.ALL_SESSIONS.put(sessionId, session);
		Server.ALL_MODELS.put(sessionId, model);

		return ResponseEntity.ok(sessionId);
	}
	
	@GetMapping("/session/{sessionId}")
	public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
		Map<String, Object> response = new HashMap<>();
		Session thisSession = Server.ALL_SESSIONS.get(sessionId);
		FinalModel thisModel = Server.ALL_MODELS.get(sessionId);
		
		if (thisSession == null)
			return ResponseEntity.internalServerError().build();
		
		response.put("firstName", thisSession.firstName);
		response.put("trialNumber", thisModel.numAnswered);
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/trialImages/{imageName}", produces = "image/svg+xml")
	public ResponseEntity<String> getImage(@PathVariable String imageName) throws Exception {
		/*
		 * TODO: Use once we generate appropriate images
		 * Path imagePath = Paths.get("src/main/resources/trialImages", imageName);
		 * return Files.readAllBytes(imagePath);
		 */
		
		if (!imageName.matches("[01]{4}.svg"))
			return ResponseEntity.badRequest().body("<svg></svg>");
		
		String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='300' height='300'>" +
                "<rect width='100%' height='100%' fill='#f0f0f0'/>" +
                "<text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' " +
                "font-size='48' font-family='Arial' fill='#333'>" + imageName.substring(0, 4) + "</text>" +
                "</svg>";
		
		return ResponseEntity.ok(svg);
	}
	
	@GetMapping("/trial/{sessionId}/{trialNum}")
	public ResponseEntity<Map<String, String>> getTrial(@PathVariable String sessionId, @PathVariable String trialNum) {
		Map<String, String> response = new HashMap<>();
		
		//TODO update with proper shit - probably not just random
		
		String imageNum = randomImageNum();
		
		Server.ALL_SESSIONS.get(sessionId).setImage(imageNum);
		
		response.put("imageUrl", "/api/trialImages/" + imageNum + ".svg");
		response.put("isEvil", "true");
		
		return ResponseEntity.ok(response);
	}
	
	private String randomImageNum() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 4; i++)
			sb.append(Math.random() > 0.5 ? '1' : '0');
		return sb.toString();
	}
	
	private final SimpMessagingTemplate messagingTemplate;
	
	@PostMapping("/submit/{sessionId}/{trialNum}")
	public ResponseEntity<String> submitChoice(@RequestBody String choice, @PathVariable String sessionId, @PathVariable String trialNum) {
		System.out.println(choice);
		
		Server.ALL_SESSIONS.get(sessionId).currentImageNum = Integer.parseInt(trialNum) + 1;
		
		JsonNode updatedStats = LiveStats.getUpdatedStats();
		messagingTemplate.convertAndSend("/topic/admin", updatedStats);
		
		return ResponseEntity.ok("{\"Why\": \"hello there :P\"}");
	}
}