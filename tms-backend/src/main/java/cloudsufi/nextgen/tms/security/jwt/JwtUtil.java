package cloudsufi.nextgen.tms.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

/*
 * JwtUtil
 *
 * Utility class responsible for handling JWT operations
 * such as extracting username, validating token,
 * and checking token expiration.
 *
 * Author: Priyanshu gupta
 */
@Component
public class JwtUtil {

    private static final String SECRET_KEY ="tms-security-key";

    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(SECRET_KEY.getBytes()).build().parseClaimsJws(token);
            return true;
        }catch(Exception ex){
            return false;
        }
    }

    public String extractUsername(String token){
        Claims claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token).getBody();

        return claims.getSubject();
    }
}
