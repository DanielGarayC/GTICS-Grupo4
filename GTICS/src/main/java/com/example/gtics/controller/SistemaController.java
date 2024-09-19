package com.example.gtics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SistemaController {
    @GetMapping({"Login"})
    public String Login(){

        return "Sistema/login";
    }
    @GetMapping({"Registro"})
    public String Registro(){

        return "Sistema/register";
    }
    @GetMapping({"RecuperarContra"})
    public String RecuperarContrasena(){

        return "Sistema/recvPass";
    }
    @GetMapping({"RecuperarContra/Token"})
    public String TokenValidacion(){

        return "Sistema/validarToken";
    }
    @GetMapping({"RecuperarContra/CambiarContra"})
    public String CambiarContrasena(){

        return "Sistema/chPass";
    }
}
