package cloudsufi.nextgen.tms.dto.response;

import lombok.*;

// @author Yashas Yadav

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
}
