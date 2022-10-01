package ca.lerta.fly.security;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinSession;

import ch.qos.logback.classic.Logger;

@Controller
public class AuthenticationController {

  Logger logger = (Logger) LoggerFactory.getLogger(AuthenticationController.class);

  public void authenticate(Command callback, boolean signUp) throws AuthenticationException, Exception {
    UI ui = UI.getCurrent();

    FlyAuth flyAuth = new FlyAuth();
    Map<String, String> map = flyAuth.createSession(signUp);
    String authUrl = map.get("auth_url");
    flyAuth.openFlyLogin(authUrl, ui);

    flyAuth.waitForTokenString(
        map.get("id"),
        (token) -> {
          logger.info("Access token retrieved {}", token);
          flyAuth.setAccessToken(token);
          VaadinSession session = ui.getSession();
          session.access(() -> {
            session.setAttribute("ACCESS_TOKEN", token);
          });
          callback.execute();
        });
  }

  public void logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
  }

}
