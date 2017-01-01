package baobab.pet.config;

import org.springframework.stereotype.Component;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Allow 100 failed login attempts per day per IP.
 *   When the amount has exceeded, allow logins only to accounts
 *   which have been succesfully accessed by this IP before.
 *     In this case, allow 100 failed login attempts per day per IP-username-combo.
 */
@Component
public class BruteForceDetector {

    private final int MAX_FAILURES = 100;
    private LoadingCache<String, Integer> failedAttemptsForIp;
    private LoadingCache<String, Integer> failedAttemptsForIpUserCombo;
    private TreeSet<String> previouslyAcceptedUserIpCombos;

    public BruteForceDetector() {
        failedAttemptsForIp = initCache();
        failedAttemptsForIpUserCombo = initCache();
        previouslyAcceptedUserIpCombos = new TreeSet<>();
    }

    public void successfulLogin(String ip, String username) {
        previouslyAcceptedUserIpCombos.add(ip + username);
    }

    public void failedLogin(String ip, String username) {
        rememberFailure(failedAttemptsForIp, ip);
        rememberFailure(failedAttemptsForIpUserCombo, ip+username);
    }

    private void rememberFailure(LoadingCache<String, Integer> cache, String key) {
        int attempts = 0;
        try {
            attempts = cache.get(key);
        } catch (ExecutionException ex) {
            attempts = 0;
        }
        attempts++;
        failedAttemptsForIp.put(key, attempts);
    }

    public boolean isBlocked(String ip, String username) {
        try {
            if (previouslyAcceptedUserIpCombos.contains(ip + username)) {
                return failedAttemptsForIpUserCombo.get(ip + username) >= MAX_FAILURES;
            } else {
                return failedAttemptsForIp.get(ip) >= MAX_FAILURES;
            }
        } catch (ExecutionException e) {
            return false;
        }
    }

    private LoadingCache<String, Integer> initCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }
}