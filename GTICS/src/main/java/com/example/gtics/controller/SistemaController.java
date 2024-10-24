package com.example.gtics.controller;

import com.example.gtics.ValidationGroup.RegistroUsuarioValidationGroup;
import com.example.gtics.entity.Distrito;
import com.example.gtics.entity.Rol;
import com.example.gtics.entity.Usuario;
import com.example.gtics.entity.Zona;
import com.example.gtics.repository.DistritoRepository;
import com.example.gtics.repository.RolRepository;
import com.example.gtics.repository.UsuarioRepository;
import com.example.gtics.repository.ZonaRepository;
import com.example.gtics.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;

@Controller
public class SistemaController {
    final DistritoRepository distritoRepository;
    final UsuarioRepository usuarioRepository;
    final RolRepository rolRepository;
    final ZonaRepository zonaRepository;

    private static final String NUMBERS = "0123456789";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/";
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


    public SistemaController(DistritoRepository distritoRepository, UsuarioRepository usuarioRepository, RolRepository rolRepository, ZonaRepository zonaRepository) {
        this.distritoRepository = distritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.zonaRepository = zonaRepository;
    }

    @GetMapping({"/ExpressDealsLogin","/", "/Login"})
    public String Login(){

        return "Sistema/login";
    }
    @GetMapping({"Registro"})
    public String Registro(Model model){
        Usuario usuario = new Usuario();
        List<Distrito> distritos = distritoRepository.findAll();
        distritos.sort(new Comparator<Distrito>() {
            @Override
            public int compare(Distrito d1, Distrito d2) {
                return d1.getNombre().compareTo(d2.getNombre());
            }
        });
        model.addAttribute("distritos", distritos);
        model.addAttribute("usuario", usuario);
        return "Sistema/register";
    }
    @GetMapping({"RecuperarContra"})
    public String RecuperarContrasena(){

        return "Sistema/recvPass";
    }
    @GetMapping({"RecuperarContra/Token"})
    public String TokenValidacion(){

        return "Sistema/validarToken";
    }
    @GetMapping({"RecuperarContra/CambiarContra"})
    public String CambiarContrasena(){

        return "Sistema/chPass";
    }

    @PostMapping("/validatePassword")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> validatePassword(@RequestBody Map<String, String> body, Principal principal) {
        String currentPassword = body.get("currentPassword");

        // Obtener el usuario logueado
        UserDetails userDetails = (UserDetails) ((Authentication) principal).getPrincipal();
        String loggedUserPassword = userDetails.getPassword(); // Contraseña encriptada

        System.out.println("Contraseña ingresada: " + currentPassword);
        System.out.println("Contraseña almacenada: " + loggedUserPassword);
        // Verificar la contraseña ingresada
        boolean isValid = passwordEncoder.matches(currentPassword, loggedUserPassword);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
    @PostMapping({"CambiarContra"})
    public String CambiarContrasena(@RequestParam("newPassword") String newPassword,
                                    @RequestParam("currentPassword") String currentPassword,
                                    @RequestParam("confirmNewPassword") String confirmNewPassword,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes){

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Rol rol = usuario.getRol();
        String viewName = "";
        switch(rol.getId()) {
            case 1: //no pasa nd
                viewName = "redirect:/ExpressDealsLogin";
                break;
            case 2:
                viewName ="redirect:/AdminZonal/Perfil";
                break;
            case 3:
                viewName ="redirect:/Agente/perfil";
                break;
            case 4:
                viewName ="redirect:/UsuarioFinal/miPerfil";
                break;
            default:
                viewName = "redirect:/default"; // Opcional: manejar un caso por defecto
                break;
        }

        String hashedCurrentPassword = usuario.getContrasena();
        // Verificar la contraseña actual ingresada (usaron BCrypt asi q yo tmb)
        if (!BCrypt.checkpw(currentPassword, hashedCurrentPassword)) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta.");
            return viewName;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las nuevas contraseñas no coinciden.");
            return viewName;
        }
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        Integer idUsuario = usuario.getId();
        // Actualizar la contraseña
        usuarioRepository.cambiarContrasena(idUsuario, hashedNewPassword);

        // Mensaje de éxito
        redirectAttributes.addFlashAttribute("success", "Contraseña cambiada con éxito.");
        return viewName;
    }

    @PostMapping("/Registrar")
    public String registrarUsuario(Model model, @Validated(RegistroUsuarioValidationGroup.class) Usuario usuario, BindingResult bindingResult, @RequestParam("passwordConfirm") String passwordConfirm){
        if(bindingResult.hasErrors() || !passwordConfirm.equals(usuario.getContrasena())){
            model.addAttribute("usuario", usuario);
            List<Distrito> distritos = distritoRepository.findAll();
            distritos.sort(new Comparator<Distrito>() {
                @Override
                public int compare(Distrito d1, Distrito d2) {
                    return d1.getNombre().compareTo(d2.getNombre());
                }
            });
            model.addAttribute("distritos", distritos);
            if (!passwordConfirm.equals(usuario.getContrasena())){
                model.addAttribute("diferentPassword", "Las contraseñas deben ser iguales");
            }
            return "Sistema/register";
        }
        Rol rol = new Rol();
        rol.setId(4);
        rol.setNombreRol("Usuario Final");
        usuario.setRol(rol);
        usuario.setBaneado(false);
        usuario.setActivo(1);
        List<Zona> zonas = zonaRepository.findAll();
        List<Distrito> distritos = distritoRepository.findAll();
        for (Distrito distrito : distritos){
            if (Objects.equals(distrito.getId(), usuario.getDistrito().getId())){
                for (Zona zona : zonas){
                    if (Objects.equals(zona.getId(), distrito.getZona().getId())){
                        usuario.setZona(zona);
                    }
                }
            }
        }
        String passwordParaEnviar = usuario.getContrasena();
        String encryptedPassword = passwordEncoder.encode(usuario.getContrasena());
        usuario.setContrasena(encryptedPassword);

        emailService.sendVerificationEmail(usuario.getEmail(), usuario.getNombre(), passwordParaEnviar);

        usuarioRepository.save(usuario);
        return "redirect:/ExpressDealsLogin";
    }

    @PostMapping("/enviarNuevaContra")
    public String recuperarPassword(@RequestParam("email") String email, Model model){
        List<Usuario> usuarios = usuarioRepository.findAll();
        boolean existe = false;
        Usuario user = new Usuario();
        for (Usuario u: usuarios){
            if (u.getEmail().equals(email)){
                user = u;
                existe = true;
                break;
            }
        }
        if (existe){
            String nuevaContrasena = generateRandomPassword();
            String encryptedPassword = passwordEncoder.encode(nuevaContrasena);
            emailService.passwordRecoveryEmail(email,user.getNombre(),nuevaContrasena);
            usuarioRepository.cambiarContrasena(user.getId(), encryptedPassword);
            return "redirect:/ExpressDealsLogin";
        }else{
            model.addAttribute("errorMsg","No se ha encontrado este correo en base de datos.");
            model.addAttribute("email", email);
            return "Sistema/recvPass";
        }
    }

    private String generateRandomPassword() {
        int length = random.nextInt(9) + 8;
        StringBuilder password = new StringBuilder();

        password.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        password.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));
        password.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));

        while (password.length() < length) {
            password.append(NUMBERS + LOWERCASE + UPPERCASE + SPECIAL_CHARACTERS).charAt(random.nextInt(NUMBERS.length() + LOWERCASE.length() + UPPERCASE.length() + SPECIAL_CHARACTERS.length()));
        }
        return shuffleString(password.toString());
    }

    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }

}
