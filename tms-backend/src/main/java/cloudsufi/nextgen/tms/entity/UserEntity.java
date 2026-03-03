package cloudsufi.nextgen.tms.entity;
import cloudsufi.nextgen.tms.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Entity representing a User in the Task Management System.
 * Maps to the 'users' table in the database.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "username", columnList = "username"),
        @Index(name = "email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

/**@Author-Smriti_Bajpai*/
public class UserEntity {
    /** Primary key -> auto-incremented unique identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Username cannot be empty")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email can not be empty")
    @Email(message = "invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_no", nullable = false)
    private String phoneNo;

    @Column(nullable = false)
    private String password;

    /**
     * User's organizational role.
     * Stored as a String in the database for better readability.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
