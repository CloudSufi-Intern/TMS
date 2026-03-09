package cloudsufi.nextgen.tms.security.service;

import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
/*
 * CustomUserDetailsService
 *
 * Service responsible for loading user-specific data
 * required for authentication. It implements
 * Spring Security's UserDetailsService interface.
 *
 * Author: Priyanshu gupta
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with email:" + username)
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();

    }
}
