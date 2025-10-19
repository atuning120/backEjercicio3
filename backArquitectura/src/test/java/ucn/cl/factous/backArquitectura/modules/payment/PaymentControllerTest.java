package ucn.cl.factous.backArquitectura.modules.payment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;

import ucn.cl.factous.backArquitectura.modules.notification.service.NotificationService;
import ucn.cl.factous.backArquitectura.modules.payment.controller.MercadoPagoController;
import ucn.cl.factous.backArquitectura.modules.payment.dto.PaymentPreferenceDTO;
import ucn.cl.factous.backArquitectura.shared.dto.PurchaseTicketDTO;
import ucn.cl.factous.backArquitectura.shared.dto.TicketDTO;
import ucn.cl.factous.backArquitectura.shared.service.TicketService;

@WebMvcTest(MercadoPagoController.class)
@TestPropertySource(properties = {
    "FRONT_URI=https://frontend.com",
    "REAL_TEST_TOKEN=GLOBAL_TEST_TOKEN"
})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private NotificationService notificationService;

    private Preference preferenceMock;
    private PaymentPreferenceDTO testPaymentData;
    private final String MOCKED_PREFERENCE_ID = "MOCK_PREF_12345";
    private final String REAL_TEST_TOKEN = "GLOBAL_TEST_TOKEN";
    
    @BeforeEach
    void setUp() {
        testPaymentData = new PaymentPreferenceDTO(100L, 1L, 2, "Test Event", 5000.0, 10000.0);
        preferenceMock = mock(Preference.class);
        System.setProperty("MERCADOPAGO_ACCESS_TOKEN", REAL_TEST_TOKEN);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("MERCADOPAGO_ACCESS_TOKEN");
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    void shouldReturnPreferenceIdOnSuccess() throws Exception {
        System.setProperty("MERCADOPAGO_ACCESS_TOKEN", "GLOBAL_TEST_TOKEN");
        try (var mockedConstruction = Mockito.mockConstruction(PreferenceClient.class,
                (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preferenceMock))) {
            when(preferenceMock.getId()).thenReturn(MOCKED_PREFERENCE_ID);

            mockMvc.perform(post("/api/mercadopago/create-preference")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testPaymentData)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(MOCKED_PREFERENCE_ID));
        }
    }

    @Test
    @DirtiesContext
    void shouldReturnErrorWhenAccessTokenIsMissing() throws Exception {
        System.clearProperty("MERCADOPAGO_ACCESS_TOKEN");

        mockMvc.perform(post("/api/mercadopago/create-preference")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPaymentData)))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Error: ACCESS_TOKEN no está configurado")));
    }
    
    @Test
    void shouldProcessPaymentSuccessAndRedirectToFrontend() throws Exception {
        System.setProperty("MERCADOPAGO_ACCESS_TOKEN", REAL_TEST_TOKEN);
        String externalReference = "USER_1_EVENT_100_QTY_2";
        TicketDTO createdTicket = new TicketDTO(1L, 5000.0, 100L, 1L, 1L, "Event1", "25/10/2025", "1A2B"); 
        
        when(ticketService.purchaseTicket(any(PurchaseTicketDTO.class))).thenReturn(createdTicket);
        doNothing().when(notificationService).sendPurchaseSuccessNotification(anyLong(), anyLong());

        mockMvc.perform(get("/api/mercadopago/payment-success")
                        .param("collection_status", "approved")
                        .param("external_reference", externalReference))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/payment-success?ticketId=1")); 
    }
    
    @Test
    void shouldRedirectToPaymentFailedWhenStatusIsNotApproved() throws Exception {
        System.setProperty("MERCADOPAGO_ACCESS_TOKEN", REAL_TEST_TOKEN);
        mockMvc.perform(get("/api/mercadopago/payment-success")
                        .param("collection_status", "rejected")
                        .param("external_reference", "USER_1_EVENT_100_QTY_2"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/payment-failed?error=not_approved&status=rejected"));
    }

    @Test
    void shouldRedirectToPaymentFailedWhenReferenceIsInvalid() throws Exception {
        System.setProperty("MERCADOPAGO_ACCESS_TOKEN", REAL_TEST_TOKEN);
        mockMvc.perform(get("/api/mercadopago/payment-success")
                        .param("collection_status", "approved")
                        .param("external_reference", "INVALID_REF"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/payment-failed?error=invalid_reference&ref=INVALID_REF"));
    }
}