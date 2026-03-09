package cloudsufi.nextgen.tms.repository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User>findByEmail(String email);
}
