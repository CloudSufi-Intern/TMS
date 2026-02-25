package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.request.UserRequest;
import cloudsufi.nextgen.tms.dto.response.UserResponse;
import cloudsufi.nextgen.tms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @author Yashas Yadav

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User API")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Add a new user")
    public ResponseEntity<UserResponse> addUser(@RequestBody UserRequest request) {
        return null;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return null;
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return null;
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name or email")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        return null;
    }
}
