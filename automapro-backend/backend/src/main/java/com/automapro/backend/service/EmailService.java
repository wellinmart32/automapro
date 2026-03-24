package com.automapro.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * Servicio para envío de emails via Gmail SMTP
 */
@Service
public class EmailService {

    @Value("${gmail.username}")
    private String gmailUsername;

    @Value("${gmail.app.password}")
    private String gmailAppPassword;

    /**
     * Envía email de recuperación de contraseña
     */
    public void enviarEmailRecuperacion(String emailDestino, String nombre, String token) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(gmailUsername, gmailAppPassword);
                }
            });

            String urlReset = "https://automapro-frontend.vercel.app/reset-password?token=" + token;

            String htmlContent = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                        <div style="background-color: #0d6efd; padding: 30px; text-align: center;">
                            <h1 style="color: white; margin: 0;">AutomaPro</h1>
                        </div>
                        <div style="padding: 30px; background-color: #f8f9fa;">
                            <h2 style="color: #333;">Recuperar Contraseña</h2>
                            <p>Hola <strong>%s</strong>,</p>
                            <p>Recibimos una solicitud para restablecer tu contraseña.</p>
                            <p>Haz clic en el botón de abajo para crear una nueva contraseña:</p>
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s"
                                   style="background-color: #0d6efd; color: white; padding: 14px 28px;
                                          text-decoration: none; border-radius: 6px; font-weight: bold;">
                                    Restablecer Contraseña
                                </a>
                            </div>
                            <p style="color: #666; font-size: 14px;">
                                Este enlace expira en <strong>30 minutos</strong>.
                            </p>
                            <p style="color: #666; font-size: 14px;">
                                Si no solicitaste esto, ignora este email.
                            </p>
                        </div>
                        <div style="padding: 20px; text-align: center; color: #999; font-size: 12px;">
                            © 2025 AutomaPro — Sistema de Automatización
                        </div>
                    </div>
                    """.formatted(nombre, urlReset);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(gmailUsername, "AutomaPro"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
            message.setSubject("Recuperar contraseña - AutomaPro");
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Email enviado a: " + emailDestino);

        } catch (Exception e) {
            System.err.println("❌ Error enviando email: " + e.getMessage());
            throw new RuntimeException("Error al enviar email: " + e.getMessage());
        }
    }
}