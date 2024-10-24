package com.example.gtics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}
