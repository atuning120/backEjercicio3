package ucn.cl.factous.backArquitectura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Value("${FRONT_URI:http://localhost:5173}")
    private String frontUri;

    @Value("${FRONT_URI_ALTERNATIVE:http://127.0.0.1:5173}")
    private String frontUriAlternative;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Permitir peticiones desde el frontend configurado
        config.addAllowedOrigin(frontendUrl);
        config.addAllowedOrigin(frontUri);
        config.addAllowedOrigin(frontUriAlternative);
        
        // También permitir localhost para desarrollo
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://127.0.0.1:5173");
        config.addAllowedOrigin("http://localhost:3000");
        
        // Permitir dominio de producción en Vercel
        config.addAllowedOrigin("https://frontend-arquitectura-sistemas-one.vercel.app");
        
        // Permitir todos los métodos HTTP
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        
        // Permitir todos los headers
        config.addAllowedHeader("*");
        
        // Permitir credenciales
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
