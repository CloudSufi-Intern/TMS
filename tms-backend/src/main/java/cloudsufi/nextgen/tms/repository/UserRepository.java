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
     * Finds users whose usernames start with the specified prefix using a native SQL query.
     *
     * The search supports pagination. Results are returned as a read-only interface projection
     * {@link UserSuggestionDTO}, containing only the user ID and username.
     *
     * This method leverages a database index on the "username" column for fast lookup.
     *
     * @param username The username prefix used for searching.
     *                 Must be at least 2 characters long.
     * @param pageable Pagination information including page number and page size.
     * @return A paginated list of {@link UserSuggestionDTO} containing matching users.
     * @see UserSuggestionDTO
     * @author vishwasvaidya
     */
    @Query(
            value = """
                SELECT u.id AS id, u.username AS username
                FROM users u
                WHERE u.username LIKE CONCAT(:username, '%')
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM users u
                WHERE u.username LIKE CONCAT(:username, '%')
                """,
            nativeQuery = true
    )
    Page<UserSuggestionDTO> searchUsers(String username, Pageable pageable);
}