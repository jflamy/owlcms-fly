package ca.lerta.fly.security;

import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;

import ca.lerta.fly.views.flylogin.FlyLoginOpenerView;
import ch.qos.logback.classic.Logger;

public interface TokenAuthentication extends BeforeEnterObserver {

    Logger logger = (Logger) LoggerFactory.getLogger(TokenAuthentication.class);

    public default void checkToken(BeforeEnterEvent event) {
        String token = (String) VaadinSession.getCurrent().getAttribute("ACCESS_TOKEN");
        if (token != null) {
            // authenticate as user
            // this must take place in the main request thread, otherwise the Authentication
            // does not get stored and associated with the session.
            Authentication result = SecurityConfiguration.getAuthentificationManager()
                    .authenticate(new UsernamePasswordAuthenticationToken("user", "user"));
            SecurityContextHolder.getContext().setAuthentication(result);
            return;
        }
        event.forwardTo(FlyLoginOpenerView.class);
    }

}
