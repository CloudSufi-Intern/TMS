package cloudsufi.nextgen.tms.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    //secret key is used to validate tokens
    //later moved to application properties
    private static final String SECRET_KEY ="tms-security-key";

    //validates whether the jwt token is correct and not expired

    public boolean validateToken(String token){
        try{
            //parsing token will throw exception if not corrrect or valid
            Jwts.parserBuilder().setSigningKey(SECRET_KEY.getBytes()).build().parseClaimsJws(token);
            return true;
        }catch(Exception ex){
            //token expired or invalid
            return false;
        }
    }

    //extracting the username from token payload

    public String extractUsername(String token){
        Claims claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token).getBody();

        return claims.getSubject();
    }
}
