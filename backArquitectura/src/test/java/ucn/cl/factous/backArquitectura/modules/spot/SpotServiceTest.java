package ucn.cl.factous.backArquitectura.modules.spot;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ucn.cl.factous.backArquitectura.modules.spot.dto.SpotDTO;
import ucn.cl.factous.backArquitectura.modules.spot.entity.Spot;
import ucn.cl.factous.backArquitectura.modules.spot.repository.SpotRepository;
import ucn.cl.factous.backArquitectura.modules.spot.service.SpotService;
import ucn.cl.factous.backArquitectura.modules.user.entity.User;
import ucn.cl.factous.backArquitectura.modules.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SpotServiceTest {

    @Mock
    private SpotRepository spotRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SpotService spotService;

    private User testOwner;
    private SpotDTO testSpotDTO;
    private Spot testSpot;

    @BeforeEach
    void setUp() {
        testOwner = new User();
        testOwner.setId(1L);

        testSpotDTO = new SpotDTO();
        testSpotDTO.setName("Test Spot");
        testSpotDTO.setOwnerId(1L);
        testSpotDTO.setLocation("Test Location");
        
        testSpot = new Spot("Test Spot", testOwner, "Test Location");
        testSpot.setId(3L);
    }

    @Test
    void shouldReturnAllSpots() {
        when(spotRepository.findAll()).thenReturn(Arrays.asList(testSpot));

        List<SpotDTO> result = spotService.getAllSpots();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Spot", result.get(0).getName());
        verify(spotRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnSpotById() {
        when(spotRepository.findById(3L)).thenReturn(Optional.of(testSpot));

        SpotDTO result = spotService.getSpotById(3L);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        verify(spotRepository, times(1)).findById(3L);
    }

    @Test
    void shouldReturnNullWhenSpotByIdNotFound() {
        when(spotRepository.findById(99L)).thenReturn(Optional.empty());

        SpotDTO result = spotService.getSpotById(99L);

        assertNull(result);
        verify(spotRepository, times(1)).findById(99L);
    }

    @Test
    void shouldCreateSpotSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(spotRepository.save(any(Spot.class))).thenReturn(testSpot);

        SpotDTO result = spotService.createSpot(testSpotDTO);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        verify(userRepository, times(1)).findById(1L);
        verify(spotRepository, times(1)).save(any(Spot.class));
    }

    @Test
    void shouldThrowExceptionWhenOwnerNotFoundOnCreate() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            spotService.createSpot(testSpotDTO);
        });
        verify(userRepository, times(1)).findById(1L);
        verify(spotRepository, times(0)).save(any());
    }

    @Test
    void shouldUpdateSpotSuccessfully() {
        Long spotId = 3L;
        SpotDTO updateDto = new SpotDTO(spotId, "Updated Name", 1L, "New Location");

        when(spotRepository.findById(spotId)).thenReturn(Optional.of(testSpot));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(spotRepository.save(any(Spot.class))).thenAnswer(i -> {
            Spot spot = i.getArgument(0);
            spot.setName(updateDto.getName());
            return spot;
        });

        SpotDTO result = spotService.updateSpot(spotId, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(spotRepository, times(1)).findById(spotId);
        verify(userRepository, times(1)).findById(1L);
        verify(spotRepository, times(1)).save(any(Spot.class));
    }

    @Test
    void shouldReturnNullWhenUpdateSpotNotFound() {
        Long spotId = 99L; 
        SpotDTO updateDto = new SpotDTO(spotId, "NonExistent", null, "No Location");

        when(spotRepository.findById(spotId)).thenReturn(Optional.empty());

        SpotDTO result = spotService.updateSpot(spotId, updateDto);

        assertNull(result);
        verify(spotRepository, times(1)).findById(spotId);
        verify(userRepository, times(0)).findById(anyLong());
        verify(spotRepository, times(0)).save(any());
    }

    @Test
    void shouldDeleteSpotSuccessfully() {
        Long spotId = 3L;

        when(spotRepository.existsById(spotId)).thenReturn(true);
        doNothing().when(spotRepository).deleteById(spotId);

        boolean isDeleted = spotService.deleteSpot(spotId);

        assertTrue(isDeleted);
        verify(spotRepository, times(1)).existsById(spotId);
        verify(spotRepository, times(1)).deleteById(spotId);
    }

    @Test
    void shouldReturnFalseWhenDeleteSpotNotFound() {
        Long spotId = 99L;

        when(spotRepository.existsById(spotId)).thenReturn(false);

        boolean isDeleted = spotService.deleteSpot(spotId);

        assertFalse(isDeleted);
        verify(spotRepository, times(1)).existsById(spotId);
        verify(spotRepository, times(0)).deleteById(spotId);
    }

    @Test
    void shouldReturnSpotsByOwner() {
        Long ownerId = 1L;
        when(spotRepository.findByOwnerId(ownerId)).thenReturn(Arrays.asList(testSpot));

        List<SpotDTO> result = spotService.getSpotsByOwner(ownerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getOwnerId());
        verify(spotRepository, times(1)).findByOwnerId(ownerId);
    }
}