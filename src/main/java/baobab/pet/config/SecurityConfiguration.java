package baobab.pet.config;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private Environment environment;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        String rememberMeKey = "this-key-only-in-development";
        if (profileIsProduction()) {
            /* Remember-me token needs a private key with high entropy in order to stop
            *  token-based brute force attacks on weak user passwords. In production
            *  we read the key from environment variable PETREMEMBERMEKEY. */
            rememberMeKey = System.getenv("PETREMEMBERMEKEY");
            if (rememberMeKey == null || rememberMeKey.length() < 12) {
                throw new RuntimeException("Invalid remember-me key! In production a key of at least " +
                        "length 12 is expected from environment variable PETREMEMBERMEKEY");
            }

            /* Uncomment to force HTTPS in production. */
            // http.requiresChannel().anyRequest().requiresSecure();
        }

        http
            .authorizeRequests()
                .antMatchers("/templates/**, /scripts/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll()
                .and()
            .rememberMe()
                .key(rememberMeKey)
                .tokenValiditySeconds(1209600);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private boolean profileIsProduction() {
        for(String profile : environment.getActiveProfiles()) {
            if (profile.equals("production")) {
                return true;
            }
        }
        return false;
    }

}
