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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;

@Controller
public class SistemaController {
    final DistritoRepository distritoRepository;
    final UsuarioRepository usuarioRepository;
    final RolRepository rolRepository;
    final ZonaRepository zonaRepository;

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

    @GetMapping({"/ExpressDealsLogin","/"})
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

    @PostMapping("/Registrar")
    public String registrarUsuario(Model model, @Validated(RegistroUsuarioValidationGroup.class) Usuario usuario, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            model.addAttribute("usuario", usuario);
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

}
