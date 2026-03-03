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



@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
     //utility class responsible for validating jwt tokens
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{

        //debug line to verify filter execution
        System.out.println("JWT filter executed for request:" + request.getRequestURI());

        //extracting authrization header from incoming request
        String authHeader = request.getHeader("Authorization");

         //check if header exists and follows bearer token format
        if(authHeader != null && authHeader.startsWith("Bearer")){

             //extracting token by removing bearer
            String token = authHeader.substring(7);

           //validate token using utility class
            boolean isValid = jwtUtil.validateToken(token);

            if(isValid){
                //in future will set authentication and extract username
                //for now we only validate token as part of swcuirty setup

            }
        }
          //continuing filter chAIN so request reaches controller
        filterChain.doFilter(request, response);
    }
}
