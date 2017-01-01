package baobab.pet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthFailListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    BruteForceDetector bruteForceDetector;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        WebAuthenticationDetails auth = (WebAuthenticationDetails) event.getAuthentication().getDetails();
        String ip = auth.getRemoteAddress();
        String username = event.getAuthentication().getName();
        bruteForceDetector.failedLogin(ip, username);
    }
}
