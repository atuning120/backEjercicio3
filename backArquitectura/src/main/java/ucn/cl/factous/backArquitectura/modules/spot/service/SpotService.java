package ucn.cl.factous.backArquitectura.modules.spot.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import ucn.cl.factous.backArquitectura.modules.spot.dto.SpotDTO;
import ucn.cl.factous.backArquitectura.modules.spot.entity.Spot;
import ucn.cl.factous.backArquitectura.modules.spot.repository.SpotRepository;
import ucn.cl.factous.backArquitectura.modules.user.entity.User;
import ucn.cl.factous.backArquitectura.modules.user.repository.UserRepository;

@Service
public class SpotService {

    @Autowired
    private SpotRepository spotRepository;
    @Autowired
    private UserRepository userRepository;

    public List<SpotDTO> getAllSpots() {
        return spotRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public SpotDTO getSpotById(Long id) {
        Optional<Spot> spotOptional = spotRepository.findById(id);
        return spotOptional.map(this::convertToDto).orElse(null);
    }

    public SpotDTO createSpot(SpotDTO spotDTO) {
        User ownerFound = userRepository.findById(spotDTO.getOwnerId()).orElse(null);
        if (ownerFound == null) {
            throw new IllegalArgumentException("Propietario con ID " + spotDTO.getOwnerId() + " no existe.");
        }
        Spot convertedSpot = new Spot(spotDTO.getName(), ownerFound, spotDTO.getLocation());
        Spot savedSpot = spotRepository.save(convertedSpot);
        return convertToDto(savedSpot);
    }

    public SpotDTO updateSpot(Long id, SpotDTO spotDTO) {
        Optional<Spot> optionalSpot = spotRepository.findById(id);
        if (optionalSpot.isPresent()) {
            Spot existingSpot = optionalSpot.get();
            existingSpot.setName(spotDTO.getName());
            existingSpot.setOwner(userRepository.findById(spotDTO.getOwnerId()).orElse(null));
            existingSpot.setLocation(spotDTO.getLocation());
            Spot updatedSpot = spotRepository.save(existingSpot);
            return convertToDto(updatedSpot);
        }
        return null;
    }

    public boolean deleteSpot(Long id) {
        if (spotRepository.existsById(id)) {
            spotRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Métodos específicos para propietarios
    public List<SpotDTO> getSpotsByOwner(Long ownerId) {
        return spotRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private SpotDTO convertToDto(Spot spot) {
        return new SpotDTO(spot.getId(), spot.getName(), spot.getOwner().getId(), spot.getLocation());
    }

    private Spot convertToEntity(SpotDTO spotDTO) {
        Spot spot = new Spot();
        spot.setName(spotDTO.getName());
        spot.setOwner(userRepository.findById(spotDTO.getOwnerId()).orElse(null));
        spot.setLocation(spotDTO.getLocation());
        return spot;
    }
}