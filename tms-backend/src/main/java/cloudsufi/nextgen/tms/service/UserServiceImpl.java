package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.UserRequestDTO;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.exception.DuplicateUserException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/*
 * Author: Smriti Bajpai
 * Description:
 * Implementation of UserService containing business logic.
 * Responsibilities:
 * - Validate user input
 * - Prevent duplicate email
 * - Prevent duplicate username
 * - Convert DTO to Entity
 * - Save user to database
 */

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserEntity addUser(UserRequestDTO request) {

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }

        if(userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username already exists");
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNo(request.getPhoneNo())
                .password(request.getPassword())
                .role(request.getRole())
                .build();

        return userRepository.save(user);
    }
}