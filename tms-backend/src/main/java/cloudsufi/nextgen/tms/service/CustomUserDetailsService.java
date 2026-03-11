package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service responsible for loading user-specific data required for authentication.
 *
 * It implements Spring Security's {@link UserDetailsService} interface to fetch
 * user details from the database during the authentication process.
 *
 * @author Ansh Parnami
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Locates the user based on the username (which is mapped to the user's email).
     * This method retrieves the {@link UserEntity} from the database, extracts their
     * assigned role, and constructs a Spring Security {@link UserDetails} object
     * that is used for authorization checks.
     * @param username the email identifying the user whose data is to be retrieved.
     * @return a fully populated {@link UserDetails} record containing the user's credentials and authorities.
     * @throws UsernameNotFoundException if the user could not be found in the database.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with email:" + username)
                );
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authority)
                .build();
    }
}
