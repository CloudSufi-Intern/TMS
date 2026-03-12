package cloudsufi.nextgen.tms.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Utility class responsible for handling JSON Web Token (JWT) operations.
 *
 * This component provides methods to generate new JWTs upon user login,
 * extract user information (such as the username) from incoming tokens,
 * and validate token authenticity and expiration.
 *
 * @author Ansh Parnami
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generates a secure cryptographic Key object used for signing and verifying JWTs.
     * It uses the secret string defined in the application properties.
     *
     * @return A cryptographic {@link Key} object based on the HMAC-SHA algorithm.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a new JWT token for the authenticated user.
     * The token includes the username as the subject, the issue date,
     * and an expiration date based on the configured expiration time.
     *
     * @param username The email or username of the authenticated user.
     * @return A completely constructed and signed JWT string.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates the provided JWT token.
     * This method parses the token using the signing key. If the token is
     * tampered with, malformed, or expired, the parsing will throw an exception,
     * which is caught to return false.
     *
     * @param token The JWT string extracted from the Authorization header.
     * @return {@code true} if the token is valid and not expired; {@code false} otherwise.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extracts the username (subject) from the provided JWT token payload.
     *
     * @param token The valid JWT string.
     * @return The username or email stored in the token's subject claim.
     */
    public String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}
