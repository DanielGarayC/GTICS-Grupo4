package com.example.gtics.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    private static final String LOGO_PATH = "static/images/logo/logoGTICSv2.png";

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Adjuntar imagen embebida
            ClassPathResource resource = new ClassPathResource(LOGO_PATH);
            helper.addInline("logoImage", resource);

            emailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    public void sendVerificationEmail(String to, String username, String password) {
        String subject = "Confirmación de creación de cuenta";
        String htmlContent = "<p>Hola <b>" + username + "</b>,</p>" +
                "<p>Tu cuenta ha sido creada exitosamente.</p>" +
                "<ul>" +
                "<li><b>Usuario:</b> " + to + "</li>" +
                "<li><b>Contraseña:</b> " + password + "</li>" +
                "</ul>" +
                "<p>¡Bienvenido a ExpressDeals!</p>" +
                "<img src='cid:logoImage' alt='Logo' style='width:200px;'>";

        sendHtmlEmail(to, subject, htmlContent);
    }


    public void emailParaAdminZonal(String to, String username, String password, String zona) {
        String subject = "Confirmación de creación de cuenta";
        String htmlContent = "<p>Hola <b>" + username + "</b>,</p>" +
                "<p>Has sido asignado como Administrador Zonal.</p>" +
                "<ul>" +
                "<li><b>Zona:</b> " + zona + "</li>" +
                "<li><b>Usuario:</b> " + to + "</li>" +
                "<li><b>Contraseña:</b> " + password + "</li>" +
                "</ul>" +
                "<p>¡Bienvenido a ExpressDeals!</p>" +
                "<img src='cid:logoImage' alt='Logo' style='width:200px;'>";

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void emailParaAgente(String to, String username, String password, String zona, String nombres, String apellido) {
        String subject = "Confirmación de creación de cuenta";
        String htmlContent = "<p>Hola <b>" + username + "</b>,</p>" +
                "<p>Has sido asignado como Agente.</p>" +
                "<ul>" +
                "<li><b>Administrador Zonal:</b> " + nombres + " " + apellido + "</li>" +
                "<li><b>Zona:</b> " + zona + "</li>" +
                "<li><b>Usuario:</b> " + to + "</li>" +
                "<li><b>Contraseña:</b> " + password + "</li>" +
                "</ul>" +
                "<p>¡Bienvenido a ExpressDeals!</p>" +
                "<img src='cid:logoImage' alt='Logo' style='width:200px;'>";

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void passwordRecoveryEmail(String to, String username, String password) {
        String subject = "Solicitud de recuperación de contraseña";
        String htmlContent = "<p>Hola <b>" + username + "</b>,</p>" +
                "<p>Se ha cambiado exitosamente tu contraseña.</p>" +
                "<ul>" +
                "<li><b>Usuario:</b> " + to + "</li>" +
                "<li><b>Contraseña:</b> " + password + "</li>" +
                "</ul>" +
                "<p>¡Gracias por usar ExpressDeals!</p>" +
                "<img src='cid:logoImage' alt='Logo' style='width:200px;'>";

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void actualizacionInfoUserGenerico(String to, String username, ArrayList<String> camposModificados,
                                              ArrayList<String> datosAntiguos, ArrayList<String> datosNuevos) {
        String subject = "Actualización de información de tu cuenta";
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<p>Hola <b>").append(username).append("</b>,</p>")
                .append("<p>Se han realizado algunos cambios en la información de tu cuenta.</p>");

        if (camposModificados.isEmpty()) {
            htmlContent.append("<p>No se han hecho cambios.</p>");
        } else {
            htmlContent.append("<p>A continuación, se presentan las modificaciones:</p>")
                    .append("<ul>");
            for (int i = 0; i < datosNuevos.size(); i++) {
                htmlContent.append("<li><b>").append(camposModificados.get(i)).append(":</b><br>")
                        .append("<b>Antes:</b> ").append(datosAntiguos.get(i)).append("<br>")
                        .append("<b>Ahora:</b> ").append(datosNuevos.get(i)).append("</li>");
            }
            if (datosAntiguos.size() != camposModificados.size()){
                htmlContent.append("<li><b>").append(camposModificados.get(camposModificados.size() - 1)).append("</b><br>");
            }

            htmlContent.append("</ul>");
        }

        htmlContent
                .append("<img src='cid:logoImage' alt='Logo' style='width:250px;'>");

        sendHtmlEmail(to, subject, htmlContent.toString());
    }
}
