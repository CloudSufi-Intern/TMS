package cloudsufi.nextgen.tms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {


//     Defines a global CorsFilter bean.
//
//     This filter:
//      - Allows requests from specified frontend origin
//      - Permits defined HTTP methods
//      - Allows required headers
//      - Supports credentials (cookies, authorization headers)

    @Bean
    public CorsFilter corsFilter() {

        // Create CORS configuration object
        CorsConfiguration config = new CorsConfiguration();

        // Allow requests from frontend application
        config.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        // Allow specified HTTP methods
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        );

        // Allow all headers (can be restricted later if needed)
        config.setAllowedHeaders(List.of("*"));

        // Allow credentials (required for JWT Authorization header support)
        config.setAllowCredentials(true);

        // Apply this configuration to all endpoints
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        // Return CorsFilter with applied configuration
        return new CorsFilter(source);
    }
}



//In SecurityConfig they must include:  http.cors(Customizer.withDefaults());