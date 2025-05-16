package org.j3lsmp.categorizationmodeling.web;

/**
 * Passed when a user requests a session with given details
 * @param firstName the user's name
 * @param lastName the user's last name
 * @param email the user's email
 * @param pronouns the user's pronouns
 */
public record SessionRequest(String firstName, String lastName, String email, String pronouns) {}