package ucn.cl.factous.backArquitectura.modules.spot.dto;

import ucn.cl.factous.backArquitectura.modules.user.entity.User;

public class SpotDTO {
    private Long id;
    private String name;
    private Long ownerId;
    private String location;

    public SpotDTO() {}

    public SpotDTO(Long id, String name, Long ownerId, String location) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (id != null) {
            this.id = id;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        if (ownerId != null) {
            this.ownerId = ownerId;
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location != null && !location.trim().isEmpty()) {
            this.location = location;
        }
    }
}