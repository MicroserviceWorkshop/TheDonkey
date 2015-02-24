package thedonkey;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MyController {

	@RequestMapping("/login")
	public String login() {
		return "login";
	}

	@RequestMapping("/hello")
	public String hello(Model model) {
		SecurityContext context = SecurityContextHolder.getContext();
		model.addAttribute("authentication", context.getAuthentication());
		return "hello";
	}

	@RequestMapping("/user")
	public String show(Model model, AbstractAuthenticationToken authentication) {
		model.addAttribute("authentication", authentication);

		if (authentication instanceof OpenIDAuthenticationToken) {
			return "user";
		}

		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			return "hello";
		}

		return "error";
	}
}
