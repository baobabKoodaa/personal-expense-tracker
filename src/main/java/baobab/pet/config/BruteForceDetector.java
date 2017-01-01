package baobab.pet.config;

import org.springframework.stereotype.Component;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Allow 100 failed login attempts per 30 days per IP.
 *   When the amount has exceeded, allow logins only to accounts
 *   which have been succesfully accessed by this IP before. In this case:
 *     Allow 100 failed login attempts per day per IP-username-combo.
 *     After a succesful login:
 *       Clear failed attempts for IP-username combo.
 *       Don't clear failed attempts for ip.
 */
@Component
public class BruteForceDetector {

    private final int MAX_FAILURES = 100;
    private LoadingCache<String, Integer> failedAttemptsForIp;
    private LoadingCache<String, Integer> failedAttemptsForIpUserCombo;
    private TreeSet<String> previouslyAcceptedUserIpCombos;

    public BruteForceDetector() {
        failedAttemptsForIp = initCache(30);
        failedAttemptsForIpUserCombo = initCache(1);
        previouslyAcceptedUserIpCombos = new TreeSet<>();
    }

    public void successfulLogin(String ip, String username) {
        previouslyAcceptedUserIpCombos.add(ip+username);
        failedAttemptsForIpUserCombo.invalidate(ip+username);
    }

    public void failedLogin(String ip, String username) {
        rememberFailure(failedAttemptsForIp, ip);
        rememberFailure(failedAttemptsForIpUserCombo, ip+username);
    }

    private void rememberFailure(LoadingCache<String, Integer> cache, String key) {
        try {
            int earlierAttempts = cache.get(key);
            cache.put(key, earlierAttempts + 1);
        } catch (ExecutionException ex) {
            cache.put(key, 1);
        }
    }

    public boolean isBlocked(String ip, String username) {
        try {
            if (previouslyAcceptedUserIpCombos.contains(ip+username)) {
                return failedAttemptsForIpUserCombo.get(ip+username) >= MAX_FAILURES;
            } else {
                return failedAttemptsForIp.get(ip) >= MAX_FAILURES;
            }
        } catch (ExecutionException e) {
            return false;
        }
    }

    private LoadingCache<String, Integer> initCache(int days) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(days, TimeUnit.DAYS)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }
}