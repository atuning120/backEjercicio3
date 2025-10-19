package ucn.cl.factous.backArquitectura.modules.user;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ucn.cl.factous.backArquitectura.modules.user.dto.UserDTO;
import ucn.cl.factous.backArquitectura.modules.user.entity.User;
import ucn.cl.factous.backArquitectura.modules.user.repository.UserRepository;
import ucn.cl.factous.backArquitectura.modules.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User createTestUser(Long id, String name, String email, String password) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("user");
        return user;
    }

    private UserDTO createTestUserDTO(Long id, String name, String email, String password) {
        return new UserDTO(id, name, email, password, "user");
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = createTestUser(1L, "Alice", "a@test.com", "pass");
        User user2 = createTestUser(2L, "Bob", "b@test.com", "pass");
        List<User> userList = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(userList);

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnUserById() {
        Long userId = 3L;
        User user = createTestUser(userId, "Jorge", "jorge@test.com", "pass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals("Jorge", result.getName());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void shouldReturnNullWhenUserByIdNotFound() {
        Long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserDTO result = userService.getUserById(userId);

        assertNull(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void shouldCreateUser() {
        UserDTO inputDto = createTestUserDTO(null, "New", "new@test.com", "pass");
        User savedEntity = createTestUser(4L, "New", "new@test.com", "pass");

        when(userRepository.save(any(User.class))).thenReturn(savedEntity);

        UserDTO result = userService.createUser(inputDto);

        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals("New", result.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        Long userId = 5L;
        User existingUser = createTestUser(userId, "OldName", "old@test.com", "oldpass");
        UserDTO updateDto = createTestUserDTO(userId, "NewName", "new@test.com", "newpass");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals("NewName", result.getName());
        assertEquals("new@test.com", result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldReturnNullWhenUpdateUserNotFound() {
        Long userId = 99L;
        UserDTO updateDto = createTestUserDTO(userId, "Invalid", "i@test.com", "pass");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserDTO result = userService.updateUser(userId, updateDto);

        assertNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        Long userId = 6L;

        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        boolean isDeleted = userService.deleteUser(userId);

        assertTrue(isDeleted);
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void shouldReturnFalseWhenDeleteUserNotFound() {
        Long userId = 99L;

        when(userRepository.existsById(userId)).thenReturn(false);

        boolean isDeleted = userService.deleteUser(userId);

        assertFalse(isDeleted);
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(0)).deleteById(userId);
    }
    
    @Test
    void shouldReturnUserByEmail() {
        String email = "findme@test.com";
        User user = createTestUser(7L, "EmailUser", email, "pass");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals("EmailUser", result.getName());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void shouldReturnNullWhenUserByEmailNotFound() {
        String email = "notfound@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserDTO result = userService.getUserByEmail(email);

        assertNull(result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void shouldVerifyPasswordSuccessfully() {
        String email = "auth@test.com";
        String correctPass = "secure123";
        User user = createTestUser(8L, "AuthUser", email, correctPass);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        boolean verified = userService.verifyPassword(email, correctPass);

        assertTrue(verified);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void shouldReturnFalseForIncorrectPassword() {
        String email = "auth@test.com";
        User user = createTestUser(8L, "AuthUser", email, "secure123");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        boolean verified = userService.verifyPassword(email, "wrongpassword");

        assertFalse(verified);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void shouldReturnFalseWhenVerifyPasswordUserNotFound() {
        String email = "nonexistent@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        boolean verified = userService.verifyPassword(email, "anypass");

        assertFalse(verified);
        verify(userRepository, times(1)).findByEmail(email);
    }
}