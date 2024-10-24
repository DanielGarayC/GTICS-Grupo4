package com.example.gtics.ValidationGroup.Validators;

import com.example.gtics.ValidationGroup.CustomAnnotations.UniqueEmail;
import com.example.gtics.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {


    final UsuarioRepository usuarioRepository;

    public UniqueEmailValidator(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isEmpty()) {
            return true; // Permitir que otras validaciones manejen el caso de campo vacío
        }
        boolean emailExists = usuarioRepository.existsByEmail(email);
        System.out.println("Email ingresado: " + email);
        System.out.println("¿El correo existe? " + emailExists);
        return !emailExists;
    }

}

