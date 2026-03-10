package cloudsufi.nextgen.tms.repository;
/**
 * Repository representing a User in the SQL Database
 * @author Ansh Parnami
 **/
import cloudsufi.nextgen.tms.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}