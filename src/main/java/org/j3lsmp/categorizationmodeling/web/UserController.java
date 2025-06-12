package org.j3lsmp.categorizationmodeling.web;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.j3lsmp.categorizationmodeling.FinalModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controller for the user's api
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
	/**
	 * Get mapping to check if a given session id indeed maps to a valid session
	 * @param sessionId the id to check
	 * @return a responseentity with true if it's a valid session
	 */
	@GetMapping("/isValidSession/{sessionId}")
	public ResponseEntity<Boolean> isValidSession(@PathVariable String sessionId) {
		return ResponseEntity.ok(Server.ALL_SESSIONS.containsKey(sessionId));
	}

	/**
	 * Post mapping to create a new session with the given details
	 * @param request the details to attach to the session
	 * @return ResponseEntity with the new session id assigned
	 */
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
	
	/**
	 * Get mapping for details of a session - only returns first name and trial number, need admin authentication to access full details
	 * 
	 * Used to show header in user's view
	 * @param sessionId the session id to check
	 * @return responseentity with map for details
	 */
	@GetMapping("/session/{sessionId}")
	public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
		Map<String, Object> response = new HashMap<>();
		Session thisSession = Server.ALL_SESSIONS.get(sessionId);
		FinalModel thisModel = Server.ALL_MODELS.get(sessionId);
		
		if (thisSession == null)
			return ResponseEntity.internalServerError().build();
		
		response.put("firstName", (thisSession.is2participants) ? String.format("%s & %s", thisSession.firstName, thisSession.p2firstName) : thisSession.firstName);
		response.put("trialNumber", thisModel.numAnswered);
		
		return ResponseEntity.ok(response);
	}
	
	/**
	 * Gets trial image given it's name (4 binary digits `.svg`)
	 * @param imageName the name, regex "[01]{4}.svg"
	 * @return ResponseEntity string SVG content
	 * @throws Exception if something goes wrong
	 */
	@GetMapping(value = "/trialImages/{imageName}", produces = "image/svg+xml")
	public ResponseEntity<String> getImage(@PathVariable String imageName) throws Exception {
		
		if (!imageName.matches("[01]{4}.svg"))
			return ResponseEntity.badRequest().body("<svg></svg>");
		
		InputStream inputStream = UserController.class.getClassLoader().getResourceAsStream("trialImages/" + imageName);
		String svg = "";
		try (Scanner scan = new Scanner(inputStream, StandardCharsets.UTF_8)) {
			svg = scan.useDelimiter("\\A").next();
		}
		
		return ResponseEntity.ok(svg);
	}
	
	/**
	 * Get mapping for trial details
	 * @param sessionId the id of the session
	 * @param trialNum the number of the trial within the session
	 * @return responseentity containing a map with imageURL and whether the fruit is edible
	 */
	@GetMapping("/trial/{sessionId}/{trialNum}")
	public ResponseEntity<Map<String, String>> getTrial(@PathVariable String sessionId, @PathVariable String trialNum) {
		Map<String, String> response = new HashMap<>();
		
		int trial = Integer.parseInt(trialNum);
		
		if (trial < 30) {
		
			
			
			String imageNum = "1011";
			boolean isEvil = false;
			
			Server.ALL_SESSIONS.get(sessionId).lastLoadTime = System.nanoTime();
			
			Server.ALL_SESSIONS.get(sessionId).setImage(imageNum);
			Server.ALL_SESSIONS.get(sessionId).currentImageEvil = isEvil;
			
			response.put("imageUrl", "/api/trialImages/" + imageNum + ".svg");
			response.put("isEvil", isEvil ? "true" : "false");
		} else {
			
		}
		
		return ResponseEntity.ok(response);
	}
	
	/**
	 * Post mapping to make a choice
	 * 
	 * Records the choice to the session records, updates the model and its metrics, pushes to the admin stats records (websocket WIP)
	 * @param choice the user's choice
	 * @param sessionId the session id
	 * @param trialNum the trial number within that session
	 * @return A response entity to show completion
	 */
	@PostMapping("/submit/{sessionId}/{trialNum}")
	public ResponseEntity<String> submitChoice(@RequestBody String choice, @PathVariable String sessionId, @PathVariable String trialNum) {
		System.out.println(choice);
		
		Session thisSession = Server.ALL_SESSIONS.get(sessionId);
		FinalModel thisModel = Server.ALL_MODELS.get(sessionId);
		
		Response newResponse = new Response(choice.contains("eat"), thisSession.currentImageEvil, System.nanoTime() - thisSession.lastLoadTime);
		
		thisSession.responses.add(newResponse);
		
		System.out.println(thisModel.getCertainty(thisSession.currentImage));
		
		thisModel.numAnswered++;
		if ((thisModel.getCertainty(thisSession.currentImage) > 50) == (choice.equals("{\"choice\":\"discard\"}"))) {
			System.out.println("Increasing numCorrect");
			thisModel.numCorrect++;
		}
		
		thisSession.currentImageNum = Integer.parseInt(trialNum) + 1;
		thisModel.updateWeights(thisSession.currentImage, thisSession.currentImageEvil);
		
		// JsonNode updatedStats = LiveStats.getUpdatedStats();
		
		return ResponseEntity.ok("{\"Why\": \"hello there :P\"}");
	}
}