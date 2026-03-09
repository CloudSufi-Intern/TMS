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

/*
 * SecurityConfig
 *
 * This class configures Spring Security for the TMS application.
 * It defines the security filter chain, authentication manager,
 * password encoder, and integrates the JWT authentication filter.
 *
 * The configuration ensures that the application follows
 * stateless authentication using JWT tokens.
 * disabled default spring security login form since authentication will be handled using JWT
 *
 * Author: Priyanshu Gupta
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http

                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())

                .formLogin(form->form.disable())


                .httpBasic(basic->basic.disable())


                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth-> auth.anyRequest().permitAll())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
