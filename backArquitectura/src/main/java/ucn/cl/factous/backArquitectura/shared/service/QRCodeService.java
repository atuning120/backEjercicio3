package ucn.cl.factous.backArquitectura.shared.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

@Service
public class QRCodeService {

    public String generateQRCode(String ticketData) {
        try {
            // Configurar el QR
            int width = 200;
            int height = 200;
            
            // Generar el QR
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                ticketData, 
                BarcodeFormat.QR_CODE, 
                width, 
                height
            );
            
            // Convertir a imagen
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // Convertir a Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (Exception e) {
            throw new RuntimeException("Error generando QR Code: " + e.getMessage());
        }
    }

    public String generateTicketQRData(Long ticketId, String eventName, String userName, String eventDate) {
        // Formato del contenido del QR
        return String.format(
            "TICKET_ID:%d|EVENT:%s|USER:%s|DATE:%s|VALIDATION_CODE:%s",
            ticketId,
            eventName,
            userName,
            eventDate,
            generateValidationCode(ticketId)
        );
    }

    private String generateValidationCode(Long ticketId) {
        // Generar código de validación único
        return "VAL" + ticketId + System.currentTimeMillis() % 10000;
    }
}
