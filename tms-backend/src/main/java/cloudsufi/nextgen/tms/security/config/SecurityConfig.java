package cloudsufi.nextgen.tms.security.config;

import cloudsufi.nextgen.tms.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                //disable CSRF coz this application exposes REST APIs
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())

                //disabled deafult spring security login form since authentication will be handled using JWT
                .formLogin(form->form.disable())

                //disabled http basic authentication
                .httpBasic(basic->basic.disable())

                //making application stateless no http sessions , every req must contain a valid jwt token
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //configuerd authorization rules
                //for now all endpoints require auhtentication
                // login/register endpointsa can be permmited later
                .authorizeHttpRequests(auth-> auth.anyRequest().permitAll())

                //registering jwt filter before spring's default authentication flter
                //ensures  jwt validation happens early in the filter chain
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
