package com.example.gtics.controller;

import com.example.gtics.entity.Usuario;
import com.example.gtics.entity.Zona;
import com.example.gtics.repository.UsuarioRepository;
import com.example.gtics.repository.ZonaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class SuperAdminController {

    private final UsuarioRepository usuarioRepository;
    private final ZonaRepository zonaRepository;
    public SuperAdminController(UsuarioRepository usuarioRepository, ZonaRepository zonaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.zonaRepository = zonaRepository;
    }

    @GetMapping({"SuperAdmin/dashboard","SuperAdmin"})
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
    public String crearAdminZonal(Model model){
        model.addAttribute("zonas", zonaRepository.findAll());
        return "SuperAdmin/GestionAdminZonal/create-zonal-admin";
    }
    @PostMapping("/AdminZonal/guardar")
    public String registrarAdminZonal(Usuario usuario, RedirectAttributes attr) {

        if (usuario.getId() == 0) {
            attr.addFlashAttribute("msg", "Admin Zonal creado exitosamente");
        } else {
            attr.addFlashAttribute("msg", "Información del admin aonal actualizada exitosamente");
        }
        usuarioRepository.save(usuario);
        return "redirect:/SuperAdmin/listaAdminZonal";
    }
    @GetMapping("/AdminZonal/eliminar")
    public String eliminarAdminZonal(@RequestParam("id") int id, RedirectAttributes attr) {

        Optional<Usuario> optProduct = usuarioRepository.findById(id);

        if (optProduct.isPresent()) {
            try {
                usuarioRepository.deleteById(id);
                attr.addFlashAttribute("msg", "El Admin Zonal ha sido eliminado exitosamente");
            } catch (Exception e) {
                e.printStackTrace();
                attr.addFlashAttribute("error", "El Admin Zonal no se pudo borrar correctamente =(.");
            }
        }
        return "redirect:/SuperAdmin/listaAdminZonal";

    }
    @GetMapping("SuperAdmin/listaAgente")
    public String listaGestionAgente(){

        return "SuperAdmin/GestionAgentes/agent-list";
    }
    @GetMapping("SuperAdmin/editarAgente")
    public String editarAgente(){

        return "SuperAdmin/GestionAgentes/agent-edit";
    }
    @GetMapping("SuperAdmin/listaSolicitudesAgentes")
    public String listaSolicitudesAgentes(){

        return "SuperAdmin/GestionAgentes/agent-request";
    }

    public String crearAgente(){

        return "SuperAdmin/GestionAgentes/create-agent";
    }

    @GetMapping("SuperAdmin/listaUsuarioFinal")
    public String listaGestionUsuarioFinal(){

        return "SuperAdmin/GestionUsuarioFinal/final-users-list";
    }

    @GetMapping("SuperAdmin/crearUsuarioFinal")
    public String crearUsuarioFinal(){

        return "SuperAdmin/GestionUsuarioFinal/create-final-user";
    }
    @GetMapping("SuperAdmin/editarUsuarioFinal")
    public String editarUsuarioFinal(){

        return "SuperAdmin/GestionUsuarioFinal/final-user-edit";
    }

    @GetMapping("SuperAdmin/agregarCategoria")
    public String agregarCategoria(){

        return "SuperAdmin/add-category";
    }

    @GetMapping("SuperAdmin/categorias")
    public String categorias(){

        return "SuperAdmin/categories";
    }
    @GetMapping("SuperAdmin/proveedores")
    public String proveedores(){

        return "SuperAdmin/vendor-grid";
    }

    @GetMapping("SuperAdmin/listaProveedores")
    public String listaProveedores(){

        return "SuperAdmin/vendor-list";
    }

    @GetMapping("SuperAdmin/perfil")
    public String añadirCategoria(){

        return "SuperAdmin/perfilSuperAdmin";
    }
}
