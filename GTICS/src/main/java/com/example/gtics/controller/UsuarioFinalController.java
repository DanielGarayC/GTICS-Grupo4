package com.example.gtics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioFinalController {
    @GetMapping({"/UsuarioFinal", "/UsuarioFinal/pagPrincipal"})
    public String mostrarPagPrincipal(){

        return "UsuarioFinal/PaginaPrincipal/pagina_principalUF";
    }
    @GetMapping("/UsuarioFinal/miPerfil")
    public String miPerfil(){

        return "UsuarioFinal/Perfil/perfil";
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

        return "UsuarioFinal/productos";
    }
    @GetMapping("/UsuarioFinal/detallesProducto")
    public String mostrarDetallesProducto(){

        return "detalleProducto";
    }
    @GetMapping("/UsuarioFinal/Review")
    public String mostrarReview(){

        return "UsuarioFinal/reviuw";
    }
    @GetMapping("/UsuarioFinal/Categorias")
    public String mostrarCategorias(){

        return "categoria";
    }
    @GetMapping("/UsuarioFinal/chatbot")
    public String interactuarChatBot(){

        return "chatbot";
    }
    @GetMapping("/UsuarioFinal/foro")
    public String verForo(){

        return "foro";
    }
}
