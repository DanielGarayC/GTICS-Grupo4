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

        return "UsuarioFinal/Productos/productos";
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
