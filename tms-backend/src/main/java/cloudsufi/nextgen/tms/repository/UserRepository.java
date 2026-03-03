package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String s);
    Optional<UserEntity> findByEmail(String s);
}
