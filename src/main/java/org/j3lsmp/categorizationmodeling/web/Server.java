package org.j3lsmp.categorizationmodeling.web;

import java.util.HashMap;
import java.util.Map;

import org.j3lsmp.categorizationmodeling.FinalModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class Server {
	
	static final Map<String, Session> ALL_SESSIONS = new HashMap<>();
	static final Map<String, FinalModel> ALL_MODELS = new HashMap<>();
	
	public static void main(String[] args) {
		SpringApplication.run(Server.class, args);
	}
	
	@PreDestroy
	public void onShutdown() {
		//TODO copy all data to file
		System.out.println("Shutting down and doing stuff...");
	}
}