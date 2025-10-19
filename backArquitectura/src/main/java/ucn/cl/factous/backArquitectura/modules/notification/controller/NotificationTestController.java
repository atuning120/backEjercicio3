package ucn.cl.factous.backArquitectura.modules.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ucn.cl.factous.backArquitectura.modules.notification.service.NotificationService;

@RestController
@RequestMapping("/test-notifications")
@CrossOrigin(origins = {"${FRONT_URI}", "${FRONT_URI_ALTERNATIVE}"})
public class NotificationTestController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send-test")
    public ResponseEntity<String> sendTestNotification() {
        try {
            System.out.println("=== ENVIANDO NOTIFICACIÓN DE PRUEBA ===");
            notificationService.sendGeneralNotification(
                "test", 
                "Notificación de Prueba", 
                "Esta es una notificación de prueba para verificar WebSocket"
            );
            System.out.println("=== NOTIFICACIÓN DE PRUEBA ENVIADA ===");
            return ResponseEntity.ok("Notificación de prueba enviada");
        } catch (Exception e) {
            System.err.println("Error enviando notificación de prueba: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/send-event-deleted")
    public ResponseEntity<String> sendEventDeletedTest() {
        try {
            System.out.println("=== ENVIANDO NOTIFICACIÓN DE EVENTO ELIMINADO DE PRUEBA ===");
            notificationService.sendEventDeletedNotification(999L);
            System.out.println("=== NOTIFICACIÓN DE EVENTO ELIMINADO ENVIADA ===");
            return ResponseEntity.ok("Notificación de evento eliminado enviada");
        } catch (Exception e) {
            System.err.println("Error enviando notificación de evento eliminado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}