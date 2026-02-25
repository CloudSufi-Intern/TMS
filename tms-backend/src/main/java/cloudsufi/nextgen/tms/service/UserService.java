package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.request.UserRequest;
import cloudsufi.nextgen.tms.dto.response.UserResponse;

import java.util.List;

// @author Yashas Yadav

public interface UserService {

    UserResponse addUser(UserRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    List<UserResponse> searchUsers(String keyword);
}
