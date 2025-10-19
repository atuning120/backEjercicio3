package ucn.cl.factous.backArquitectura.modules.notification.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ucn.cl.factous.backArquitectura.modules.notification.dto.NotificationDTO;
import ucn.cl.factous.backArquitectura.modules.notification.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = {"${FRONT_URI}", "${FRONT_URI_ALTERNATIVE}"})
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long userId) {
        if ( userId == null || userId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        List<NotificationDTO> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable("id") Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase-success")
    public ResponseEntity<String> sendPurchaseSuccess(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = ((Number) payload.get("userId")).longValue();
            Long eventId = ((Number) payload.get("eventId")).longValue();
            notificationService.sendPurchaseSuccessNotification(userId, eventId);
            return ResponseEntity.ok("Notificación de compra enviada");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar la notificación: " + e.getMessage());
        }
    }

    @PostMapping("/event-deleted")
    public ResponseEntity<String> sendEventDeleted(@RequestBody Map<String, Object> payload) {
        try {
            Long eventId = ((Number) payload.get("eventId")).longValue();
            notificationService.sendEventDeletedNotification(eventId);
            return ResponseEntity.ok("Notificación de evento eliminado enviada");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar la notificación: " + e.getMessage());
        }
    }

    @PostMapping("/organizer-message")
    public ResponseEntity<String> sendOrganizerMessage(@RequestBody Map<String, Object> payload) {
        try {
            Long eventId = ((Number) payload.get("eventId")).longValue();
            String customMessage = (String) payload.get("customMessage");
            notificationService.sendOrganizerMessageNotification(eventId, customMessage);
            return ResponseEntity.ok("Notificación de mensaje de organizador enviada");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar la notificación: " + e.getMessage());
        }
    }

    @PostMapping("/general")
    public ResponseEntity<String> sendGeneral(@RequestBody NotificationDTO dto) {
        try {
            notificationService.sendGeneralNotification(dto.getType(), dto.getTitle(), dto.getMessage());
            return ResponseEntity.ok("Notificación general enviada y persistida.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar la notificación general: " + e.getMessage());
        }
    }
}