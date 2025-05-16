package org.j3lsmp.categorizationmodeling.web;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Holds data for a current session
 */
public class Session {
	
	/**
	 * This session's ID number (6 digits, stored as a string for ease)
	 */
	public String sessionId;
	
	/**
	 * Information about the user
	 */
	@SuppressWarnings("javadoc")
	public String firstName, lastName, email, pronouns;
	
	/**
	 * Mapper used for JSON construction
	 */
	static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * The index of image the user is currently evaluating (NOT 4 digit binary image ID)
	 */
	int currentImageNum = 0;
	
	/**
	 * List of previous responses by the user in this session
	 */
	ArrayList<Response> responses = new ArrayList<>();
	
	/**
	 * The nanoTime at which the user loaded the current image
	 */
	long lastLoadTime = System.nanoTime();
	
	/**
	 * The boolean traits of the image the user is looking at
	 */
	boolean[] currentImage = {false, false, false, false};
	
	/**
	 * Whether or not the current image is actually evil/edible
	 */
	boolean currentImageEvil = false;
	
	/**
	 * Basic constructor, sets up a session with the given details
	 * @param id session id
	 * @param firstName user's first name
	 * @param lastName user's last name
	 * @param email user's email
	 * @param pronouns user's pronouns
	 */
	Session(String id, String firstName, String lastName, String email, String pronouns) {
		this.sessionId = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.pronouns = pronouns;
	}
	
	/**
	 * Sets the live image given a 4 digit binary image id
	 * @param imageCode 4 digit image id
	 */
	void setImage(String imageCode) {
		for (int i = 0; i < 4; i++)
			currentImage[i] = imageCode.charAt(i) == '1';
	}
	
	/**
	 * Returns a json of all the sessions held on the server for use in shutdown to write to file
	 * @return the root node of a json that holds all session information
	 */
	static JsonNode parseSessions() {
		ArrayNode rootNode = MAPPER.createArrayNode();
		for (String sessionId : Server.ALL_SESSIONS.keySet()) {
			Session thisSession = Server.ALL_SESSIONS.get(sessionId);
			
			ObjectNode sessionNode = MAPPER.createObjectNode();
			sessionNode.put("id", sessionId)
				.put("firstName", thisSession.firstName)
				.put("lastName", thisSession.lastName)
				.put("email", thisSession.email)
				.put("pronouns", thisSession.pronouns);
			
			ArrayNode sessionTrials = MAPPER.createArrayNode();
			for (Response res : thisSession.responses) {
				ObjectNode trialNode = MAPPER.createObjectNode();
				trialNode.put("response", res.response())
					.put("reality", res.isActuallyEvil())
					.put("time_ns", res.timeTaken());
				
				sessionTrials.add(trialNode);
			}
			sessionNode.set("Trials", sessionTrials);
			rootNode.add(sessionNode);
		}
		return rootNode;
	}
}

/**
 * Holds data from a response
 */
record Response(boolean response, boolean isActuallyEvil, long timeTaken) {}