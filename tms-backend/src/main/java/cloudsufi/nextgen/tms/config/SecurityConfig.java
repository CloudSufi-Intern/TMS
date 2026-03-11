package cloudsufi.nextgen.tms.config;

import cloudsufi.nextgen.tms.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * This class configures Spring Security for the TMS application.
 * It defines the core security filter chain and integrates the custom
 * JWT authentication filter. The configuration ensures that the application
 * follows stateless authentication using JWT tokens.
 * Default Spring Security login forms and HTTP basic authentication
 * are disabled since authentication is strictly handled via JWT.
 *
 * @author Ansh Parnami
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the HTTP security rules for the application.
     * Disables CSRF, sets session management to stateless, permits the
     * signup and login endpoints publicly, and requires authentication
     * for all other requests.
     *
     * @param http the {@link HttpSecurity} object to modify.
     * @return the fully configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs while building the security configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
                .formLogin(form->form.disable())
                .httpBasic(basic->basic.disable())
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth-> auth
                        .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Provides a BCrypt password encoder bean for hashing passwords on sign-up
     * and verifying them on login.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     * @author Yashas Yadav
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
