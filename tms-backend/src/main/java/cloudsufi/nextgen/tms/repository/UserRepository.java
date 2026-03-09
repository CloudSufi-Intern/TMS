package cloudsufi.nextgen.tms.repository;
import java.util.Optional;

/**
 * Repository interface for accessing User data from databse
 *
 * author : priyanshu gupta
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User>findByEmail(String email);
}
