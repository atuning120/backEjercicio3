package ucn.cl.factous.backArquitectura.modules.spot;

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

import ucn.cl.factous.backArquitectura.modules.spot.controller.SpotController;
import ucn.cl.factous.backArquitectura.modules.spot.dto.SpotDTO;
import ucn.cl.factous.backArquitectura.modules.spot.service.SpotService;

@WebMvcTest(SpotController.class)
class SpotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpotService spotService;

    @Autowired
    private ObjectMapper objectMapper;

    private SpotDTO createTestSpotDTO(Long id, String name, Long ownerId) {
        SpotDTO dto = new SpotDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setOwnerId(ownerId);
        dto.setLocation("Location for " + name);
        return dto;
    }

    @Test
    void shouldReturnAllSpotsSuccessfully() throws Exception {
        List<SpotDTO> allSpots = Arrays.asList(
            createTestSpotDTO(1L, "Stadium A", 10L),
            createTestSpotDTO(2L, "Club B", 11L)
        );

        when(spotService.getAllSpots()).thenReturn(allSpots);

        mockMvc.perform(get("/spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Stadium A"));

        verify(spotService, times(1)).getAllSpots();
    }

    @Test
    void shouldReturnSpotByIdSuccessfully() throws Exception {
        Long spotId = 10L;
        SpotDTO expectedDto = createTestSpotDTO(spotId, "Test Spot", 1L);

        when(spotService.getSpotById(spotId)).thenReturn(expectedDto);

        mockMvc.perform(get("/spots/{id}", spotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spotId))
                .andExpect(jsonPath("$.name").value("Test Spot"));

        verify(spotService, times(1)).getSpotById(spotId);
    }

    @Test
    void shouldReturn404WhenSpotNotFoundById() throws Exception {
        Long spotId = 99L;

        when(spotService.getSpotById(spotId)).thenReturn(null);

        mockMvc.perform(get("/spots/{id}", spotId))
                .andExpect(status().isNotFound());

        verify(spotService, times(1)).getSpotById(spotId);
    }

    @Test
    void shouldCreateSpotSuccessfully() throws Exception {
        SpotDTO inputDto = createTestSpotDTO(null, "New Spot", 1L);
        SpotDTO createdDto = createTestSpotDTO(50L, "New Spot", 1L);

        when(spotService.createSpot(any(SpotDTO.class))).thenReturn(createdDto);

        mockMvc.perform(post("/spots")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50L))
                .andExpect(jsonPath("$.name").value("New Spot"));

        verify(spotService, times(1)).createSpot(any(SpotDTO.class));
    }

    @Test
    void shouldUpdateSpotSuccessfully() throws Exception {
        Long spotId = 10L;
        SpotDTO inputDto = createTestSpotDTO(null, "Updated Spot", 1L);
        SpotDTO updatedDto = createTestSpotDTO(spotId, "Updated Spot", 1L);

        when(spotService.updateSpot(eq(spotId), any(SpotDTO.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/spots/{id}", spotId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Spot"));

        verify(spotService, times(1)).updateSpot(eq(spotId), any(SpotDTO.class));
    }

    @Test
    void shouldReturn404OnUpdateWhenSpotNotFound() throws Exception {
        Long spotId = 99L;
        SpotDTO inputDto = createTestSpotDTO(null, "NonExistent", 1L);

        when(spotService.updateSpot(eq(spotId), any(SpotDTO.class))).thenReturn(null);

        mockMvc.perform(put("/spots/{id}", spotId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());

        verify(spotService, times(1)).updateSpot(eq(spotId), any(SpotDTO.class));
    }

    @Test
    void shouldDeleteSpotSuccessfully() throws Exception {
        Long spotId = 6L;

        when(spotService.deleteSpot(spotId)).thenReturn(true);

        mockMvc.perform(delete("/spots/{id}", spotId))
                .andExpect(status().isNoContent());

        verify(spotService, times(1)).deleteSpot(spotId);
    }

    @Test
    void shouldReturn404OnDeleteWhenSpotNotFound() throws Exception {
        Long spotId = 99L;

        when(spotService.deleteSpot(spotId)).thenReturn(false);

        mockMvc.perform(delete("/spots/{id}", spotId))
                .andExpect(status().isNotFound());

        verify(spotService, times(1)).deleteSpot(spotId);
    }

    @Test
    void shouldReturnSpotsByOwnerIdSuccessfully() throws Exception {
        Long ownerId = 1L;
        List<SpotDTO> spots = Arrays.asList(createTestSpotDTO(1L, "Owner Spot 1", ownerId));

        when(spotService.getSpotsByOwner(ownerId)).thenReturn(spots);

        mockMvc.perform(get("/spots/owner/{ownerId}", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ownerId").value(ownerId));

        verify(spotService, times(1)).getSpotsByOwner(ownerId);
    }
}