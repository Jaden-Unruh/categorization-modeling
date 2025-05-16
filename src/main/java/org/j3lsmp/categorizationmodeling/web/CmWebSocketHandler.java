package org.j3lsmp.categorizationmodeling.web;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Fairly default websockethandler. Be advised, I still haven't figured out websockets and this doesn't work.
 */
@Component
public class CmWebSocketHandler extends TextWebSocketHandler {
	
	/**
	 * {@inheritDoc}
	 * Sends a welcome message and prints the session id
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("Connected: " + session.getId());
		session.sendMessage(new TextMessage("Welcome"));
	}
	
	/**
	 * {@inheritDoc}
	 * Echoes a received text message and prints to console
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		System.out.println("Received: " + message.getPayload());
		session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
	}
	
	/**
	 * {@inheritDoc}
	 * Prints the error to console
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		System.err.println("Transport error: " + exception.getMessage());
	}
	
	/**
	 * P{@inheritDoc}
	 * Prints to console
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		System.out.println("Connection closed: " + session.getId());
	}
	
	/**
	 * {@inheritDoc}
	 * Returns false (no support for partial messages)
	 * @return false
	 */
	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}
