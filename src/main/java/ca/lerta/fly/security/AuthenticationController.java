package ca.lerta.fly.security;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import ch.qos.logback.classic.Logger;

@Controller
public class AuthenticationController {
  // private static final String SECURITY_CONTEXT_REPOSITORY = "SECURITY_CONTEXT_REPOSITORY";

  Logger logger = (Logger) LoggerFactory.getLogger(AuthenticationController.class);

  @Autowired
  AuthenticationManager authenticationManager;

  public void authenticate() {
    FlyAuth flyAuth = new FlyAuth();
    Map<String, String> map = flyAuth.createSession();
    String authUrl = map.get("auth_url");
    logger.info("opening login page {}", authUrl);
    UI ui = UI.getCurrent();
    new Thread(() -> {
      ui.access(() -> {
        ui.getPage().open(authUrl);
      });
    }).start();

    AuthenticationManager authenticationManagerBean = SecurityConfiguration.authenticationManagerBean;
    SecurityContext context = SecurityContextHolder.getContext();
    // HttpServletRequest httpServletRequest =
    // VaadinServletRequest.getCurrent().getHttpServletRequest();
    // HttpServletResponse httpServletResponse =
    // VaadinServletResponse.getCurrent().getHttpServletResponse();

    flyAuth.waitForTokenString(
        map.get("id"),
        (token) -> {
          logger.info("Access token retrieved {}", token);
          flyAuth.setAccessToken(token);
          VaadinSession session = ui.getSession();
          session.access(() -> {
            session.setAttribute("ACCESS_TOKEN", token);
          });
        });

    Authentication result = authenticationManagerBean
        .authenticate(new UsernamePasswordAuthenticationToken("user", "user"));
    context.setAuthentication(result);
    logger.info("authentication changed");
    // this.securityContextRepository(ui.getSession()).saveContext(context,
    // httpServletRequest, httpServletResponse);
  }

  // SecurityContextRepository scr = null;

  // private SecurityContextRepository securityContextRepository(VaadinSession
  // session) {
  // session.access(() -> {
  // scr = (SecurityContextRepository)
  // session.getAttribute(SECURITY_CONTEXT_REPOSITORY);
  // if (scr == null) {
  // scr = new HttpSessionSecurityContextRepository();
  // session.setAttribute(SECURITY_CONTEXT_REPOSITORY, scr);
  // }
  // });
  // return scr;
  // }

  public void logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
  }

}
