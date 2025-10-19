package ucn.cl.factous.backArquitectura.modules.auth.dto;

import ucn.cl.factous.backArquitectura.modules.user.dto.UserDTO;

public class AuthResponseDTO {
    private boolean success;
    private String message;
    private UserDTO user;

    // Constructores
    public AuthResponseDTO() {}

    public AuthResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponseDTO(boolean success, String message, UserDTO user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
