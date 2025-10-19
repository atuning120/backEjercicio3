package ucn.cl.factous.backArquitectura.modules.payment.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

import io.github.cdimascio.dotenv.Dotenv;
import ucn.cl.factous.backArquitectura.modules.notification.service.NotificationService;
import ucn.cl.factous.backArquitectura.modules.payment.dto.PaymentPreferenceDTO;
import ucn.cl.factous.backArquitectura.shared.dto.PurchaseTicketDTO;
import ucn.cl.factous.backArquitectura.shared.dto.TicketDTO;
import ucn.cl.factous.backArquitectura.shared.service.TicketService;

@RestController
@RequestMapping("/api/mercadopago")
@CrossOrigin(origins = { "${FRONT_URI}", "${FRONT_URI_ALTERNATIVE}" })
public class MercadoPagoController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/create-preference")
    public String createPaymentPreference(@RequestBody PaymentPreferenceDTO paymentData) {
        try {
            // Cargar variables de entorno (desde sistema o .env si existe)
            String accessToken = getEnvironmentVariable("TEST_ACCESS_TOKEN", "MERCADOPAGO_ACCESS_TOKEN");
            if (accessToken == null || accessToken.isEmpty()) {
                accessToken = System.getProperty("MERCADOPAGO_ACCESS_TOKEN"); //Alternativa para el test
            }
            String frontUri = getEnvironmentVariable("FRONT_URI", "FRONTEND_URL");

            if (accessToken == null || accessToken.isEmpty()) {
                return "Error: ACCESS_TOKEN no está configurado";
            }

            if (frontUri == null || frontUri.isEmpty()) {
                return "Error: FRONTEND_URL no está configurado";
            }

            // Configurar credencial
            MercadoPagoConfig.setAccessToken(accessToken);

            // Normalizar la URI del frontend (remover barra final si existe)
            String normalizedFrontUri = frontUri.endsWith("/") ? frontUri.substring(0, frontUri.length() - 1)
                    : frontUri;

            // Validar que la URI no esté vacía después de la normalización
            if (normalizedFrontUri.isEmpty()) {
                return "Error: FRONT_URI está vacío o solo contiene una barra";
            }

            // Construir URLs de retorno
            // Para desarrollo local, seguir usando el frontend
            // Para producción, usar el endpoint del backend para procesar el pago
            String successUrl, pendingUrl, failureUrl;
            
            // Detectar si estamos en modo desarrollo
            boolean isLocalhost = normalizedFrontUri.contains("localhost") || normalizedFrontUri.contains("127.0.0.1");
            
            if (isLocalhost) {
                // Desarrollo: dirigir al frontend
                successUrl = normalizedFrontUri + "/payment-confirmation";
                pendingUrl = normalizedFrontUri + "/available-events";
                failureUrl = normalizedFrontUri + "/available-events";
            } else {
                // Producción: dirigir al backend para procesar, luego redirigir al frontend
                String backendUrl = getEnvironmentVariable("BACKEND_URL", "RENDER_EXTERNAL_URL");
                if (backendUrl == null) {
                    backendUrl = "https://backendarquitecturasistemas-one.onrender.com";
                }
                successUrl = backendUrl + "/api/mercadopago/payment-success";
                pendingUrl = normalizedFrontUri + "/available-events";
                failureUrl = normalizedFrontUri + "/available-events";
            }

            System.out.println("URLs de retorno configuradas:");
            System.out.println("Success: " + successUrl);
            System.out.println("Pending: " + pendingUrl);
            System.out.println("Failure: " + failureUrl);
            System.out.println("Modo desarrollo (localhost): " + isLocalhost);

            // URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .pending(pendingUrl)
                    .failure(failureUrl)
                    .build();

            // Item de la preferencia
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id("EVENT_" + paymentData.getEventId())
                    .title("Tickets para " + paymentData.getEventName())
                    .description("Compra de " + paymentData.getQuantity() + " ticket(s) para el evento: "
                            + paymentData.getEventName())
                    .categoryId("entertainment")
                    .quantity(paymentData.getQuantity())
                    .currencyId("CLP")
                    .unitPrice(BigDecimal.valueOf(paymentData.getUnitPrice())) // Precio seguro
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            // Armar request de preferencia
            PreferenceRequest preferenceRequest;

            // Solo agregar autoReturn si no estamos en localhost (desarrollo)
            if (!isLocalhost) {
                preferenceRequest = PreferenceRequest.builder()
                        .items(items)
                        .backUrls(backUrls)
                        .autoReturn("all")
                        .externalReference("USER_" + paymentData.getUserId() + "_EVENT_" + paymentData.getEventId() + "_QTY_" + paymentData.getQuantity())
                        .build();
                System.out.println("Configuración: Producción con autoReturn");
            } else {
                // Para desarrollo local, no usar autoReturn
                preferenceRequest = PreferenceRequest.builder()
                        .items(items)
                        .backUrls(backUrls)
                        .externalReference("USER_" + paymentData.getUserId() + "_EVENT_" + paymentData.getEventId() + "_QTY_" + paymentData.getQuantity())
                        .build();
                System.out.println("Configuración: Desarrollo sin autoReturn");
            }

            // Crear preferencia con el SDK de MercadoPago
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getId();  // Devolver solo el preferenceId

        } catch (MPApiException apiException) {
            System.err.println("MercadoPago API Error: " + apiException.getApiResponse().getContent());
            return "Error creating payment preference: " + apiException.getApiResponse().getContent();
        } catch (MPException mpException) {
            System.err.println("MercadoPago SDK Error: " + mpException.getMessage());
            return "Error creating payment preference: " + mpException.getMessage();
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            return "Error creating payment preference: " + e.getMessage();
        }
    }

    /**
     * Método helper para obtener variables de entorno
     * Intenta obtener de variables del sistema primero, luego de .env si existe
     */
    private String getEnvironmentVariable(String devName, String prodName) {
        // Primero intentar variables de entorno del sistema (producción)
        String value = System.getenv(prodName);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        
        value = System.getenv(devName);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        
        // Si no están en el sistema, intentar cargar desde .env (desarrollo)
        try {
            Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
            
            value = dotenv.get(prodName);
            if (value != null && !value.isEmpty()) {
                return value;
            }
            
            return dotenv.get(devName);
        } catch (Exception e) {
            // Si no se puede cargar .env, simplemente retornar null
            System.out.println("No se pudo cargar archivo .env: " + e.getMessage());
            return null;
        }
    }

    /**
     * Endpoint para procesar el éxito del pago y crear tickets automáticamente
     * MercadoPago envía los parámetros como GET
     */
    @GetMapping("/payment-success")
    public ResponseEntity<?> processPaymentSuccess(@RequestParam(required = false) String collection_id,
                                                  @RequestParam(required = false) String collection_status,
                                                  @RequestParam(required = false) String external_reference,
                                                  @RequestParam(required = false) String payment_id,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String merchant_order_id) {
        try {
            System.out.println("Procesando pago exitoso:");
            System.out.println("Collection ID: " + collection_id);
            System.out.println("Collection Status: " + collection_status);
            System.out.println("Payment ID: " + payment_id);
            System.out.println("Status: " + status);
            System.out.println("External Reference: " + external_reference);
            System.out.println("Merchant Order ID: " + merchant_order_id);

            // Determinar el estado del pago y el ID de referencia
            String paymentStatus = collection_status != null ? collection_status : status;
            String reference = external_reference;

            // Verificar que tenemos la información necesaria
            if (reference == null || reference.isEmpty()) {
                String frontUri = getEnvironmentVariable("FRONT_URI", "FRONTEND_URL");
                return ResponseEntity.status(302)
                    .header("Location", frontUri + "/payment-failed?error=no_reference")
                    .body("No se encontró referencia externa");
            }

            // Verificar que el pago fue aprobado
            if (paymentStatus == null || (!paymentStatus.equals("approved") && !paymentStatus.equals("success"))) {
                String frontUri = getEnvironmentVariable("FRONT_URI", "FRONTEND_URL");
                return ResponseEntity.status(302)
                    .header("Location", frontUri + "/payment-failed?error=not_approved&status=" + paymentStatus)
                    .body("Pago no fue aprobado: " + paymentStatus);
            }

            // Extraer información del external_reference: "USER_123_EVENT_456_QTY_2"
            String[] parts = reference.split("_");
            if (parts.length != 6 || !"USER".equals(parts[0]) || !"EVENT".equals(parts[2]) || !"QTY".equals(parts[4])) {
                String frontUri = getEnvironmentVariable("FRONT_URI", "FRONTEND_URL");
                return ResponseEntity.status(302)
                    .header("Location", frontUri + "/payment-failed?error=invalid_reference&ref=" + reference)
                    .body("Formato de referencia externa inválido: " + reference);
            }

            Long userId = Long.parseLong(parts[1]);
            Long eventId = Long.parseLong(parts[3]);
            Integer quantity = Integer.parseInt(parts[5]);

            // Crear el ticket usando el servicio existente con la cantidad correcta
            PurchaseTicketDTO purchaseDTO = new PurchaseTicketDTO(eventId, userId, quantity);
            TicketDTO ticket = ticketService.purchaseTicket(purchaseDTO);
            
            // Intentar enviar notificación, pero no fallar si hay error
            try {
                notificationService.sendPurchaseSuccessNotification(userId, eventId);
                System.out.println("Notificación enviada exitosamente");
            } catch (Exception notifError) {
                System.err.println("Error al enviar notificación (no crítico): " + notifError.getMessage());
                // Continuamos el flujo aunque falle la notificación
            }
            
            System.out.println("Ticket creado exitosamente: " + ticket.getId());
            
            // Redirigir al frontend con éxito
            String frontUri = getEnvironmentVariable("FRONT_URI", "FRONTEND_URL");
            return ResponseEntity.status(302)
                .header("Location", frontUri + "/payment-success?ticketId=" + ticket.getId())
                .body("Ticket creado exitosamente");

        } catch (Exception e) {
            System.err.println("Error procesando pago exitoso: " + e.getMessage());
            e.printStackTrace();
            
            String frontUri = getEnvironmentVariable("FRONT_URI", "FRONTEND_URL");
            return ResponseEntity.status(302)
                .header("Location", frontUri + "/payment-failed")
                .body("Error interno del servidor");
        }
    }

    /**
     * Endpoint de debug para ver todos los parámetros que envía MercadoPago
     */
    @GetMapping("/payment-debug")
    public ResponseEntity<String> debugPayment(@RequestParam java.util.Map<String, String> allParams) {
        StringBuilder response = new StringBuilder("Parámetros recibidos:\n");
        for (java.util.Map.Entry<String, String> entry : allParams.entrySet()) {
            response.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        System.out.println("DEBUG - " + response.toString());
        return ResponseEntity.ok(response.toString());
    }
}
