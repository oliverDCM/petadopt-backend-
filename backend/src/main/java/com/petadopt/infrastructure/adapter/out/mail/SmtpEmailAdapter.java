package com.petadopt.infrastructure.adapter.out.mail;

import com.petadopt.application.port.out.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Envía los correos por SMTP en un hilo aparte (@Async), así la petición HTTP no espera al servidor de correo.
 * Un fallo se registra en el log sin destinatario ni contenido, y nunca se propaga a quien llamó.
 */
@Component
public class SmtpEmailAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailAdapter.class);

    private final JavaMailSender mailSender;
    private final String remitente;

    public SmtpEmailAdapter(JavaMailSender mailSender, @Value("${app.mail.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Async
    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        try {
            mailSender.send(mensaje);
            log.info("Correo enviado: '{}'", asunto);
        } catch (MailException e) {
            log.error("No se pudo enviar el correo '{}' ({})", asunto, e.getClass().getSimpleName());
        }
    }
}
