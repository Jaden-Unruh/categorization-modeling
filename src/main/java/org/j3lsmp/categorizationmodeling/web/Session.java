package org.j3lsmp.categorizationmodeling.web;

import java.util.ArrayList;

public class Session {
	
	public String sessionId;
	public String firstName, lastName, email, pronouns;
	
	int currentImageNum = 0;
	
	ArrayList<Response> responses = new ArrayList<>();
	
	boolean[] currentImage = {false, false, false, false};
	
	Session(String id, String firstName, String lastName, String email, String pronouns) {
		this.sessionId = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.pronouns = pronouns;
	}
	
	void setImage(String imageCode) {
		for (int i = 0; i < 4; i++)
			currentImage[i] = imageCode.charAt(i) == '1';
	}
}

record Response(boolean response, boolean isActuallyEvil, long timeTaken) {}