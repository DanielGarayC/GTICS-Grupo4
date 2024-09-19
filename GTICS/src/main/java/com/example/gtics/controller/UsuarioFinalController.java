package com.example.gtics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioFinalController {
    @GetMapping({"/UsuarioFinal", "/UsuarioFinal/pagPrincipal"})
    public String mostrarPagPrincipal(){

        return "UsuarioFinal/pagina_principalUF";
    }
    @GetMapping("/UsuarioFinal/miPerfil")
    public String miPerfil(){

        return "UsuarioFinal/perfil";
    }
    @GetMapping("/UsuarioFinal/listaMisOrdenes")
    public String mostrarListaMisOrdenes(){

        return "UsuarioFinal/listaMisOrdenes";
    }
    @GetMapping("/UsuarioFinal/listaProductos")
    public String mostrarListaProductos(){

        return "UsuarioFinal/productos";
    }
    @GetMapping("/UsuarioFinal/Review")
    public String mostrarReview(){

        return "UsuarioFinal/reviuw";
    }
    @GetMapping("/UsuarioFinal/Categorias")
    public String mostrarCategorias(){

        return "categoria";
    }
}
