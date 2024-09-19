package com.example.gtics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioFinalController {
    @GetMapping("/UsuarioFinal, /UsuarioFinal/pagPrincipal")
    public String mostrarPagPrincipal(){

        return "pagina_principalUF";
    }
    @GetMapping("/UsuarioFinal/MiPerfil")
    public String miPerfil(){

        return "perfil";
    }
    @GetMapping("/UsuarioFinal/listaMisOrdenes")
    public String mostrarListaMisOrdenes(){

        return "listaMisOrdenes";
    }
    @GetMapping("/UsuarioFinal/listaProductos")
    public String mostrarListaProductos(){

        return "productos";
    }
    @GetMapping("/UsuarioFinal/Review")
    public String mostrarReview(){

        return "reviuw";
    }
}
