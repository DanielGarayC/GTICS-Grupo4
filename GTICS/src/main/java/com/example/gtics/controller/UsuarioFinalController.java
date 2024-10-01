package com.example.gtics.controller;

import com.example.gtics.entity.Solicitudagente;
import com.example.gtics.entity.Usuario;
import com.example.gtics.entity.Validacionescodigosagente;
import com.example.gtics.repository.SolicitudAgenteRepository;
import com.example.gtics.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class UsuarioFinalController {
    private final UsuarioRepository usuarioRepository;
    private final SolicitudAgenteRepository solicitudAgenteRepository;

    public UsuarioFinalController(SolicitudAgenteRepository solicitudAgenteRepository, UsuarioRepository usuarioRepository) {
        this.solicitudAgenteRepository = solicitudAgenteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute
    public void addUsuarioToModel(Model model) {
        Optional<Usuario> optUsuario = usuarioRepository.findById(7);  // Aquí cambias el ID según el usuario que necesites
        // Usuario agregado globalmente
        optUsuario.ifPresent(usuario -> model.addAttribute("usuario", usuario));
    }

    @GetMapping({"/UsuarioFinal", "/UsuarioFinal/pagPrincipal"})
    public String mostrarPagPrincipal(Model model){
            return "UsuarioFinal/PaginaPrincipal/pagina_principalUF";
    }

    @PostMapping("/UsuarioFinal/solicitudAgente")
    public String enviarSolicitudaSerAgente(Solicitudagente solicitudagente){
        solicitudagente.setIndicadorSolicitud(0);
        //solicitudagente.setValidaciones(1);
        solicitudagente.setCodigoJurisdiccion("333");

        //solicitudagente.setValidaciones(new Validacionescodigosagente());
        /*
        System.out.println(solicitudagente.getCodigoAduana());
        System.out.println(solicitudagente.getCodigoJurisdiccion());
        System.out.println(solicitudagente.getIndicadorSolicitud());
        */
        solicitudAgenteRepository.save(solicitudagente);
        Optional<Usuario> optUsuario = usuarioRepository.findById(7);
        Solicitudagente ultimaSolicitud = solicitudAgenteRepository.findTopByOrderByIdDesc();

        if(optUsuario.isPresent()) {
            Usuario us = optUsuario.get(); // usuario random que solicita ser agente


            usuarioRepository.asignarSolictudAusuario(ultimaSolicitud.getId(), us.getId());
        }
        return "redirect:/UsuarioFinal";
    }


    @GetMapping("/UsuarioFinal/miPerfil")
    public String miPerfil(Model model){

        return "UsuarioFinal/Perfil/miperfil";
    }
    @GetMapping("/UsuarioFinal/listaMisOrdenes")
    public String mostrarListaMisOrdenes(){

        return "UsuarioFinal/Ordenes/listaMisOrdenes";
    }
    @GetMapping("/UsuarioFinal/detallesOrden")
    public String mostrarDetallesOrden(){

        return "UsuarioFinal/Ordenes/detalleOrden";
    }
    @GetMapping("/UsuarioFinal/listaProductos")
    public String mostrarListaProductos(){

        return "UsuarioFinal/Productos/listaProductos";
    }
    @GetMapping("/UsuarioFinal/detallesProducto")
    public String mostrarDetallesProducto(){

        return "UsuarioFinal/Productos/detalleProducto";
    }
    @GetMapping("/UsuarioFinal/categorias")
    public String mostrarCategorias(){

        return "UsuarioFinal/Productos/categoria";
    }
    @GetMapping("/UsuarioFinal/Review")
    public String mostrarReview(){

        return "UsuarioFinal/Productos/reviuw";
    }
    @GetMapping("/UsuarioFinal/procesoCompra")
    public String procesoComprar(){

        return "UsuarioFinal/ProcesoCompra/proceso_compra";
    }
    @GetMapping("/UsuarioFinal/chatbot")
    public String interactuarChatBot(){

        return "UsuarioFinal/ProcesoCompra/chatbot";
    }
    @GetMapping("/UsuarioFinal/chatSoporte")
    public String chatSoporte(){

        return "UsuarioFinal/ProcesoCompra/chatSoporte";
    }
    @GetMapping("/UsuarioFinal/foro")
    public String verForo(){

        return "UsuarioFinal/Foro/foro";
    }
    @GetMapping("/UsuarioFinal/faq")
    public String preguntasFrecuentes(){

        return "UsuarioFinal/Foro/preguntasFrecuentes";
    }
}
