package com.example.gtics.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String redirectUrl = determineRedirectUrl(authentication);
        response.sendRedirect(redirectUrl);
    }

    private String determineRedirectUrl(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Super Admin"))) {
            return "/SuperAdmin";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Usuario Final"))) {
            return "/UsuarioFinal";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Administrador Zonal"))) {
            return "/AdminZonal";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Agente"))) {
            return "/Agente";
        } else {
            return "/default";
        }
    }
}
