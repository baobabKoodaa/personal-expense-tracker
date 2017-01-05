package baobab.pet.config;

import baobab.pet.data.domain.Book;
import baobab.pet.data.domain.User;
import baobab.pet.data.DAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Service
public class LoginService implements UserDetailsService {

    @Autowired
    private DAO dao;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private BruteForceDetector bruteForceDetector;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String ip = request.getRemoteAddr();
        if (bruteForceDetector.isBlocked(ip, username)) {
            throw new RuntimeException("IP address temporarily blocked for too many password attempts.");
        }

        User user = dao.findUserByName(username);
        if (user == null || !user.isCurrent()) {
            throw new UsernameNotFoundException("No such user: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getName(),
                user.getEncodedPassword(),
                true,
                true,
                true,
                true,
                Arrays.asList(new SimpleGrantedAuthority(user.getRole())));
    }

}
