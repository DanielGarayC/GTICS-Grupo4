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
                "Por favor, asegúrate de cambiar tu contraseña después de iniciar sesión.\n" +
                "¡Bienvenido a nuestro sistema!";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
