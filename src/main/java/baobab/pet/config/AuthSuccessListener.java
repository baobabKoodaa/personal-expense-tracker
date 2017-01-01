package baobab.pet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    BruteForceDetector bruteForceDetector;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        WebAuthenticationDetails auth = (WebAuthenticationDetails) event.getAuthentication().getDetails();
        String ip = auth.getRemoteAddress();
        String username = event.getAuthentication().getName();
        bruteForceDetector.successfulLogin(ip, username);
    }
}
