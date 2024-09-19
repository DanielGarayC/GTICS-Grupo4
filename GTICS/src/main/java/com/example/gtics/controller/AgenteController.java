package com.example.gtics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class AgenteController {
    @GetMapping({"Agente", "AdminZonal/Inicio"})
    public String Inicio(){

        return "Agente/inicio";
    }
    @GetMapping({"Agente/Chat"})
    public String ChatAgente(){

        return "Agente/chatVistaAgente";
    }

    @GetMapping({"Agente/UsuariosAsignados"})
    public String UsuariosAsignados(){

        return "Agente/UsuariosAsignados/usuariosAsignados";
    }


    @GetMapping({"Agente/Ordenes"})
    public String Ordenes(){

        return "Agente/OrdenesDeUsuario/ordeneslista";
    }
    @GetMapping({"Agente/Ordenes/Usuario/Lista"})
    public String OrdenesUsuarioLista(){

        return "Agente/OrdenesDeUsuario/ordenesDeUsuarioLista";
    }
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

