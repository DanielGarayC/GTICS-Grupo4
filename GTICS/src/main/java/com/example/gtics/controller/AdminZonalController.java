package com.example.gtics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller

public class AdminZonalController {
    @GetMapping({"AdminZonal", "AdminZonal/Dashboard"})
    public String Dashboard(){

        return "AdminZonal/Dashboard/dashboard";
    }
    @GetMapping({"AdminZonal/Agentes"})
    public String Agentes(){

        return "AdminZonal/GestionAgentes/agentes";
    }
    @GetMapping({ "AdminZonal/CrearAgente"})
    public String CrearAgente(){

        return "AdminZonal/GestionAgentes/crearAgente";
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

