package cloudsufi.nextgen.tms;

import cloudsufi.nextgen.tms.dto.UserSuggestionDTO;
import cloudsufi.nextgen.tms.repository.UserRepository;
import cloudsufi.nextgen.tms.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserService}.
 *
 * This test class verifies the functionality of user search
 * by mocking the {@link UserRepository}.
 * @author vishwasvaidya
 */
class TmsApplicationTests {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	public TmsApplicationTests() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testSearchUsers() {
		UserSuggestionDTO dto = new UserSuggestionDTO(1L,"john");

		Page<UserSuggestionDTO> page = new PageImpl<>(List.of(dto));
		when(userRepository.searchUsers(eq("jo"), any(Pageable.class))).thenReturn(page);

		Page<UserSuggestionDTO> result = userService.searchUsers("jo",0,10);
		assertEquals(1, result.getContent().size());
	}
}
