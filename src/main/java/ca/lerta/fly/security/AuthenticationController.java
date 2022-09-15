package ca.lerta.fly.security;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinSession;

import ch.qos.logback.classic.Logger;

@Controller
public class AuthenticationController {
  // private static final String SECURITY_CONTEXT_REPOSITORY = "SECURITY_CONTEXT_REPOSITORY";

  Logger logger = (Logger) LoggerFactory.getLogger(AuthenticationController.class);

  @Autowired
  AuthenticationManager authenticationManager;

  public void authenticate(Command c) {
    UI ui = UI.getCurrent();

    FlyAuth flyAuth = new FlyAuth();
    Map<String, String> map = flyAuth.createSession();
    String authUrl = map.get("auth_url");
    flyAuth.openFlyLogin(authUrl, ui);

    // this must take place in the main request thread, otherwise the authentication does not get
    // stored and associated with the session.
    Authentication result = SecurityConfiguration.authenticationManagerBean
        .authenticate(new UsernamePasswordAuthenticationToken("user", "user"));
    SecurityContextHolder.getContext().setAuthentication(result);

    flyAuth.waitForTokenString(
        map.get("id"),
        (token) -> {
          logger.info("Access token retrieved {}", token);
          flyAuth.setAccessToken(token);
          VaadinSession session = ui.getSession();
          session.access(() -> {
            session.setAttribute("ACCESS_TOKEN", token);
          });
          c.execute();
        });
  }


  public void logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
  }

}
