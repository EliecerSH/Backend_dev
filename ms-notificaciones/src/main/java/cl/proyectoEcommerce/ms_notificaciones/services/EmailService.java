package cl.proyectoEcommerce.ms_notificaciones.services;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String remitente;
    private final boolean modoSimulado;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${notificaciones.email.remitente}") String remitente,
            @Value("${notificaciones.email.modo-simulado}") boolean modoSimulado) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.modoSimulado = modoSimulado;
    }

    // Notificación disparada por el evento orden.creada. En este entregable no
    // existe
    // un lookup del correo real del usuario a partir de su Azure OID, así que se
    // deja
    // la evidencia en el log; en producción aquí se resolvería el email vía
    // ms-usuarios
    // y se delegaría a enviarCorreo(...).
    public void enviarNotificacionOrden(String usuarioOid, String asunto, String cuerpo) {
        log.info("[NOTIFICACIÓN DE COMPRA] usuario={} asunto='{}' -> {}", usuarioOid, asunto, cuerpo);
        if (modoSimulado) {
            log.info("Modo simulado activo (NOTIFICACIONES_MODO_SIMULADO=true): no se envía correo real.");
        }
    }

    // Envío directo usado por el endpoint de prueba
    // /api/v1/notificaciones/test-email (Postman)
    public void enviarCorreo(String destinatario, String asunto, String mensaje) {
        if (modoSimulado) {
            log.info("[MODO SIMULADO] Correo a {} | Asunto: {} | Mensaje: {}", destinatario, asunto, mensaje);
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje);
            mailSender.send(mimeMessage);
            log.info("Correo enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo: " + e.getMessage());
        }
    }
}
