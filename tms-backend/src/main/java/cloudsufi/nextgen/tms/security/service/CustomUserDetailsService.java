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

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    //repository used to fetch data from the databse


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
            // fetch user from database
        //placeholder implemnetation
        //real user lookup will be implemented once authentication apis are added

        throw new UsernameNotFoundException("user lookup not implemented yet");

    }
}
