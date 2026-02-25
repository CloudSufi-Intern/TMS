package cloudsufi.nextgen.tms.dto.request;

import lombok.*;

// @author Yashas Yadav

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    private String name;
    private String email;
    private String password;
}
