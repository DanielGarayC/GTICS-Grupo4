package com.example.gtics.controller;

import com.example.gtics.entity.Rol;
import com.example.gtics.entity.Usuario;
import com.example.gtics.entity.Zona;
import com.example.gtics.repository.UsuarioRepository;
import com.example.gtics.repository.ZonaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class SuperAdminController {

    private final UsuarioRepository usuarioRepository;
    private final ZonaRepository zonaRepository;
    public SuperAdminController(UsuarioRepository usuarioRepository, ZonaRepository zonaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.zonaRepository = zonaRepository;
    }
    Rol rolAZ = new Rol();
    @GetMapping({"SuperAdmin/dashboard","SuperAdmin"})
    public String dashboard(){

        return "SuperAdmin/Dashboard/dashboard-superadmin";
    }
    @GetMapping("SuperAdmin/listaAdminZonal")
    public String listaGestionAdminZonal(Model model, @RequestParam(value = "busqueda", required = false) String busqueda){
        try {
            List<Usuario> listaAZ = usuarioRepository.findByIdRol_Id(2);
            if (listaAZ.isEmpty()){
                model.addAttribute("msg", "No hay coordinadores zonales registrados");
            }else {

                model.addAttribute("usuarios", listaAZ);
            }
        }catch (Exception e){
            model.addAttribute("error","Error al cargar la lista de coordinadores zonales.");
            e.printStackTrace();
        }

        return "SuperAdmin/GestionAdminZonal/admin-zonal-list";
    }
    @GetMapping("SuperAdmin/editarAdminZonal/{id}")
    public String editarAdminZonal(@PathVariable("id") Integer id, Model model){
        try {
            Optional<Usuario> optionalAZ = usuarioRepository.findById(id);
            if (optionalAZ.isPresent() && optionalAZ.get().getRol().getId() == 2){
                model.addAttribute("usuario",optionalAZ.get());
            } else {
                model.addAttribute("error","Admin Zonal no encontrado o el rol no es válido");
                return "SuperAdmin/listaAdminZonal";
            }
        }catch (Exception e) {
            model.addAttribute("error", "Error al cargar el admin zonal para editar.");
            e.printStackTrace();
        }
        model.addAttribute("zonas", zonaRepository.findAll());
        return "SuperAdmin/GestionAdminZonal/admin-zonal-edit";
    }
    @GetMapping("SuperAdmin/crearAdminZonal")
    public String crearAdminZonal(Model model){
        Usuario usuario = new Usuario();
        rolAZ.setId(2);
        usuario.setRol(rolAZ);
        model.addAttribute("usuario", usuario);
        model.addAttribute("zonas", zonaRepository.findAll());
        return "SuperAdmin/GestionAdminZonal/create-zonal-admin";
    }
    @PostMapping("/AdminZonal/guardar")
    public String guardarAdminZonal(Usuario usuario, RedirectAttributes attr) {

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
    public String listaGestionAgente(Model model ){

        try {
            List<Usuario> agentes = usuarioRepository.findByIdRol_Id(3);
            if (agentes.isEmpty()){
                model.addAttribute("message", "No hay agentes Registrados");
            }else {
                model.addAttribute("agentes", agentes);

            }
        }catch (Exception e){
            model.addAttribute("error","Error al cargar la lista de agentes.");
            e.printStackTrace();
        }


        return "SuperAdmin/GestionAgentes/agent-list";
    }
    @GetMapping("SuperAdmin/editarAgente/{id}")
    public String editarAgente(@PathVariable("id") Integer id, Model model){
        try {
            Optional<Usuario> optionalAgente = usuarioRepository.findById(id);
            if (optionalAgente.isPresent() && optionalAgente.get().getRol().getId() == 2){
                model.addAttribute("agente",optionalAgente.get());
            } else {
                model.addAttribute("error","Agente no encontrado o el rol no es válido");
                return "SuperAdmin/GestionAgentes/agent-list";  // Redirigir a la lista si no se encuentra el agente
            }
        }catch (Exception e) {
            model.addAttribute("error", "Error al cargar el agente para editar.");
            e.printStackTrace();
        }
        return "SuperAdmin/GestionAgentes/agent-edit";
    }

    @PostMapping("/guardarAgente")
    public String guardarAgente(@ModelAttribute("agente") Usuario agente, Model model) {
        try {
            if (agente.getRol().getId() == 2) {  // Asegura que el rol del usuario sea de agente
                usuarioRepository.save(agente);
            } else {
                model.addAttribute("error", "El rol del usuario no es válido para un agente.");
                return "SuperAdmin/GestionAgentes/agent-edit";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el agente.");
            e.printStackTrace();
            return "SuperAdmin/GestionAgentes/agent-edit";
        }
        return "redirect:/SuperAdmin/listaAgente";
    }

    @PostMapping("/SuperAdmin/eliminarAgente/{id}")
    public String eliminarAgente(@PathVariable("id") Integer id, Model model) {
        try {
            Optional<Usuario> optionalAgente = usuarioRepository.findById(id);
            if (optionalAgente.isPresent() && optionalAgente.get().getRol().getId() == 2) {
                usuarioRepository.deleteById(id);
            } else {
                model.addAttribute("error", "Agente no encontrado o el rol no es válido.");
                return "redirect:/SuperAdmin/listaAgente?error";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar el agente.");
            e.printStackTrace();
            return "redirect:/SuperAdmin/listaAgente?error";
        }
        return "redirect:/SuperAdmin/listaAgente";
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
