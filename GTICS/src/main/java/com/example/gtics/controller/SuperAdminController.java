package com.example.gtics.controller;

import com.example.gtics.entity.Distrito;
import com.example.gtics.entity.Rol;
import com.example.gtics.entity.Usuario;
import com.example.gtics.entity.Zona;
import com.example.gtics.repository.*;
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
    
    private final RolRepository rolRepository;

    private final ProveedorRepository proveedorRepository;

    public SuperAdminController(UsuarioRepository usuarioRepository, ZonaRepository zonaRepository,
                                RolRepository rolRepository,
                                DistritoRepository distritoRepository, ProveedorRepository proveedorRepository,
                                ProductoRepository productoRepository,
                                OrdenRepository ordenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.zonaRepository = zonaRepository;
        this.rolRepository = rolRepository;
        this.distritoRepository = distritoRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.ordenRepository = ordenRepository;
    }


    private final DistritoRepository distritoRepository;
    private final ProductoRepository productoRepository;
    private final OrdenRepository ordenRepository;

    @GetMapping({"SuperAdmin/dashboard","SuperAdmin"})
    public String dashboard(Model model){
       //Cantidad de ordenes por mes
        model.addAttribute("OrdenesPormes", ordenRepository.getOrdenesMes());
        // Cantidad de ordenes por estado de seguimiento
        model.addAttribute("OrdenesPorEstado", ordenRepository.getOrdenesEstado());
        // Productos mas importantes (10), entity producto
        model.addAttribute("ProductosMasImportados", productoRepository.findProductosRelevantes());
        model.addAttribute("TotalVentas", productoRepository.getCantidadProductos());
        // Proveedores mas solicitados, entity: usuario
        model.addAttribute("ProveedoresMasSolicitados", proveedorRepository.findProveedoresMasSolicitados());
        // Proveedores con peores comentarios, entity: usuario
        model.addAttribute("PeoresProveedores", proveedorRepository.findProveedoresPorCalidadASC());
        // Cantidad de agentes
        model.addAttribute("CantidadAgentes", usuarioRepository.getCantidadAgentes());
        // Cantidad de usuarios inactivos vs activos
        model.addAttribute("CantidadUsuariosInactivos",usuarioRepository.getCantidadInactivos());
        model.addAttribute("CantidadUsuariosActivos", usuarioRepository.getCantidadActivos());
        // Cantidad de usuarios baneados
        model.addAttribute("CantidadUsuariosBaneados", usuarioRepository.getCantidadBaneados());
        // Cantidad de proveedores baneados
        model.addAttribute("CantidadProveedoresBaneados", proveedorRepository.countProveedoresBaneados());
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
                return "redirect:/SuperAdmin/listaAdminZonal";
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

        model.addAttribute("distritos", distritoRepository.findAll());
        model.addAttribute("usuario", usuario);
        model.addAttribute("zonas", zonaRepository.findAll());
        return "SuperAdmin/GestionAdminZonal/create-zonal-admin";
    }
    @PostMapping("/SuperAdmin/AdminZonal/guardar")
    public String guardarAdminZonal(@ModelAttribute("usuario") Usuario usuario,@RequestParam("zonaId") Integer zonaId, RedirectAttributes attr) {

        try {
            // Verificar si ya existen 2 coordinadores en la zona
            int cantidadCoordinadores = usuarioRepository.countCoordinadoresByZona(zonaId);
            if (cantidadCoordinadores >= 2) {
                attr.addFlashAttribute("error", "Ya existen 2 coordinadores en esta zona.");
                return "redirect:/SuperAdmin/crearAdminZonal";
            }
            Optional<Rol> optionalAZRol = rolRepository.findById(2);
            if (optionalAZRol.isPresent()) {
                usuario.setRol(optionalAZRol.get());
            } else {
                throw new RuntimeException("Rol Admin Zonal no encontrado");
            }
            Optional<Zona> optionalZona = zonaRepository.findById(zonaId);
            if (optionalZona.isPresent()) {
                usuario.setZona(optionalZona.get());  // Asignar la zona al usuario
            } else {
                throw new RuntimeException("Zona no encontrada");
            }
            Optional<Distrito> optionalNoRegistradoDistrito = distritoRepository.findById(5);
            if (optionalNoRegistradoDistrito.isPresent()) {
                Distrito noRegistradoDistrito = optionalNoRegistradoDistrito.get();

                // Actualiza el distrito "No Registrado" con la zona seleccionada
                noRegistradoDistrito.setZona(optionalZona.get());
                usuario.setDistrito(noRegistradoDistrito);
            } else {
                throw new RuntimeException("Distrito 'No Registrado' no encontrado");
            }
            usuario.setBaneado(false);
            if (usuario.getId() == null) {
                attr.addFlashAttribute("msg", "Admin Zonal creado exitosamente");
            } else {
                attr.addFlashAttribute("msg", "Información del admin zonal actualizada exitosamente");
            }
            usuarioRepository.save(usuario);
        } catch (Exception e) {
            attr.addFlashAttribute("error", "Ocurrió un error al guardar el Admin Zonal.");
            e.printStackTrace();
        }
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
            if (optionalAgente.isPresent() && optionalAgente.get().getRol().getId() == 3){
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
    public String listaSolicitudesAgentes(Model model){

        List<Usuario> listaUsuariosSolicitudes = usuarioRepository.mostrarSolicitudesAgente();
        model.addAttribute("listaUsuariosSolicitudes",listaUsuariosSolicitudes);

        return "SuperAdmin/GestionAgentes/agent-request";
    }

    @GetMapping("/SuperAdmin/rechazarSolicitudAgente")
    public String RechazarSolicitudAgente(@RequestParam Integer id,
                                          RedirectAttributes redirectAttributes) {

        Optional<Usuario> optUsuario = usuarioRepository.findById(id);

        if (optUsuario.isPresent()) {

            Usuario usuario = optUsuario.get();

            usuario.setIdSolicitudAgente(null);
            usuarioRepository.save(usuario);

            redirectAttributes.addFlashAttribute("successMessage", "El usuario ha sido rechazado éxitosamente.");

            return "redirect:/SuperAdmin/listaSolicitudesAgentes";

        }else {
            return "redirect:/SuperAdmin/listaSolicitudesAgentes";
        }

    }

    @GetMapping("SuperAdmin/verUsuario/{id}")
    public String verUsuario(@PathVariable("id") Integer id, Model model){
        try {
            Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);
            if (optionalUsuario.isPresent() && optionalUsuario.get().getRol().getId() == 4){
                model.addAttribute("usuario",optionalUsuario.get());
                String nombre = optionalUsuario.get().getNombre();
                model.addAttribute("nombre",nombre);

            } else {
                model.addAttribute("error","Agente no encontrado o el rol no es válido");
                return "SuperAdmin/GestionAgentes/agent-request";  // Redirigir a la lista si no se encuentra el agente
            }
        }catch (Exception e) {
            model.addAttribute("error", "Error al cargar el usuario");
            e.printStackTrace();
        }
        return "SuperAdmin/GestionAgentes/agent-ver-usuario";
    }






    @GetMapping("SuperAdmin/cambiarRolaAgente")
    public String cambiarRolaAgente(Model model,@RequestParam("id") Integer id){

        usuarioRepository.actualizarRolAAgente(id);

        return "redirect:/SuperAdmin/listaSolicitudesAgentes";
    }

    @GetMapping("SuperAdmin/listaUsuarioFinal")
    public String listaGestionUsuarioFinal(Model model){
        List<Usuario> finalUsersList = usuarioRepository.findByIdRol_Id(4);
        model.addAttribute("finalUsersList", finalUsersList);
        return "SuperAdmin/GestionUsuarioFinal/final-users-list";
    }

    @GetMapping("SuperAdmin/crearUsuarioFinal")
    public String crearUsuarioFinal(){

        return "SuperAdmin/GestionUsuarioFinal/create-final-user";
    }
    @GetMapping("SuperAdmin/editarUsuarioFinal/{id}")
    public String editarUsuarioFinal(Model model, @PathVariable("id") Integer idUsuarioFinal){
        Optional<Usuario> finalUser = usuarioRepository.findById(idUsuarioFinal);
        List<Distrito> listaDistritos = distritoRepository.findAll();
        model.addAttribute("listaDistritos", listaDistritos);
        model.addAttribute("finalUser", finalUser);
        return "SuperAdmin/GestionUsuarioFinal/final-user-edit";
    }

    @GetMapping("SuperAdmin/banearUsuarioFinal/{id}")
    public String banearUsuarioFinal(@PathVariable("id") Integer idUsuarioFinal, Model model) {
        usuarioRepository.banUsuario(idUsuarioFinal);
        return "redirect:/SuperAdmin/listaUsuarioFinal";
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
