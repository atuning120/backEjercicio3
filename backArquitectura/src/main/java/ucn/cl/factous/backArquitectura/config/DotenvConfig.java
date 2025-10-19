package ucn.cl.factous.backArquitectura.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()  // No fallar si no existe el archivo
                    .load();

            // Solo cargar las variables si el archivo .env existe
            if (dotenv != null) {
                dotenv.entries().forEach(entry -> {
                    // Solo establecer si no existe ya como variable de entorno del sistema
                    if (System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
                System.out.println("Archivo .env cargado exitosamente");
            }
        } catch (Exception e) {
            // En producción (Render), es normal que no exista .env
            System.out.println("No se encontró archivo .env - usando variables de entorno del sistema");
        }
    }
}
