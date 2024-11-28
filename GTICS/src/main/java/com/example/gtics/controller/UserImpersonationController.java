package com.example.gtics.controller;

import com.example.gtics.entity.Usuario;
import com.example.gtics.repository.UsuarioRepository;
import com.example.gtics.repository.UsuarioSessionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class UserImpersonationController {
    final UsuarioRepository usuarioRepository;
    final UsuarioSessionRepository usuarioSessionRepository;

    public UserImpersonationController(UsuarioRepository usuarioRepository,
                                       UsuarioSessionRepository usuarioSessionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioSessionRepository = usuarioSessionRepository;
    }

    @GetMapping("/SuperAdmin/impersonateUser/{userId}")
    public String impersonateUser(@PathVariable Integer userId, HttpSession session) {
        // Verificar que el usuario actual es Super Admin
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSuperAdmin = currentAuth.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("Super Admin"));

        if (!isSuperAdmin) {
            return "redirect:/access-denied";
        }

        // Buscar el usuario a suplantar
        Usuario userToImpersonate = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear una nueva autenticación con los permisos del usuario
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(userToImpersonate.getRol().getNombreRol()));

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                userToImpersonate.getEmail(),
                userToImpersonate.getContrasena(),
                authorities
        );

        // Establecer la nueva autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // Guardar información de suplantación en la sesión
        session.setAttribute("originalUser", currentAuth.getName());
        session.setAttribute("isImpersonating", true);

        // Redirigir según el rol del usuario suplantado
        String rolNombre = userToImpersonate.getRol().getNombreRol();
        switch (rolNombre) {
            case "Usuario Final":
                return "redirect:/UsuarioFinal";
            case "Administrador Zonal":
                return "redirect:/AdminZonal";
            case "Agente":
                return "redirect:/Agente";
            default:
                return "redirect:/";
        }
    }

    @GetMapping("/revertImpersonation")
    public String revertImpersonation(HttpSession session) {
        // Verificar si hay una suplantación activa
        Boolean isImpersonating = (Boolean) session.getAttribute("isImpersonating");
        if (isImpersonating != null && isImpersonating) {
            // Recuperar el usuario original
            String originalUsername = (String) session.getAttribute("originalUser");

            // Cargar el usuario original
            Optional<Usuario> originalUser = usuarioRepository.findByEmail(originalUsername);

            // Crear una nueva autenticación para el Super Admin
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("Super Admin"));

            Authentication originalAuth = new UsernamePasswordAuthenticationToken(
                    originalUser.get().getEmail(),
                    originalUser.get().getContrasena(),
                    authorities
            );

            // Restablecer el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(originalAuth);

            // Limpiar atributos de sesión
            session.removeAttribute("originalUser");
            session.removeAttribute("isImpersonating");

            // Redirigir a la página de Super Admin
            return "redirect:/SuperAdmin";
        }

        // Si no hay suplantación, redirigir a inicio
        return "redirect:/";
    }
}
