package com.petadopt.infrastructure.adapter.out.mail;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpEmailAdapterTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final SmtpEmailAdapter adapter = new SmtpEmailAdapter(mailSender, "PetAdopt <no-reply@petadopt.local>");

    @Test
    void arma_el_mensaje_con_remitente_destinatario_asunto_y_cuerpo() {
        adapter.enviar("ana@test.com", "Asunto", "Cuerpo del correo");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage m = captor.getValue();
        assertThat(m.getFrom()).isEqualTo("PetAdopt <no-reply@petadopt.local>");
        assertThat(m.getTo()).containsExactly("ana@test.com");
        assertThat(m.getSubject()).isEqualTo("Asunto");
        assertThat(m.getText()).isEqualTo("Cuerpo del correo");
    }

    @Test
    void un_fallo_del_servidor_smtp_no_se_propaga() {
        doThrow(new MailSendException("Connection refused")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> adapter.enviar("ana@test.com", "Asunto", "Cuerpo"));
    }
}
