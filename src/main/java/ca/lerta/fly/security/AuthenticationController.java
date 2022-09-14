package ca.lerta.fly.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;

@Controller
public class AuthenticationController
{
  @Autowired
  AuthenticationManager authenticationManager;

  //@Autowired
  //SecurityContextRepository securityContextRepository;

  public Authentication authenticate(String username, String password, HttpServletRequest request, HttpServletResponse response)
  {
    //FIXME: check that login to fly.io has succeeded.

    // authenticate the dummy user so the rest of the pages is done.
    Authentication result = SecurityConfiguration.authenticationManagerBean.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    SecurityContextHolder.getContext().setAuthentication(result);

    //this.securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
    return result;
  }

  public void logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {    
        new SecurityContextLogoutHandler().logout(request, response, auth);
    }
  }

}
