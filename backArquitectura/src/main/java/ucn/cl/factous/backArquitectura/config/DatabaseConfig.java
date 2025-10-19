package ucn.cl.factous.backArquitectura.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "file:.env", ignoreResourceNotFound = true)
@ConfigurationProperties
public class DatabaseConfig {
    // Esta clase ayuda a cargar el archivo .env como propiedades si existe
    // En producción (Render), se usan variables de entorno del sistema
}
