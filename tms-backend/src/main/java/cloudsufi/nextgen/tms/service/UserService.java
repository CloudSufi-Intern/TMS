package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.UserRequestDTO;
import cloudsufi.nextgen.tms.entity.UserEntity;

/*
 * Author: Smriti Bajpai
 * Description:
 * Service interface defining business operations for user management.
 */

public interface UserService {

    UserEntity addUser(UserRequestDTO request);

}