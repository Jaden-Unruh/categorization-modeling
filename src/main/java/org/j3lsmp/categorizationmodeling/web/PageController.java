package org.j3lsmp.categorizationmodeling.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageController {
	
	@RequestMapping(value = { "/", "/{x:^(?!api|_app|.*\\..*).*$}", "/{x:^(?!api|_app|.*\\..*).*$}/**" })
    public String forwardToFrontend() {
        return "forward:/index.html";
    }
}