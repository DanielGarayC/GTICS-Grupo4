package com.example.gtics.controller;

import com.example.gtics.entity.*;
import com.example.gtics.repository.DistritoRepository;
import com.example.gtics.repository.RolRepository;
import com.example.gtics.repository.SolicitudAgenteRepository;
import com.example.gtics.repository.UsuarioRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;


@Controller

public class AdminZonalController {
    private final UsuarioRepository usuarioRepository;
    private final SolicitudAgenteRepository solicitudAgenteRepository;
    private final DistritoRepository distritoRepository;
    private final RolRepository rolRepository;
    public  AdminZonalController(SolicitudAgenteRepository solicitudAgenteRepository, UsuarioRepository usuarioRepository,DistritoRepository distritoRepository,RolRepository rolRepository){
        this.solicitudAgenteRepository = solicitudAgenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.distritoRepository = distritoRepository;
        this.rolRepository = rolRepository;
    }
    @GetMapping({"AdminZonal", "AdminZonal/Dashboard"})
    public String Dashboard(){

        return "AdminZonal/Dashboard/dashboard";
    }
    @GetMapping({"AdminZonal/Agentes"})
    public String Agentes(){

        return "AdminZonal/GestionAgentes/agentes";
    }
    @GetMapping({ "AdminZonal/Agentes/Crear"})
    public String CrearAgente(Model model){
        Optional<Usuario> optUsuario = usuarioRepository.findById(6);
        if(optUsuario.isPresent()){
            Usuario adminzonal = optUsuario.get();
            model.addAttribute("adminzonal",adminzonal);

            // Obtener la lista de distritos asociados a la zona del Admin Zonal
            List<Distrito> distritos = distritoRepository.findByZona_Id(adminzonal.getZona().getId());
            model.addAttribute("distritos", distritos);
            return "AdminZonal/GestionAgentes/crearAgente";
        }else{
            return "AdminZonal/GestionAgentes/crearAgente";
        }
    }

    @PostMapping("/AdminZonal/Agentes/Guardar")
    public String guardarAgente(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellidoPaterno") String apellidoPaterno,
            @RequestParam("apellidoMaterno") String apellidoMaterno,
            @RequestParam("dni") String dni,
            @RequestParam("telefono") String telefono,
            @RequestParam("email") String email,
            @RequestParam("direccion") String direccion,
            @RequestParam("distritoId") Integer distritoId,
            @RequestParam("codigoJurisdiccion") String codigoJurisdiccion,
            @RequestParam(value = "razonSocial", required = false) String razonSocial,
            @RequestParam("codigoAduana") String codigoAduana,
            @RequestParam("ruc") String ruc,
            RedirectAttributes attr) {

        // Verifica que llega al controlador
        System.out.println("Llega al método guardarAgente");

            // Crear la nueva solicitud de agente
            Solicitudagente solicitudAgente = new Solicitudagente();
            solicitudAgente.setCodigoAduana(codigoAduana);
            solicitudAgente.setCodigoJurisdiccion(codigoJurisdiccion);
            solicitudAgente.setIndicadorSolicitud(1); // Por defecto se guarda como "Por coordinador"
            solicitudAgente.setValidaciones(1); // Asignar un valor por defecto de validaciones
            solicitudAgenteRepository.save(solicitudAgente); // Guardamos la solicitud

            // Log para ver si la solicitud se ha guardado correctamente
            System.out.println("Solicitud guardada con ID: " + solicitudAgente.getId());

            // Obtener el distrito seleccionado
            Optional<Distrito> distritoOpt = distritoRepository.findById(distritoId);
            if (distritoOpt.isPresent()) {
                // Obtener el rol de "agente" con ID 4
                Optional<Rol> rolAgenteOpt = rolRepository.findById(4);
                if (rolAgenteOpt.isPresent()) {
                    // Obtener la zona del Admin Zonal (que ya fue cargado en el método `CrearAgente`)
                    Optional<Usuario> adminZonalOpt = usuarioRepository.findById(6); // Asegúrate de que este ID sea correcto
                    if (adminZonalOpt.isPresent()) {
                        Zona zona = adminZonalOpt.get().getZona(); // Obtener la zona del Admin Zonal

                        // Crear un nuevo usuario con los datos recibidos del formulario
                        Usuario nuevoAgente = new Usuario();
                        nuevoAgente.setNombre(nombre);
                        nuevoAgente.setApellidoPaterno(apellidoPaterno);
                        nuevoAgente.setApellidoMaterno(apellidoMaterno);
                        nuevoAgente.setDni(dni);
                        nuevoAgente.setTelefono(telefono);
                        nuevoAgente.setEmail(email);
                        nuevoAgente.setDireccion(direccion);
                        nuevoAgente.setDistrito(distritoOpt.get());
                        nuevoAgente.setAgtCodigoaduana(codigoAduana);
                        nuevoAgente.setAgtRuc(ruc);
                        nuevoAgente.setAgtRazonsocial(razonSocial != null ? razonSocial : "");
                        nuevoAgente.setAgtCodigojurisdiccion(codigoJurisdiccion);
                        nuevoAgente.setIdSolicitudAgente(solicitudAgente); // Asociar la solicitud al usuario
                        nuevoAgente.setBaneado(false); // El usuario no está baneado inicialmente
                        nuevoAgente.setRol(rolAgenteOpt.get()); // Asignar el rol de agente (ID 4)
                        nuevoAgente.setZona(zona); // Asignar la zona del Admin Zonal
                        nuevoAgente.setContrasena("hola");
                        // Guardamos el nuevo usuario (agente)
                        Usuario savedAgente = usuarioRepository.save(nuevoAgente);

                        // Log para ver si el usuario se ha guardado correctamente
                        System.out.println("Usuario (Agente) guardado con ID: " + savedAgente.getId());

                        attr.addFlashAttribute("msg", "El nuevo agente fue creado exitosamente.");
                    } else {
                        attr.addFlashAttribute("error", "No se pudo encontrar el Admin Zonal.");
                    }
                } else {
                    attr.addFlashAttribute("error", "No se pudo encontrar el rol de agente.");
                }
            } else {
                attr.addFlashAttribute("error", "No se pudo encontrar el distrito seleccionado.");
            }

        return "redirect:/AdminZonal/Agentes";
    }




    @GetMapping({ "AdminZonal/Productos"})
    public String Productos(){

        return "AdminZonal/GestionProductos/productos";
    }
    @GetMapping({ "AdminZonal/Perfil"})
    public String Perfil(){

        return "AdminZonal/Perfil/perfilAdminZonal";
    }


}

