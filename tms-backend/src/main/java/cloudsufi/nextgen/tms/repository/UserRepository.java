package cloudsufi.nextgen.tms.repository;
/**
 * Repository representing a User in the SQL Database
 * @author Ansh Parnami
 **/
import cloudsufi.nextgen.tms.dto.UserSuggestionDTO;
import cloudsufi.nextgen.tms.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/*
 * Author: Smriti Bajpai
 *
 * Description:
 * Repository responsible for database interaction for UserEntity.
 *
 * Responsibilities:
 * - CRUD operations
 * - Duplicate email checks
 * - Query user by username or email
 */

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    /**
     * Finds users whose usernames start with the specified prefix.
     *
     * The search is case-insensitive and supports pagination.
     * Only the user ID and username are returned through {@link UserSuggestionDTO}.
     *
     * @param username The username prefix used for searching.
     * @param pageable Pagination information including page number and page size.
     *
     * @return A paginated list of {@link UserSuggestionDTO} containing matching users.
     * @author vishwasvaidya
     */
    @Query("""
        SELECT new cloudsufi.nextgen.tms.dto.UserSuggestionDTO(u.id, u.username)
        FROM UserEntity u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT(:username, '%'))
        """)
    Page<UserSuggestionDTO> searchUsers(String username, Pageable pageable);
}
