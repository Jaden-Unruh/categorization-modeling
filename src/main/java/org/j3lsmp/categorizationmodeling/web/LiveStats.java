package org.j3lsmp.categorizationmodeling.web;

import org.j3lsmp.categorizationmodeling.FinalModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON builder for stats as viewed by admins
 */
class LiveStats {
	/**
	 * Builds a big JSON file to populate the admin stats table, using current information
	 * @return the root json node of the stats file
	 */
	static JsonNode getUpdatedStats() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		for (String key : Server.ALL_MODELS.keySet()) {
			ObjectNode child = mapper.createObjectNode();
			child.put("firstName", Server.ALL_SESSIONS.get(key).firstName);
			child.put("lastName", Server.ALL_SESSIONS.get(key).lastName);
			child.put("email", Server.ALL_SESSIONS.get(key).email);
			child.put("pronouns", Server.ALL_SESSIONS.get(key).pronouns);
			child.put("currentImage", FinalModel.strFromTraits(Server.ALL_SESSIONS.get(key).currentImage));
			child.put("currentImageNum", Server.ALL_SESSIONS.get(key).currentImageNum);
			child.put("modelCorrect", Server.ALL_MODELS.get(key).numCorrect);
			child.put("prediction", Server.ALL_MODELS.get(key).getCertainty(Server.ALL_SESSIONS.get(key).currentImage));
			
			rootNode.set(key, child);
		}
		
		return rootNode;
	}
}