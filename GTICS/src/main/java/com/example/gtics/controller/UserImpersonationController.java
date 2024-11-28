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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

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
        System.out.println(userToImpersonate.getNombre());

        // Crear detalles de usuario con autoridades basadas en su rol
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(userToImpersonate.getRol().getNombreRol()));

        // Crear UserDetails
        UserDetails userDetails = User.withUsername(userToImpersonate.getEmail())
                .password(userToImpersonate.getContrasena())
                .authorities(authorities)
                .build();

        // Crear nueva autenticación con los detalles del usuario suplantado
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
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
            Usuario originalUser = usuarioRepository.findByEmail(originalUsername)
                    .orElseThrow(() -> new RuntimeException("Usuario original no encontrado"));

            // Crear detalles de usuario con autoridades de Super Admin
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("Super Admin"));

            // Crear UserDetails para el Super Admin
            UserDetails superAdminDetails = User.withUsername(originalUser.getEmail())
                    .password(originalUser.getContrasena())
                    .authorities(authorities)
                    .build();

            // Crear nueva autenticación para el Super Admin
            Authentication originalAuth = new UsernamePasswordAuthenticationToken(
                    superAdminDetails,
                    superAdminDetails.getPassword(),
                    superAdminDetails.getAuthorities()
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
