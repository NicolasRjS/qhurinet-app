package com.upc.qhurinet.serviceimpl;
import com.upc.qhurinet.services.CorreoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestClientException;
@Service @Slf4j
public class CorreoServiceImpl implements CorreoService {
    @Autowired private org.springframework.beans.factory.ObjectProvider<JavaMailSender> remitente;
    @Value("${correo.modo:smtp}") private String modo;
    @Value("${correo.remitente:}") private String desde;
    @Value("${correo.verificacion-url:http://localhost:4200/verify-email}") private String url;
    @Override public void enviarVerificacion(String email, String token) {
        if ("log".equals(modo)) {
            log.info("Token de verificación de correo para {}: {}", email, token);
            return;
        }
        JavaMailSender sender = remitente.getIfAvailable();
        if (sender == null || desde.isBlank()) throw new RestClientException("Correo no configurado");
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(desde); mensaje.setTo(email); mensaje.setSubject("Verifica tu cuenta QhuriNet");
        mensaje.setText("Confirma tu correo: " + url + "?token=" + token);
        try { sender.send(mensaje); }
        catch (org.springframework.mail.MailException ex) { throw new RestClientException("No se pudo enviar la verificación"); }
    }
}
