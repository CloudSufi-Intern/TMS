package cloudsufi.nextgen.tms.security.filter;

import cloudsufi.nextgen.tms.security.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.http.HttpResponse;


/*
 * JwtAuthenticationFilter
 *
 * This filter intercepts incoming HTTP requests and extracts
 * the JWT token from the Authorization header.
 *
 * It validates the token and sets the authentication
 * in the Spring Security context if the token is valid.
 *
 * Author: Priyanshu gupta
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{


        System.out.println("JWT filter executed for request:" + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer")){


            String token = authHeader.substring(7);


            boolean isValid = jwtUtil.validateToken(token);

            if(isValid){
            }
        }

        filterChain.doFilter(request, response);
    }
}
