package ca.lerta.fly.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.spring.security.VaadinWebSecurityConfigurerAdapter;

import ca.lerta.fly.views.flylogin.FlyOpenerView;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurityConfigurerAdapter {

    public static final String LOGOUT_URL = "/";
    static AuthenticationManager authenticationManagerBean;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        setLoginView(http, FlyOpenerView.class, LOGOUT_URL);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        web.ignoring().antMatchers("/images/*.png");
    }

    @Override
    @Bean
    @SuppressWarnings("deprecation")
    // deprecated because Vaadin adapter extends deprecated class
    public AuthenticationManager authenticationManagerBean() throws Exception {
        System.err.println("authenticationManagerBean " + super.authenticationManagerBean().toString());
        authenticationManagerBean = super.authenticationManagerBean();
        return super.authenticationManagerBean();
    }

}
