package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.UserRequestDTO;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * Author: Smriti Bajpai
 * Description:
 * REST Controller responsible for handling user related APIs.
 *
 * Endpoint:
 * POST /api/users
 *
 * Responsibilities:
 * - Accept user creation requests
 * - Validate request body
 * - Deliver business logic to service layer
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserEntity> addUser(@Valid @RequestBody UserRequestDTO request) {

        UserEntity user = userService.addUser(request);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}