package com.example.gtics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SuperAdminController {
    @GetMapping("SuperAdmin/dashboard")
    public String dashboard(){

        return "SuperAdmin/Dashboard/dashboard-superadmin";
    }
    @GetMapping("SuperAdmin/listaAdminZonal")
    public String listaGestionAdminZonal(){

        return "SuperAdmin/GestionAdminZonal/admin-zonal-list";
    }
    @GetMapping("SuperAdmin/editarAdminZonal")
    public String editarAdminZonal(){

        return "SuperAdmin/GestionAdminZonal/admin-zonal-edit";
    }
    @GetMapping("SuperAdmin/crearAdminZonal")
    public String crearAdminZonal(){

        return "SuperAdmin/GestionAdminZonal/create-zonal-admin";
    }
    @GetMapping("SuperAdmin/listaAgente")
    public String listaGestionAgente(){

        return "SuperAdmin/GestionAgentes/agent-list";
    }
    @GetMapping("SuperAdmin/editarAgente")
    public String editarAgente(){

        return "SuperAdmin/GestionAgentes/agent-edit";
    }
    @GetMapping("SuperAdmin/crearAgente")
    public String crearAgente(){

        return "SuperAdmin/GestionAgentes/create-agent";
    }
}
