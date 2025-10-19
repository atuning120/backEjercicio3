package ucn.cl.factous.backArquitectura.modules.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ucn.cl.factous.backArquitectura.modules.auth.dto.LoginDTO;
import ucn.cl.factous.backArquitectura.modules.user.dto.UserDTO;
import ucn.cl.factous.backArquitectura.modules.user.service.UserService;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO createTestUserDTO(Long id, String email, String password, String role) {
        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setName("Test User");
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setRole(role);
        return dto;
    }

    @Test
    void loginShouldReturn200OnSuccessfulAuthentication() throws Exception {
        String email = "success@test.com";
        String password = "correctpassword";
        LoginDTO loginDTO = new LoginDTO(email, password);
        UserDTO user = createTestUserDTO(1L, email, password, "admin");

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(userService.verifyPassword(email, password)).thenReturn(true);

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login exitoso"))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.password").doesNotExist());

        verify(userService, times(1)).getUserByEmail(email);
        verify(userService, times(1)).verifyPassword(email, password);
    }

    @Test
    void loginShouldReturn401WhenUserNotFound() throws Exception {
        String email = "notfound@test.com";
        LoginDTO loginDTO = new LoginDTO(email, "anypass");

        when(userService.getUserByEmail(email)).thenReturn(null);

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));

        verify(userService, times(1)).getUserByEmail(email);
        verify(userService, times(0)).verifyPassword(anyString(), anyString());
    }

    @Test
    void loginShouldReturn401WhenPasswordIsIncorrect() throws Exception {
        String email = "user@test.com";
        String correctPass = "secure";
        LoginDTO loginDTO = new LoginDTO(email, "wrongpass");
        UserDTO user = createTestUserDTO(1L, email, correctPass, "user");

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(userService.verifyPassword(email, loginDTO.getPassword())).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Contraseña incorrecta"));

        verify(userService, times(1)).getUserByEmail(email);
        verify(userService, times(1)).verifyPassword(email, loginDTO.getPassword());
    }

    @Test
    void registerShouldReturn201OnSuccessfulRegistration() throws Exception {
        String email = "newuser@test.com";
        UserDTO registerDTO = createTestUserDTO(null, email, "newpass", "user");
        UserDTO createdUser = createTestUserDTO(2L, email, "newpass", "user");

        when(userService.getUserByEmail(email)).thenReturn(null);
        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"))
                .andExpect(jsonPath("$.user.id").value(2L))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.password").doesNotExist());

        verify(userService, times(1)).getUserByEmail(email);
        verify(userService, times(1)).createUser(any(UserDTO.class));
    }

    @Test
    void registerShouldReturn409WhenEmailAlreadyExists() throws Exception {
        String email = "existing@test.com";
        UserDTO registerDTO = createTestUserDTO(null, email, "newpass", "user");
        UserDTO existingUser = createTestUserDTO(1L, email, "oldpass", "user");

        when(userService.getUserByEmail(email)).thenReturn(existingUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("El email ya está registrado"));

        verify(userService, times(1)).getUserByEmail(email);
        verify(userService, times(0)).createUser(any(UserDTO.class));
    }
}