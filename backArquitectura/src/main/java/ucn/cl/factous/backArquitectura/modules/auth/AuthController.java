package ucn.cl.factous.backArquitectura.modules.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ucn.cl.factous.backArquitectura.modules.auth.dto.AuthResponseDTO;
import ucn.cl.factous.backArquitectura.modules.auth.dto.LoginDTO;
import ucn.cl.factous.backArquitectura.modules.user.dto.UserDTO;
import ucn.cl.factous.backArquitectura.modules.user.service.UserService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"${FRONT_URI:http://localhost:5173}", "${FRONT_URI_ALTERNATIVE:http://127.0.0.1:5173}", "${FRONTEND_URL:http://localhost:3000}"})
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        try {
            // Buscar usuario por email
            UserDTO user = userService.getUserByEmail(loginDTO.getEmail());
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(false, "Usuario no encontrado"));
            }

            // Verificar contraseña (en un entorno real usarías hash)
            if (userService.verifyPassword(loginDTO.getEmail(), loginDTO.getPassword())) {
                // Login exitoso - no devolver la contraseña
                UserDTO safeUser = new UserDTO();
                safeUser.setId(user.getId());
                safeUser.setName(user.getName());
                safeUser.setEmail(user.getEmail());
                safeUser.setRole(user.getRole()); 
                // No establecer password para seguridad
                
                return ResponseEntity.ok(new AuthResponseDTO(true, "Login exitoso", safeUser));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(false, "Contraseña incorrecta"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponseDTO(false, "Error interno del servidor"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody UserDTO userDTO) {
        try {
            // Verificar si el email ya existe
            UserDTO existingUser = userService.getUserByEmail(userDTO.getEmail());
            if (existingUser != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponseDTO(false, "El email ya está registrado"));
            }

            // Crear nuevo usuario
            UserDTO newUser = userService.createUser(userDTO);
            
            // Crear respuesta segura sin contraseña
            UserDTO safeUser = new UserDTO();
            safeUser.setId(newUser.getId());
            safeUser.setName(newUser.getName());
            safeUser.setEmail(newUser.getEmail());
            safeUser.setRole(newUser.getRole()); // Usar 'role' en vez de 'accountType'
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponseDTO(true, "Usuario registrado exitosamente", safeUser));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponseDTO(false, "Error al registrar usuario"));
        }
    }
}
