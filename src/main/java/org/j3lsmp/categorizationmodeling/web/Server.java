package org.j3lsmp.categorizationmodeling.web;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.j3lsmp.categorizationmodeling.FinalModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PreDestroy;

/**
 * The primary spring boot application and entry method
 */
@SpringBootApplication
public class Server {
	
	/**
	 * A Map of all current sessions by their ID
	 */
	static final Map<String, Session> ALL_SESSIONS = new HashMap<>();
	
	/**
	 * A Map of each sessions live model by session ID
	 */
	static final Map<String, FinalModel> ALL_MODELS = new HashMap<>();
	
	/**
	 * Entry method of Categorization Modeling Project - Web
	 * @param args passed to Spring
	 */
	public static void main(String[] args) {
		SpringApplication.run(Server.class, args);
	}
	
	/**
	 * DTF used for making file on system exit
	 */
	static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
	
	/**
	 * Run during safe shutdown before termination. Saves all session data to an output json file for later access
	 */
	@PreDestroy
	public void onShutdown() {
		try {
			String filename = "catModelingOutput_" + LocalDateTime.now().format(DTF) + ".json";
			String path = "E:\\Categorization modeling project\\web outputs";
			
			File outputFile = new File(path, filename);
			
			outputFile.createNewFile();
			
			Session.MAPPER.writeValue(outputFile, Session.parseSessions());
		} catch (IOException e) {
			//TODO: File writing failed - try sending email? Or another output method. Too much to print to console, probably.
			System.out.println("Writing failed");
			e.printStackTrace();
		}
	}
}