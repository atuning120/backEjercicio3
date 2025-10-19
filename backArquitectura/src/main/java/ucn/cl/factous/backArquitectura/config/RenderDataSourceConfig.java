package ucn.cl.factous.backArquitectura.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("render")
public class RenderDataSourceConfig {

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Parsear la URL de PostgreSQL de Render
        DatabaseUrlInfo urlInfo = parseRenderDatabaseUrl(databaseUrl);
        
        // Configurar HikariCP con los componentes parseados
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(urlInfo.jdbcUrl);
        config.setUsername(urlInfo.username);
        config.setPassword(urlInfo.password);
        config.setDriverClassName("org.postgresql.Driver");
        
        // Configuración optimizada para Render
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        // Configuraciones adicionales para PostgreSQL
        config.addDataSourceProperty("ssl", "true");
        config.addDataSourceProperty("sslmode", "require");
        
        System.out.println("🔗 Configurando DataSource para Render:");
        System.out.println("   URL: " + urlInfo.jdbcUrl);
        System.out.println("   Usuario: " + urlInfo.username);
        System.out.println("   Base de datos: " + urlInfo.database);
        
        return new HikariDataSource(config);
    }

    /**
     * Parsea la URL de PostgreSQL de Render y extrae los componentes
     */
    private DatabaseUrlInfo parseRenderDatabaseUrl(String renderUrl) {
        if (renderUrl == null || renderUrl.isEmpty()) {
            throw new RuntimeException("DATABASE_URL no está configurada");
        }
        
        System.out.println("🔍 URL original de Render: " + renderUrl);
        
        try {
            // Parsear la URL usando URI para extraer componentes
            URI uri = URI.create(renderUrl);
            
            String scheme = uri.getScheme(); // postgresql
            String host = uri.getHost();     // dpg-d37bf1er433s73ejroe0-a.oregon-postgres.render.com
            int port = uri.getPort() == -1 ? 5432 : uri.getPort(); // Puerto por defecto PostgreSQL si no está especificado
            String database = uri.getPath().substring(1); // bd_arquitecturauno (sin la barra inicial)
            
            // Extraer usuario y contraseña del userInfo
            String userInfo = uri.getUserInfo(); // bd_arquitecturauno_user:nhi78k03TQ0a40Eq7QGC6xBMQwoxesC7
            String[] credentials = userInfo.split(":");
            String username = credentials[0];
            String password = credentials[1];
            
            // Construir URL JDBC con los componentes correctos
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            
            System.out.println("✅ Componentes parseados:");
            System.out.println("   Host: " + host);
            System.out.println("   Puerto: " + port + (uri.getPort() == -1 ? " (por defecto)" : ""));
            System.out.println("   Base de datos: " + database);
            System.out.println("   Usuario: " + username);
            System.out.println("   JDBC URL: " + jdbcUrl);
            
            return new DatabaseUrlInfo(jdbcUrl, username, password, database);
            
        } catch (Exception e) {
            throw new RuntimeException("Error parseando DATABASE_URL: " + renderUrl, e);
        }
    }
    
    /**
     * Clase para almacenar la información de la base de datos parseada
     */
    private static class DatabaseUrlInfo {
        final String jdbcUrl;
        final String username;
        final String password;
        final String database;
        
        DatabaseUrlInfo(String jdbcUrl, String username, String password, String database) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
            this.database = database;
        }
    }
}