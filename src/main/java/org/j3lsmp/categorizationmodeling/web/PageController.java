package org.j3lsmp.categorizationmodeling.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Primary controller for pages - forwards all front-end pages to index, and everything else gets handled by relevant controllers
 */
@Controller
public class PageController {

	/**
	 * No-op on ws connections
	 */
	@RequestMapping("/ws")
	public void ws() {
		// no-op
	}

	/**
	 * forward pages to index.html
	 * @return "forward:/index.html"
	 */
	@RequestMapping({ "/", "/admin/login", "/admin", "/about", "/resume", "/start", "/session/{sessionId}" })
	public String forwardToFrontend() {
		return "forward:/index.html";
	}
}