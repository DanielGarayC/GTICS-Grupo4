package com.example.gtics.controller;

import com.example.gtics.entity.Orden;
import com.example.gtics.entity.Usuario;
import com.example.gtics.repository.DistritoRepository;
import com.example.gtics.repository.OrdenRepository;
import com.example.gtics.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller

public class AgenteController {

    final UsuarioRepository usuarioRepository;
    final DistritoRepository distritoRepository;
    final OrdenRepository ordenRepository;

    // Constructor que recibe los tres repositorios
    public AgenteController(UsuarioRepository usuarioRepository,
                            DistritoRepository distritoRepository,
                            OrdenRepository ordenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.distritoRepository = distritoRepository;
        this.ordenRepository = ordenRepository;
    }



    @GetMapping({"Agente", "AdminZonal/Inicio"})
    public String Inicio(){

        return "Agente/inicio";
    }
    @GetMapping({"Agente/Chat"})
    public String ChatAgente(){

        return "Agente/chatVistaAgente";
    }

    @GetMapping({"Agente/UsuariosAsignados"})
    public String UsuariosAsignados(Model model){

        // Supongamos que ya tienes el objeto "agente" cargado o puedes cargarlo por su ID
        Usuario agente = usuarioRepository.findById(1).orElseThrow(() -> new RuntimeException("Agente no encontrado"));

        // Filtrar usuarios con idRol=4, agente asignado y no baneados
        List<Usuario> usuariosAsignados = usuarioRepository.findUsuariosFiltrados(4, agente);

        model.addAttribute("listaUsuariosAsignados", usuariosAsignados);

        return "Agente/UsuariosAsignados/usuariosAsignados";
    }


    // Método para banear a un usuario usando RequestParam
    @GetMapping("/Agente/banear")
    public String banearUsuario(@RequestParam Integer id,
                                RedirectAttributes redirectAttributes) {

        Optional<Usuario> optUsuario = usuarioRepository.findById(id);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();

            usuario.setBaneado((byte) 1);
            usuarioRepository.save(usuario);

            redirectAttributes.addFlashAttribute("successMessage", "El usuario ha sido baneado éxitosamente.");

            return "redirect:/Agente/UsuariosAsignados";

        }else {
            return "redirect:/Agente/UsuariosAsignados";
        }

    }



    @GetMapping({"Agente/Ordenes"})
    public String Ordenes(){

        return "Agente/OrdenesDeUsuario/ordeneslista";
    }
/*
    @GetMapping("/Agente/Ordenes/Usuario/Lista")
    public String verOrdenesPorUsuario(@RequestParam("id") Integer idUsuario,
                                       Model model) {

        Optional<Usuario> optUsuario = usuarioRepository.findById(idUsuario);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();
            List<Orden> ordenes = ordenRepository.findByUsuarioId(idUsuario);
            model.addAttribute("usuario", usuario);
            model.addAttribute("ordenes", ordenes);
            return "Agente/OrdenesDeUsuario/ordenesDeUsuarioLista";

        }else {
            return "redirect:/Agente/UsuariosAsignados";
        }

    }
*/


    @GetMapping({"Agente/Ordenes/Usuario"})
    public String OrdenesUsuario(){

        return "Agente/OrdenesDeUsuario/ordenesDeUsuario";
    }
    @GetMapping({"Agente/Ordenes/Detalles"})
    public String DetalleOrden(){

        return "Agente/OrdenesDeUsuario/detalleOrden";
    }
    @GetMapping({"Agente/Ordenes/Descargar"})
    public String DescargarOrden(){

        return "Agente/OrdenesDeUsuario/DescargarOrdenesPorUsuario";
    }


    @GetMapping({"Agente/Reportes/Descargar"})
    public String DescargarReporte(){

        return "Agente/Reportes/descargarReporte";
    }
}

