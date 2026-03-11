package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.LoginRequestDTO;
import cloudsufi.nextgen.tms.dto.LoginResponseDTO;
import cloudsufi.nextgen.tms.dto.SignUpRequestDTO;
import cloudsufi.nextgen.tms.dto.SignUpResponseDTO;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.exception.AuthenticationException;
import cloudsufi.nextgen.tms.exception.BadRequestException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import cloudsufi.nextgen.tms.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for handling authentication operations.
 * Handles user sign-up with BCrypt password encryption and
 * JWT token generation upon successful login.
 *
 * @author Yashas Yadav
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Registers a new user with a BCrypt-encrypted password.
     *
     * @param request DTO containing sign-up details
     * @return the saved user details as {@link SignUpResponseDTO}
     * @throws BadRequestException if email or username already exists
     */
    public SignUpResponseDTO signUp(SignUpRequestDTO request) {

        log.info("Received sign-up request for username: {} and email: {}",
                request.getUsername(), request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Sign-up failed: Email already exists -> {}", request.getEmail());
            throw new BadRequestException("User with this email already exists.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Sign-up failed: Username already exists -> {}", request.getUsername());
            throw new BadRequestException("User with this username already exists.");
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNo(request.getPhoneNo())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        UserEntity savedUser = userRepository.save(user);

        log.info("User registered successfully with id: {}", savedUser.getId());

        return SignUpResponseDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .phoneNo(savedUser.getPhoneNo())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    /**
     * Authenticates a user and returns a JWT token valid for 15 minutes.
     *
     * @param request DTO containing login credentials
     * @return {@link LoginResponseDTO} containing the signed JWT token
     * @throws AuthenticationException if email is not found or password does not match
     */
    public LoginResponseDTO login(LoginRequestDTO request) {

        log.info("Received login request for email: {}", request.getEmail());

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: No account found for email -> {}", request.getEmail());
                    return new AuthenticationException("Invalid email or password.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Incorrect password for email -> {}", request.getEmail());
            throw new AuthenticationException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        log.info("Login successful for email: {}", request.getEmail());

        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .build();
    }
}
