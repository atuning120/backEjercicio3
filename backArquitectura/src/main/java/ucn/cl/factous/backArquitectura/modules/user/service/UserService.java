package ucn.cl.factous.backArquitectura.modules.user.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ucn.cl.factous.backArquitectura.modules.user.dto.UserDTO;
import ucn.cl.factous.backArquitectura.modules.user.entity.User;
import ucn.cl.factous.backArquitectura.modules.user.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.map(this::convertToDto).orElse(null);
    }

    public UserDTO createUser(UserDTO userDTO) {
        User user = convertToEntity(userDTO);
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setName(userDTO.getName()); 
            existingUser.setEmail(userDTO.getEmail());
            existingUser.setPassword(userDTO.getPassword());
            // Actualizar el rol si se proporciona
            if (userDTO.getRole() != null) {
                existingUser.setRole(userDTO.getRole());
            }

            User updatedUser = userRepository.save(existingUser);
            return convertToDto(updatedUser);
        }
        return null;
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Nuevos métodos para autenticación
    public UserDTO getUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(this::convertToDto).orElse(null);
    }

    public boolean verifyPassword(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // En un entorno real, usarías BCrypt o similar para hash de contraseñas
            return user.getPassword().equals(password);
        }
        return false;
    }
    
    public UserDTO convertToDto(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getRole());
    }

    public User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        // Mapear role del DTO al campo role de la entidad
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
        return user;
    }
}