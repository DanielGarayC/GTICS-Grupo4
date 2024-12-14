package com.example.gtics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendVerificationEmail(String to, String username, String password) {
        String subject = "Confirmación de creación de cuenta";
        String text = "Hola " + username + ",\n\n" +
                "Tu cuenta ha sido creada exitosamente.\n" +
                "A continuación, tus credenciales:\n" +
                "Usuario: " + to + "\n" +
                "Contraseña: " + password + "\n\n" +

                "¡Bienvenido a ExpressDeals!";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void passwordRecoveryEmail(String to, String username, String password) {
        String subject = "Solicitud de recuperación de contraseña ";
        String text = "Hola " + username + ",\n\n" +
                "Se ha cambiado exitosamente tu contraseña.\n"+
                "A continuación, tus credenciales:\n" +
                "Usuario: " + to + "\n" +
                "Contraseña: " + password + "\n\n";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void actualizacionInfoUserGenerico(String to, String username, ArrayList<String> camposModificados,
                                        ArrayList<String> datosAntiguos, ArrayList<String> datosNuevos) {
        String subject = "Actualización de información de tu cuenta";
        StringBuilder text = new StringBuilder();
        text.append("Hola ").append(username).append(",\n\n")
                .append("Se han realizado algunos cambios en la información de tu cuenta.\n");

        if (camposModificados.isEmpty()) {
            text.append("No se han hecho cambios.\n");
        } else {
            text.append("A continuación, se presentan las modificaciones: \n\n");

            for (int i = 0; i < camposModificados.size(); i++) {
                text.append("- ").append(camposModificados.get(i)).append(": \n")
                        .append("   Antes: ").append(datosAntiguos.get(i)).append("\n")
                        .append("   Ahora: ").append(datosNuevos.get(i)).append("\n\n");
            }
        }


        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text.toString());
        emailSender.send(message);
    }


}
