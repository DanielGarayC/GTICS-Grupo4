package com.example.gtics.controller;

import com.example.gtics.entity.Usuario;
import com.example.gtics.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.util.Optional;

@Controller
public class UsuarioFinalController {
    private final UsuarioRepository usuarioRepository;

    public UsuarioFinalController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping({"/UsuarioFinal", "/UsuarioFinal/pagPrincipal"})
    public String mostrarPagPrincipal(Model model){

        Optional<Usuario> optUsuario = usuarioRepository.findById(7);
        if(optUsuario.isPresent()){
            Usuario us = optUsuario.get(); // usuario random que solicita ser agente
            model.addAttribute("usuario",us);
            return "UsuarioFinal/PaginaPrincipal/pagina_principalUF";
        }else{
            return "UsuarioFinal/PaginaPrincipal/pagina_principalUF";
        }

    }
    @GetMapping("/UsuarioFinal/miPerfil")
    public String miPerfil(){

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
