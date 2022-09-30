package ca.lerta.fly.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

import ca.lerta.fly.views.flylogin.FlyLoginOpenerView;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    public static final String LOGOUT_URL = "/";
    public static AuthenticationManager authentificationManager;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        setLoginView(http, FlyLoginOpenerView.class, LOGOUT_URL);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        web.ignoring().antMatchers("/images/*.png");
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        // for some reason cannot autowire this as a property in other classes, workaround
        SecurityConfiguration.authentificationManager = authenticationConfiguration.getAuthenticationManager();
        return authenticationConfiguration.getAuthenticationManager();
    }
}
