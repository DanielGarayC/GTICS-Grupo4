package com.example.gtics.controller;

import com.example.gtics.DNIAPI;
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
    @GetMapping({"/Registro"})
    public String Registro(@ModelAttribute("usuario") Usuario usuario, Model model){
        List<Distrito> distritos = distritoRepository.findAll();
        distritos.sort(new Comparator<Distrito>() {
            @Override
            public int compare(Distrito d1, Distrito d2) {
                return d1.getNombre().compareTo(d2.getNombre());
            }
        });
        model.addAttribute("distritos", distritos);
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

    @GetMapping("/validateCurrentPassword")
    public ResponseEntity<String> validateCurrentPassword(@RequestParam String currentPassword, HttpSession session) {
        // Obtener el idAgente de la sesión
        Integer id = (Integer) session.getAttribute("id");

        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }

        // Recuperar el usuario desde la base de datos utilizando el idAgente
        Optional<Usuario> optUsuario = usuarioRepository.findById(id);

        if (!optUsuario.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        Usuario usuario = optUsuario.get();

        // Aquí obtienes la contraseña hasheada del usuario
        String storedHashedPassword = usuario.getContrasena();

        // Verificar si la contraseña proporcionada coincide con la almacenada
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean isPasswordMatch = passwordEncoder.matches(currentPassword, storedHashedPassword);

        if (isPasswordMatch) {
            return ResponseEntity.ok("Contraseña válida.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("La contraseña actual es incorrecta.");
        }
    }
    @PostMapping({"/CambiarContra"})
    public String CambiarContrasena(@RequestParam("newPassword") String newPassword,
                                    @RequestParam("currentPassword") String currentPassword,
                                    @RequestParam("confirmNewPassword") String confirmNewPassword,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes){

        Integer id = (Integer) session.getAttribute("id");
        if (id == null) {
            return "redirect:/ExpressDealsLogin"; // Redirige a login si no hay sesión
        }

        Optional<Usuario> optUsuario = usuarioRepository.findById(id);
        if (!optUsuario.isPresent()) {
            return "redirect:/ExpressDealsLogin"; // Redirige a login si no hay usuario
        }

        Usuario usuario = optUsuario.get();
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
            redirectAttributes.addFlashAttribute("error", "Contraseña actual erronea");
            return viewName;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("error", "Ha ingresado contraseñas distintas, verifique los datos ingresados");
            return viewName;
        }
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        Integer idUsuario = usuario.getId();
        // Actualizar la contraseña
        usuarioRepository.cambiarContrasena(idUsuario, hashedNewPassword);

        // Mensaje de éxito
        redirectAttributes.addFlashAttribute("success", "Contraseña actualizada correctamente"); // o "error"

        return viewName;
    }

    @PostMapping("/Registrar")
    public String registrarUsuario(Model model, @Validated(RegistroUsuarioValidationGroup.class) Usuario usuario, BindingResult bindingResult, @RequestParam("passwordConfirm") String passwordConfirm, RedirectAttributes attr){
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
            return "redirect:/Registro";
        }
        //Verificacion del dni con la api
        List<String> datosRENIEC = DNIAPI.getDni(usuario.getDni());

        if (!datosRENIEC.isEmpty()){
            String apiDni = datosRENIEC.get(3);
            String apiNombres = datosRENIEC.get(0);
            String apiApellidoP = datosRENIEC.get(1);
            String apiApellidoM = datosRENIEC.get(2);
            if(apiNombres.isEmpty()){
                String dniError = "El DNI no existe";
                System.out.println(dniError);
                attr.addFlashAttribute("dniError", dniError);
                return "redirect:/Registro";

            }else{
                System.out.println("Datos de la persona");
                System.out.println("DNI: " + apiDni);
                System.out.println("Nombres: " + apiNombres);
                System.out.println("Apellidos: " + apiApellidoP + " " + apiApellidoM);
                if (!usuario.getApellidoPaterno().equals(apiApellidoP) || !usuario.getApellidoMaterno().equals(apiApellidoM)){
                    String dpError = "Los datos ingresados no coinciden con la data de la RENIEC";
                    System.out.println(dpError);
                    attr.addFlashAttribute("datosPersonalesError", dpError);
                    return "redirect:/Registro";

                }
            }
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
            String contrasenaLimitada = nuevaContrasena.substring(0, Math.min(nuevaContrasena.length(), 12));
            String encryptedPassword = passwordEncoder.encode(contrasenaLimitada);
            emailService.passwordRecoveryEmail(email,user.getNombre(),contrasenaLimitada);
            usuarioRepository.cambiarContrasena(user.getId(), encryptedPassword);
            return "redirect:/ExpressDealsLogin";
        }else{
            model.addAttribute("errorMsg","No se ha encontrado este correo en base de datos.");
            model.addAttribute("email", email);
            return "Sistema/recvPass";
        }
    }

    public String generateRandomPassword() {
        int length = 12;
        StringBuilder password = new StringBuilder();

        password.append(NUMBERS.charAt(random.nextInt(NUMBERS.length()))); // 1 número
        password.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length()))); // 1er carácter especial
        password.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length()))); // 2do carácter especial
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length()))); // 1 letra

        while (password.length() < 8) {
            password.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }

        while (password.length() < length) {
            password.append(NUMBERS + LOWERCASE + UPPERCASE + SPECIAL_CHARACTERS)
                    .charAt(random.nextInt(NUMBERS.length() + LOWERCASE.length() + UPPERCASE.length() + SPECIAL_CHARACTERS.length()));
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
