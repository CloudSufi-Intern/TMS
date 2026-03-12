package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.LoginRequestDTO;
import cloudsufi.nextgen.tms.dto.LoginResponseDTO;
import cloudsufi.nextgen.tms.dto.SignUpRequestDTO;
import cloudsufi.nextgen.tms.dto.SignUpResponseDTO;
import cloudsufi.nextgen.tms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for handling authentication operations in the Task Management System.
 * Provides endpoints for user sign-up and login.
 *
 * @author Yashas Yadav
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user. Password is BCrypt-encrypted before being stored.
     *
     * @param request DTO containing sign-up details
     * @return ResponseEntity containing the created {@link SignUpResponseDTO}
     */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDTO> signUp(@Valid @RequestBody SignUpRequestDTO request) {

        log.info("Received sign-up request for username: {} and email: {}",
                request.getUsername(), request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signUp(request));
    }

    /**
     * Authenticates a user and returns a JWT token valid for 15 minutes.
     *
     * @param request DTO containing login credentials
     * @return ResponseEntity containing the {@link LoginResponseDTO} with the JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {

        log.info("Received login request for email: {}", request.getEmail());

        return ResponseEntity.ok(authService.login(request));
    }
}
