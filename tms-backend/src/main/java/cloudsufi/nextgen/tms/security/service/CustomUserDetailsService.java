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


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        throw new UsernameNotFoundException("user lookup not implemented yet");

    }
}
