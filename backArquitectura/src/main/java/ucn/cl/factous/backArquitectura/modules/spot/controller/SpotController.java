package ucn.cl.factous.backArquitectura.modules.spot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ucn.cl.factous.backArquitectura.modules.spot.dto.SpotDTO;
import ucn.cl.factous.backArquitectura.modules.spot.service.SpotService;

@RestController
@RequestMapping("/spots")
@CrossOrigin(origins = {"${FRONT_URI}", "${FRONT_URI_ALTERNATIVE}"})
public class SpotController {

    @Autowired
    private SpotService spotService;

    @GetMapping
    public List<SpotDTO> getAllSpots() {
        return spotService.getAllSpots();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SpotDTO> getSpotById(@PathVariable Long id) {
        SpotDTO spotDTO = spotService.getSpotById(id);
        if (spotDTO != null) {
            return ResponseEntity.ok(spotDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<SpotDTO> createSpot(@RequestBody SpotDTO spotDTO) {
        SpotDTO createdSpot = spotService.createSpot(spotDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSpot);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpotDTO> updateSpot(@PathVariable Long id, @RequestBody SpotDTO spotDTO) {
        SpotDTO updatedSpot = spotService.updateSpot(id, spotDTO);
        if (updatedSpot != null) {
            return ResponseEntity.ok(updatedSpot);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpot(@PathVariable Long id) {
        boolean isDeleted = spotService.deleteSpot(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoints específicos para propietarios
    @GetMapping("/owner/{ownerId}")
    public List<SpotDTO> getSpotsByOwner(@PathVariable Long ownerId) {
        return spotService.getSpotsByOwner(ownerId);
    }
}