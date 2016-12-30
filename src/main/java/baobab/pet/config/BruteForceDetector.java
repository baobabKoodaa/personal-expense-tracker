package baobab.pet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class BruteForceDetector implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final int MAX_ATTEMPT = 100;
    private LoadingCache<String, Integer> attemptsCache;

    public BruteForceDetector() {
        attemptsCache = CacheBuilder.newBuilder()
                                    .expireAfterWrite(1, TimeUnit.DAYS)
                                    .build(new CacheLoader<String, Integer>() {
                                        public Integer load(String key) {
                                            return 0;
                                        }
                                    });
    }

    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        WebAuthenticationDetails auth = (WebAuthenticationDetails) e.getAuthentication().getDetails();
        String ip = auth.getRemoteAddress();
        int attempts = 0;
        try {
            attempts = attemptsCache.get(ip);
        } catch (ExecutionException ex) {
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(ip, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (ExecutionException e) {
            return false;
        }
    }
}