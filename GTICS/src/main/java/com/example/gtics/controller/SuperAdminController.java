package com.example.gtics.controller;

import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class SuperAdminController {

    private final UsuarioRepository usuarioRepository;
    private final ZonaRepository zonaRepository;
    private final RolRepository rolRepository;
    private final ProveedorRepository proveedorRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final ProductoZonaRepository productoZonaRepository;
    private final TiendaRepository tiendaRepository;

    public SuperAdminController(UsuarioRepository usuarioRepository, ZonaRepository zonaRepository,
                                RolRepository rolRepository,
                                DistritoRepository distritoRepository, ProveedorRepository proveedorRepository,
                                ProductoRepository productoRepository,
                                OrdenRepository ordenRepository,
                                CategoriaRepository categoriaRepository,
                                SubcategoriaRepository subcategoriaRepository,
                                ProductoZonaRepository productoZonaRepository,
                                TiendaRepository tiendaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.zonaRepository = zonaRepository;
        this.rolRepository = rolRepository;
        this.distritoRepository = distritoRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.ordenRepository = ordenRepository;
        this.categoriaRepository = categoriaRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.productoZonaRepository = productoZonaRepository;
        this.tiendaRepository=tiendaRepository;
    }


    private final DistritoRepository distritoRepository;
    private final ProductoRepository productoRepository;
    private final OrdenRepository ordenRepository;

    @GetMapping({"SuperAdmin/dashboard","SuperAdmin"})
    public String dashboard(Model model){
       //Cantidad de ordenes por mes
        model.addAttribute("ordenesPorMes", ordenRepository.getOrdenesMes());
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
        model.addAttribute("CantidadUsuariosRegistrados",usuarioRepository.getCantidadRegistrados());
        model.addAttribute("CantidadUsuariosActivos", usuarioRepository.getCantidadActivos());
        // Cantidad de usuarios baneados
        model.addAttribute("CantidadUsuariosBaneados", usuarioRepository.getCantidadBaneados());
        // Cantidad de proveedores baneados
        model.addAttribute("CantidadProveedoresBaneados", proveedorRepository.countProveedoresBaneados());
        return "SuperAdmin/Dashboard/dashboard-superadmin";
    }
    @GetMapping("SuperAdmin/listaAdminZonal")
    public String listaGestionAdminZonal(Model model, @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(value = "busqueda", required = false) String busqueda){
        try {
            int pageSize = 3;
            Pageable pageable = PageRequest.of(page, pageSize);

            Page<Usuario> finalUsersList = usuarioRepository.findByRol_Id(2, pageable);
            if (finalUsersList.isEmpty()){
                model.addAttribute("msg", "No hay coordinadores zonales registrados");
            }else {

                model.addAttribute("usuarios", finalUsersList.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", finalUsersList.getTotalPages());
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
    public String listaGestionAgente(Model model) {

        try {
            List<Object[]> agentesData = usuarioRepository.mostrarAgentesConEstadosYRazonSocial();

            if (agentesData.isEmpty()) {
                model.addAttribute("message", "No hay agentes registrados");
            } else {
                List<Map<String, String>> agentes = new ArrayList<>();

                for (Object[] row : agentesData) {
                    Map<String, String> agente = new HashMap<>();
                    agente.put("idusuario", row[0].toString());
                    agente.put("nombre", row[1].toString());
                    agente.put("apellidopaterno", row[2].toString());
                    agente.put("apellidomaterno", row[3].toString());
                    agente.put("dni", row[4].toString());
                    agente.put("telefono", row[5].toString());
                    agente.put("agt_codigoaduana", row[6] != null ? row[6].toString() : "");  // Maneja nulos para código aduana
                    agente.put("estadoCodigoAduana", row[7] != null ? row[7].toString() : "");  // Maneja nulos para estado de código aduana
                    agente.put("agt_codigojurisdiccion", row[8] != null ? row[8].toString() : "");  // Maneja nulos para código jurisdicción
                    agente.put("estadoCodigoJurisdiccion", row[9] != null ? row[9].toString() : "");  // Maneja nulos para estado de código jurisdicción
                    agente.put("agt_razonsocial", row[10] != null ? row[10].toString() : "");  // Maneja nulos para razón social

                    agente.put("nombrezona", row[11].toString());

                    agentes.add(agente);
                }

                model.addAttribute("agentes", agentes);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la lista de agentes.");
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
        model.addAttribute("zonas", zonaRepository.findAll());
        return "SuperAdmin/GestionAgentes/agent-edit";
    }

    @PostMapping("/SuperAdmin/Agente/guardar")
    public String guardarAgente(@ModelAttribute("agente") Usuario agente, Model model) {
        try {
            // Buscar el agente existente
            Usuario agenteExistente = usuarioRepository.findById(agente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Agente no encontrado"));

            // Actualizar solo los campos modificados
            if (!agente.getNombre().equals(agenteExistente.getNombre())) {
                agenteExistente.setNombre(agente.getNombre());
            }
            if (!agente.getApellidoPaterno().equals(agenteExistente.getApellidoPaterno())) {
                agenteExistente.setApellidoPaterno(agente.getApellidoPaterno());
            }
            if (!agente.getApellidoMaterno().equals(agenteExistente.getApellidoMaterno())) {
                agenteExistente.setApellidoMaterno(agente.getApellidoMaterno());
            }
            if (!agente.getDni().equals(agenteExistente.getDni())) {
                agenteExistente.setDni(agente.getDni());
            }
            if (!agente.getTelefono().equals(agenteExistente.getTelefono())) {
                agenteExistente.setTelefono(agente.getTelefono());
            }
            if (!agente.getEmail().equals(agenteExistente.getEmail())) {
                agenteExistente.setEmail(agente.getEmail());
            }
            if (!agente.getAgtRazonsocial().equals(agenteExistente.getAgtRazonsocial())) {
                agenteExistente.setAgtRazonsocial(agente.getAgtRazonsocial());
            }
            if (!agente.getAgtCodigoaduana().equals(agenteExistente.getAgtCodigoaduana())) {
                agenteExistente.setAgtCodigoaduana(agente.getAgtCodigoaduana());
            }
            if (!agente.getAgtCodigojurisdiccion().equals(agenteExistente.getAgtCodigojurisdiccion())) {
                agenteExistente.setAgtCodigojurisdiccion(agente.getAgtCodigojurisdiccion());
            }

            // Si no tiene zona o ha cambiado, actualizarla
            if (agente.getZona() != null && !agente.getZona().equals(agenteExistente.getZona())) {
                agenteExistente.setZona(agente.getZona());
            }

            // Guardar solo si algo ha cambiado
            usuarioRepository.save(agenteExistente);

        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el agente.");
            e.printStackTrace();
            return "SuperAdmin/GestionAgentes/agent-edit";
        }

        return "redirect:/SuperAdmin/listaAgente";
    }


    @GetMapping("/SuperAdmin/eliminarAgente")
    public String eliminarAgente(@RequestParam("id") int id, RedirectAttributes attr) {
        Optional<Usuario> optProduct = usuarioRepository.findById(id);

        if (optProduct.isPresent()) {
            try {
                usuarioRepository.deleteById(id);
                attr.addFlashAttribute("msg", "El agente ha sido eliminado exitosamente");
            } catch (Exception e) {
                e.printStackTrace();
                attr.addFlashAttribute("error", "El agente no se pudo borrar correctamente =(.");
            }
        } else {
            attr.addFlashAttribute("error", "El Agente no fue encontrado.");
        }
        return "redirect:/SuperAdmin/listaAgente";
    }



    @GetMapping("SuperAdmin/listaSolicitudesAgentes")
    public String listaSolicitudesAgentes(Model model) {
        // Realiza la consulta que devuelve los datos con los estados aleatorios
        List<Object[]> listaUsuariosSolicitudes = usuarioRepository.mostrarSolicitudesConEstadosAleatorios();

        // Añadir la lista de solicitudes al modelo
        model.addAttribute("listaUsuariosSolicitudes", listaUsuariosSolicitudes);

        // Redireccionar a la vista correspondiente
        return "SuperAdmin/GestionAgentes/agent-request";
    }

    @PostMapping("SuperAdmin/listaSolicitudesAgentesFiltro")
    public String listaSolicitudesAgentesFiltro(Model model,@RequestParam("indicador") Integer indicador) {
        System.out.println(indicador);
        if(indicador==2) {
            return "redirect:/SuperAdmin/listaSolicitudesAgentes";
        }

        List<Object[]> listaUsuariosSolicitudes = usuarioRepository.mostrarSolicitudesConEstadosAleatoriosFiltro(indicador);

        // Añadir la lista de solicitudes al modelo
        model.addAttribute("listaUsuariosSolicitudes", listaUsuariosSolicitudes);

        // Redireccionar a la vista correspondiente
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
    public String listaGestionUsuarioFinal( @RequestParam(defaultValue = "0") int page,
                                            Model model){

        int pageSize = 3;
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Usuario> finalUsersList = usuarioRepository.findByRol_Id(4, pageable);

        model.addAttribute("finalUsersList", finalUsersList.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", finalUsersList.getTotalPages());

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

    @GetMapping("SuperAdmin/verUsuarioFinal/{id}")
    public String verUsuarioFinal(Model model, @PathVariable("id") Integer idUsuarioFinal){
        Optional<Usuario> finalUser = usuarioRepository.findById(idUsuarioFinal);
        model.addAttribute("finalUser", finalUser);
        return "SuperAdmin/GestionUsuarioFinal/final-user-info";
    }

    @PostMapping("SuperAdmin/Actualizar/{id}")
    public String actualizarUsuarioFinal(Model model, Usuario usuario, @PathVariable("id") Integer idUsuarioFinal){

        usuarioRepository.actualizarUsuarioFinal(usuario.getDni(), usuario.getNombre(), usuario.getApellidoPaterno(), usuario.getApellidoMaterno(), usuario.getEmail(), usuario.getDireccion(), usuario.getTelefono(), usuario.getDistrito().getId(), idUsuarioFinal);
        return "redirect:/SuperAdmin/listaUsuarioFinal";
    }

    @GetMapping("SuperAdmin/banearUsuarioFinal/{id}")
    public String banearUsuarioFinal(@PathVariable("id") Integer idUsuarioFinal, Model model) {
        usuarioRepository.banUsuario(idUsuarioFinal);
        return "redirect:/SuperAdmin/listaUsuarioFinal";
    }

    @GetMapping("SuperAdmin/agregarProducto")
    public String agregarProducto(Model model, @RequestParam(value = "idCategoria", required = false) Integer idCategoria) {
        Producto producto = new Producto();
        List<Categoria> categorias = categoriaRepository.findAll();
        List<Proveedor> proveedores = proveedorRepository.findAll();
        List<Zona> zonas = zonaRepository.findAll();
        List<Subcategoria> subcategorias;

        if (idCategoria != null) {
            subcategorias = subcategoriaRepository.findByCategoria_Id(idCategoria);
        } else {
            subcategorias = List.of();
        }

        // Inicializar la lista de productoZonas para evitar que sea null
        Map<Integer, ProductoZona> productoZonas = new HashMap<>();

        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categorias);
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("zonas", zonas);
        model.addAttribute("subcategorias", subcategorias);
        model.addAttribute("productoZonas", productoZonas);

        return "SuperAdmin/add-product";
    }


    @PostMapping("/SuperAdmin/guardarProducto")
    public String guardarProducto(@ModelAttribute("producto") Producto producto,
                                  @RequestParam("zona-1") Integer cantidadZona1,
                                  @RequestParam("zona-2") Integer cantidadZona2,
                                  @RequestParam("zona-3") Integer cantidadZona3,
                                  @RequestParam("zona-4") Integer cantidadZona4,
                                  RedirectAttributes attr) {

        try {
            if (producto.getCantVentas() == null) {
                producto.setCantVentas("0");
            }
            if (producto.getBorrado() == null) {
                producto.setBorrado(0);
            }
            productoRepository.save(producto);
            guardarProductoZona(producto, 1, cantidadZona1);
            guardarProductoZona(producto, 2, cantidadZona2);
            guardarProductoZona(producto, 3, cantidadZona3);
            guardarProductoZona(producto, 4, cantidadZona4);

            attr.addFlashAttribute("msg", "Producto creado exitosamente.");
        } catch (Exception e) {
            attr.addFlashAttribute("error", "Ocurrió un error al crear el producto.");
            e.printStackTrace();
        }
        return "redirect:/SuperAdmin/productos";
    }

    private void guardarProductoZona(Producto producto, Integer zonaId, Integer cantidad) {
        Optional<ProductoZona> optionalProductoZona = productoZonaRepository.findByProductoIdAndZonaId(producto.getId(), zonaId);

        ProductoZona productoZona;
        if (optionalProductoZona.isPresent()) {
            productoZona = optionalProductoZona.get();
        } else {
            productoZona = new ProductoZona();
            productoZona.setProducto(producto);
            Optional<Zona> zonaOpt = zonaRepository.findById(zonaId);
            zonaOpt.ifPresent(productoZona::setZona);
        }
        productoZona.setCantidad(cantidad);
        productoZonaRepository.save(productoZona);
    }

    @GetMapping("/SuperAdmin/editarProducto/{id}")
    public String editarProducto(@PathVariable("id") Integer id, Model model) {
        Optional<Producto> optionalProducto = productoRepository.findById(id);

        if (optionalProducto.isPresent()) {
            Producto producto = optionalProducto.get();
            List<Categoria> categorias = categoriaRepository.findAll();
            List<Proveedor> proveedores = proveedorRepository.findAll();
            List<Zona> zonas = zonaRepository.findAll();
            List<Subcategoria> subcategorias = subcategoriaRepository.findAll();

            // Cargar las cantidades por zona en un mapa
            List<ProductoZona> productoZonas = productoZonaRepository.findByProductoId(id);
            Map<Integer, ProductoZona> zonasMap = new HashMap<>();
            for (ProductoZona productoZona : productoZonas) {
                zonasMap.put(productoZona.getZona().getId(), productoZona);
            }

            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categorias);
            model.addAttribute("proveedores", proveedores);
            model.addAttribute("zonas", zonas);
            model.addAttribute("subcategorias", subcategorias);
            model.addAttribute("productoZonas", zonasMap);

            return "SuperAdmin/edit-product";
        } else {
            return "redirect:/SuperAdmin/productos";
        }
    }


    @GetMapping("/SuperAdmin/eliminarProducto/{id}")
    public String eliminarProducto(@PathVariable("id") Integer id, RedirectAttributes attr) {
        Optional<Producto> optionalProducto = productoRepository.findById(id);

        if (optionalProducto.isPresent()) {
            Producto producto = optionalProducto.get();
            producto.setBorrado(1);
            productoRepository.save(producto);
            attr.addFlashAttribute("msg", "El producto ha sido eliminado.");
        } else {
            attr.addFlashAttribute("error", "Producto no encontrado.");
        }

        return "redirect:/SuperAdmin/productos";
    }

    @GetMapping("SuperAdmin/productos")
    public String productos(Model model) {
        List<Producto> listaProductos = productoRepository.findAllActive();
        model.addAttribute("productos", listaProductos);
        return "SuperAdmin/productos";
    }
    //
    @GetMapping("SuperAdmin/proveedores")
    public String proveedores(Model model,
                              @RequestParam(defaultValue = "0") int page){
        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Tienda> tiendas = tiendaRepository.findAll(pageable);

        model.addAttribute("tiendas", tiendas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tiendas.getTotalPages());
        return "SuperAdmin/GestionProveedores/vendor-grid";
    }

    @GetMapping("/SuperAdmin/listaProveedores")
    public String listaProveedores(Model model,
                                   @RequestParam(defaultValue = "0") int page){
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Proveedor> proveedores = proveedorRepository.findAll(pageable);

        model.addAttribute("proveedores", proveedores.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", proveedores.getTotalPages());
        return "SuperAdmin/GestionProveedores/vendor-list";
    }

    @GetMapping("/SuperAdmin/borrar")
    public String borrar(@RequestParam("id") int id, RedirectAttributes attr){
        System.out.println(id);

        Optional<Proveedor> optProveedor = proveedorRepository.findById(id);

        if (optProveedor.isPresent()) {
            try {
                proveedorRepository.deleteById(id);
                attr.addFlashAttribute("msg", "El proveedor ha sido eliminado exitosamente.");
            } catch (Exception e) {
                e.printStackTrace();
                attr.addFlashAttribute("error", "El proveedor no se pudo borrar correctamente.");
            }
        } else {
            attr.addFlashAttribute("error", "Proveedor no encontrado.");
        }

        return "redirect:/SuperAdmin/listaProveedores";
    }

    @GetMapping("/Proveedor/eliminar")
    public String eliminarProveedor(@RequestParam("id") int id, RedirectAttributes attr) {

        Optional<Proveedor> optProduct = proveedorRepository.findById(id);

        if (optProduct.isPresent()) {
            try {
                proveedorRepository.deleteById(id);
                attr.addFlashAttribute("msg", "El Proveedor ha sido eliminado exitosamente");
            } catch (Exception e) {
                e.printStackTrace();
                attr.addFlashAttribute("error", "El Proveedor no se pudo borrar correctamente =(.");
            }
        }
        return "redirect:/SuperAdmin/listaProveedores";

    }

    @GetMapping("SuperAdmin/editarProveedor/{id}")
    public String editarProveedor(@PathVariable("id") Integer id, Model model){
        try {
            Optional<Proveedor> optionalAZ = proveedorRepository.findById(id);
            if (optionalAZ.isPresent()){
                List<Tienda> tiendas=tiendaRepository.findAll();
                System.out.println(tiendas.size());
                model.addAttribute("tiendas", tiendas);
                model.addAttribute("proveedor",optionalAZ.get());
            } else {
                model.addAttribute("error","Proveedor no encontrado");
                return "redirect:/SuperAdmin/listaProveedores";
            }
        }catch (Exception e) {
            model.addAttribute("error", "Error al cargar proveedor para editar.");
            e.printStackTrace();
        }

        return "SuperAdmin/GestionProveedores/vendor-edit";
    }

    @GetMapping("/SuperAdmin/agregarTienda")
    public String agregarTienda(){

        return "SuperAdmin/GestionProveedores/add-store";
    }

    @PostMapping("/SuperAdmin/Proveedor/guardar")
    public String guardarProveedor(
            @ModelAttribute("proveedor") Proveedor proveedor,
            @RequestParam("tienda") Integer tiendaId,
            RedirectAttributes attr) {

        try {
            // Buscar la tienda por el id recibido en el formulario
            Optional<Tienda> optionalTienda = tiendaRepository.findById(tiendaId);

            // Verificar si la tienda existe
            if (optionalTienda.isPresent()) {
                // Asignar la tienda al proveedor
                proveedor.setTienda(optionalTienda.get());
            } else {
                throw new RuntimeException("Tienda no encontrada");
            }

            // Guardar el proveedor en el repositorio
            proveedorRepository.save(proveedor);

            if (proveedor.getId() == null) {
                attr.addFlashAttribute("msg", "Proveedor creado exitosamente");
            } else {
                attr.addFlashAttribute("msg", "Información del proveedor actualizada exitosamente");
            }
        } catch (Exception e) {
            attr.addFlashAttribute("error", "Ocurrió un error al guardar el proveedor.");
            e.printStackTrace();
        }

        // Redirigir a la lista de proveedores después de guardar
        return "redirect:/SuperAdmin/listaProveedores";
    }

    @GetMapping("/proveedores/fototienda/{id}")
    public ResponseEntity<ByteArrayResource> obtenerFotoTienda(@PathVariable Integer id) {
        Tienda tienda = tiendaRepository.findById(id).orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        byte[] foto = tienda.getFotoTienda();
        ByteArrayResource resource = new ByteArrayResource(foto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"foto_tienda_" + id + ".jpg\"")
                .contentLength(foto.length)
                .body(resource);
    }


    @GetMapping("SuperAdmin/perfil")
    public String anadirCategoria(){

        return "SuperAdmin/perfilSuperAdmin";
    }
}
