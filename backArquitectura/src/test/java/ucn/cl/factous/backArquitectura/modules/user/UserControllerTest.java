package ucn.cl.factous.backArquitectura.modules.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ucn.cl.factous.backArquitectura.modules.user.controller.UserController;
import ucn.cl.factous.backArquitectura.modules.user.dto.UserDTO;
import ucn.cl.factous.backArquitectura.modules.user.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO createTestUserDTO(Long id, String name) {
        return new UserDTO(id, name, name + "@test.com", "password123", "test_role");
    }

    @Test
    void shouldReturnAllUsersSuccessfully() throws Exception {
        UserDTO user1 = createTestUserDTO(1L, "Alice");
        UserDTO user2 = createTestUserDTO(2L, "Bob");
        List<UserDTO> allUsers = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(allUsers);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).getAllUsers();
    }
    
    @Test
    void shouldReturnUserByIdSuccessfully() throws Exception {
        Long userId = 3L;
        UserDTO expectedDto = createTestUserDTO(userId, "Organizador");

        when(userService.getUserById(userId)).thenReturn(expectedDto);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Organizador"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        Long userId = 99L;

        when(userService.getUserById(userId)).thenReturn(null);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        UserDTO inputDto = createTestUserDTO(null, "NewUser");
        UserDTO createdDto = createTestUserDTO(5L, "NewUser");

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdDto);

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.name").value("NewUser"));

        verify(userService, times(1)).createUser(any(UserDTO.class));
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        Long userId = 4L;
        UserDTO inputDto = createTestUserDTO(null, "UpdatedName");
        UserDTO updatedDto = createTestUserDTO(userId, "UpdatedName");

        when(userService.updateUser(eq(userId), any(UserDTO.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("UpdatedName"));

        verify(userService, times(1)).updateUser(eq(userId), any(UserDTO.class));
    }

    @Test
    void shouldReturn404OnUpdateWhenUserNotFound() throws Exception {
        Long userId = 99L;
        UserDTO inputDto = createTestUserDTO(null, "NonExistent");

        when(userService.updateUser(eq(userId), any(UserDTO.class))).thenReturn(null);

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound()); // Expect 404 Not Found

        verify(userService, times(1)).updateUser(eq(userId), any(UserDTO.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
        Long userId = 6L;

        when(userService.deleteUser(userId)).thenReturn(true);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent()); // Expect 204 No Content

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void shouldReturn404OnDeleteWhenUserNotFound() throws Exception {
        Long userId = 99L;

        when(userService.deleteUser(userId)).thenReturn(false);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(userId);
    }
}