package com.example.gtics.controller;

import com.example.gtics.dto.Agente;
import com.example.gtics.dto.ProductoTabla;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;

import jakarta.validation.Valid;
import org.springframework.boot.Banner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;


@Controller

public class AdminZonalController {
    private final UsuarioRepository usuarioRepository;
    private final SolicitudAgenteRepository solicitudAgenteRepository;
    private final DistritoRepository distritoRepository;
    private final RolRepository rolRepository;
    private final ProductoRepository productoRepository;
    private final SolicitudreposicionRepository solicitudreposicionRepository;

    public  AdminZonalController(SolicitudAgenteRepository solicitudAgenteRepository, UsuarioRepository usuarioRepository,DistritoRepository distritoRepository,RolRepository rolRepository,
                                 ProductoRepository productoRepository,
                                 SolicitudreposicionRepository solicitudreposicionRepository){
        this.solicitudAgenteRepository = solicitudAgenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.distritoRepository = distritoRepository;
        this.rolRepository = rolRepository;
        this.productoRepository = productoRepository;
        this.solicitudreposicionRepository = solicitudreposicionRepository;
    }
    @GetMapping({"AdminZonal", "AdminZonal/Dashboard"})
    public String Dashboard(Model model){
        int idAdminZonal = 11;
        Usuario AdmZonal = usuarioRepository.findUsuarioById(idAdminZonal);
        int idZona =AdmZonal.getZona().getId();
        List<Agente> listaAgentes = usuarioRepository.findAgentesByAdminZonal(idAdminZonal);
        ArrayList<String> labelsAgenteChart = new ArrayList();
        ArrayList<Integer> usuariosPorAgente = new ArrayList();
        for(Agente agente : listaAgentes){
            String nombre = agente.getNombre()+ ' ' + agente.getApellidoPaterno();
            labelsAgenteChart.add(nombre);
            usuariosPorAgente.add(agente.getCantidadUsuarios());
        }
        //Primera parte
        model.addAttribute("ProductosMenorStock", productoRepository.prductosPocoStockZona(idZona));
        //G1
        model.addAttribute("CantidadUsuariosRegistrados",usuarioRepository.getCantidadRegistradosZona(idZona));
        model.addAttribute("CantidadUsuariosActivos", usuarioRepository.getCantidadActivosZona(idZona));
        //G2
        model.addAttribute("labelsAgentes", labelsAgenteChart);
        model.addAttribute("usuariosPorAgente", usuariosPorAgente);
        //LI (arreglar la db csm)
        model.addAttribute("ProductosMasImportados", productoRepository.findProductosRelevantesZona(idZona));
        model.addAttribute("TotalVentas", productoRepository.getCantidadProductosZona(idZona));

        //LD
        model.addAttribute("topImportadores", usuarioRepository.getTopImportadores());
        return "AdminZonal/Dashboard/dashboard";
    }
    @GetMapping({"AdminZonal/Agentes"})
    public String Agentes(Model model){
        int idAdminZonal = 11;
        boolean crearAgente = true;
        int cantAgentes = usuarioRepository.cantAgentesByAZ(idAdminZonal);
        System.out.println(cantAgentes);
        if(cantAgentes>2){
            crearAgente = false;
        }
        model.addAttribute("crearAgente", crearAgente);
        model.addAttribute("listaAgentes", usuarioRepository.findAgentesByAdminZonal(idAdminZonal));
        return "AdminZonal/GestionAgentes/agentes";
    }
    @GetMapping({ "AdminZonal/Agentes/Crear"})
    public String CrearAgente(@ModelAttribute("agente") Usuario agente, Model model){
        int idAdminZonal = 11;
        Optional<Usuario> optUsuario = usuarioRepository.findById(idAdminZonal);
        if(optUsuario.isPresent()){
            Usuario adminzonal = optUsuario.get();
            model.addAttribute("adminzonal",adminzonal);

            // Obtener la lista de distritos asociados a la zona del Admin Zonal
            List<Distrito> distritos = distritoRepository.findByZona_Id(adminzonal.getZona().getId());
            //Funcion que valida si la cantidad de agentes asociados al coordinador es menor a 3
            model.addAttribute("distritos", distritos);
            return "AdminZonal/GestionAgentes/crearAgente";
        }else{
            return "AdminZonal/GestionAgentes/crearAgente";
        }
    }

    public String generarContrasenaAleatoria(int longitud) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[longitud];
        random.nextBytes(bytes);
        String contrasena = Base64.getEncoder().encodeToString(bytes);

        // Opcional: Puedes modificar para quitar caracteres que no quieras
        return contrasena.substring(0, longitud); // Limitar la longitud a la deseada
    }

    @PostMapping("/AdminZonal/Agentes/Guardar")
    public String guardarAgente(@ModelAttribute("agente") @Valid Usuario agente, BindingResult bindingResult, Model model, RedirectAttributes attr) {

        //VALIDACIONES: codigo ruc 11 digitos | codigo aduanero tiene 3 digitos | codigo de jurisdiccion 4-6 digitos
        int idAdminZonal = 11;
        System.out.println("Llega al método guardarAgente");
        if(bindingResult.hasErrors()){
            Optional<Usuario> optUsuario = usuarioRepository.findById(idAdminZonal);
            if(optUsuario.isPresent()){
                Usuario adminzonal = optUsuario.get();
                model.addAttribute("adminzonal",adminzonal);

                // Obtener la lista de distritos asociados a la zona del Admin Zonal
                List<Distrito> distritos = distritoRepository.findByZona_Id(adminzonal.getZona().getId());
                //Funcion que valida si la cantidad de agentes asociados al coordinador es menor a 3
                model.addAttribute("distritos", distritos);
                return "AdminZonal/GestionAgentes/crearAgente";
            }
        }else {
            try {
                // Crear la nueva solicitud de agente
                Solicitudagente solicitudAgente = new Solicitudagente();
                solicitudAgente.setCodigoAduana(agente.getAgtCodigoaduana());
                solicitudAgente.setCodigoJurisdiccion(agente.getAgtCodigojurisdiccion());
                solicitudAgente.setCodigoRuc(agente.getAgtRuc());
                solicitudAgente.setIndicadorSolicitud(1); // Por defecto se guarda como "Por coordinador"
                solicitudAgenteRepository.save(solicitudAgente); // Guardamos la solicitud

                // Log para ver si la solicitud se ha guardado correctamente
                System.out.println("Solicitud guardada con ID: " + solicitudAgente.getId());

                // Obtener el distrito seleccionado
                Optional<Distrito> distritoOpt = distritoRepository.findById(agente.getDistrito().getId());
                if (distritoOpt.isPresent()) {
                    // Obtener el rol de "agente" con ID 3
                    Optional<Rol> rolAgenteOpt = rolRepository.findById(3);
                    if (rolAgenteOpt.isPresent()) {
                        // Obtener la zona del Admin Zonal (que ya fue cargado en el método `CrearAgente`)
                        Optional<Usuario> adminZonalOpt = usuarioRepository.findById(idAdminZonal); // Asegúrate de que este ID sea correcto
                        if (adminZonalOpt.isPresent()) {
                            Zona zona = adminZonalOpt.get().getZona(); // Obtener la zona del Admin Zonal

                            // Crear un nuevo usuario con los datos recibidos del formulario
                            Usuario nuevoAgente = new Usuario();
                            nuevoAgente.setNombre(agente.getNombre());
                            nuevoAgente.setApellidoPaterno(agente.getApellidoPaterno());
                            nuevoAgente.setApellidoMaterno(agente.getApellidoMaterno());
                            nuevoAgente.setDni(agente.getDni());
                            nuevoAgente.setTelefono(agente.getTelefono());
                            nuevoAgente.setEmail(agente.getEmail());
                            nuevoAgente.setDireccion(agente.getDireccion());
                            nuevoAgente.setDistrito(distritoOpt.get());
                            nuevoAgente.setAgtCodigoaduana(agente.getAgtCodigoaduana());
                            nuevoAgente.setAgtRuc(agente.getAgtRuc());
                            nuevoAgente.setAgtRazonsocial(agente.getAgtRazonsocial() != null ? agente.getAgtRazonsocial() : "");
                            nuevoAgente.setAgtCodigojurisdiccion(agente.getAgtCodigojurisdiccion());
                            nuevoAgente.setIdSolicitudAgente(solicitudAgente); // Asociar la solicitud al usuario
                            nuevoAgente.setBaneado(false); // El usuario no está baneado inicialmente
                            nuevoAgente.setRol(rolAgenteOpt.get()); // Asignar el rol de agente (ID 3)
                            nuevoAgente.setZona(zona); // Asignar la zona del Admin Zonal
                            nuevoAgente.setUCantimportaciones(String.valueOf(0));
                            nuevoAgente.setIdAdminZonal(adminZonalOpt.get());
                            nuevoAgente.setActivo(0);

                            // Generar una contraseña aleatoria y asignarla al agente
                            String contrasenaAleatoria = generarContrasenaAleatoria(10); // Genera una contraseña de 10 caracteres
                            nuevoAgente.setContrasena(contrasenaAleatoria);

                            // Guardamos el nuevo usuario (agente)
                            Usuario savedAgente = usuarioRepository.save(nuevoAgente);

                            // Log para ver si el usuario se ha guardado correctamente
                            System.out.println("Usuario (Agente) guardado con ID: " + savedAgente.getId());

                            attr.addFlashAttribute("msg", "El nuevo agente fue creado exitosamente.");
                        } else {
                            attr.addFlashAttribute("error", "No se pudo encontrar el Admin Zonal.");
                        }
                    } else {
                        attr.addFlashAttribute("error", "No se pudo encontrar el rol de agente.");
                    }
                } else {
                    attr.addFlashAttribute("error", "No se pudo encontrar el distrito seleccionado.");
                }
            } catch (Exception e) {
                attr.addFlashAttribute("error", "Ocurrió un error al crear el agente.");
                e.printStackTrace();
            }
        }
        return "redirect:/AdminZonal/Agentes";
    }
    @GetMapping("/AdminZonal/Agentes/fotoagente/{id}")
    public ResponseEntity<ByteArrayResource> obtenerFotoAgente(@PathVariable Integer id) {
        Usuario agente = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Foto no encontrada"));
        byte[] foto = agente.getFoto();
        ByteArrayResource resource = new ByteArrayResource(foto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"foto_usuario_" + id + ".jpg\"")
                .contentLength(foto.length)
                .body(resource);
    }



    @GetMapping({ "AdminZonal/Productos"})
    public String Productos(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "4") int size, Model model){
        List<ProductoTabla> todosLosProductos = productoRepository.getProductosTabla();

        // Calcular el total de productos
        int totalProductos = todosLosProductos.size();

        // Calcular el número total de páginas
        int totalPages = (int) Math.ceil((double) totalProductos / size);

        // Calcular el índice inicial y final para la sublista
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalProductos);

        // Obtener la sublista de productos para la página actual
        List<ProductoTabla> productosPaginados = todosLosProductos.subList(fromIndex, toIndex);

        // Añadir la lista paginada de productos al modelo
        model.addAttribute("listaProductos", productosPaginados);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        return "AdminZonal/GestionProductos/productos";
    }

    @PostMapping({"AdminZonal/Productos/guardarFecha"})
    public String GuardarFecha(@RequestParam("productoId") Integer productoId,
                               @RequestParam("fechaEntrega") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaEntrega){
        Optional<Producto> productoOPT = productoRepository.findById(productoId);
        System.out.println("ID: " + productoId);
        if(productoOPT.isPresent()){
            Producto producto = productoOPT.get();
            producto.setFechaArribo(fechaEntrega);
            productoRepository.save(producto);
        }

        return "redirect:/AdminZonal/Productos";
    }

    @PostMapping({"AdminZonal/Productos/GuardarSolReposicion"})
    public String GuardarSolReposicion(@RequestParam("productoId") Integer productoId,
                                       @RequestParam("cantidad") Integer cantSolicitada){
        int idAdminZonal = 11;
        Optional<Usuario> az = usuarioRepository.findById(idAdminZonal);
        Optional<Producto> productoOPT = productoRepository.findById(productoId);
        System.out.println("ID: " + productoId);
        if(productoOPT.isPresent() && az.isPresent()){
            Producto producto = productoOPT.get();

            Optional<Solicitudreposicion> solicitudExistente = solicitudreposicionRepository.findByIdProducto(producto);

            Solicitudreposicion newSol;
            if(solicitudExistente.isPresent()){
                newSol = solicitudExistente.get();
                newSol.setCantidadSolicitada(String.valueOf(cantSolicitada));

            }else{
                newSol = new Solicitudreposicion();
                newSol.setIdProducto(producto);
                newSol.setCantidadSolicitada(String.valueOf(cantSolicitada));
                newSol.setIdUsuario(az.get());
            }
            solicitudreposicionRepository.save(newSol);

        }

        return "redirect:/AdminZonal/Productos";
    }


    @GetMapping("/AdminZonal/Productos/fotoproducto/{id}")
    public ResponseEntity<ByteArrayResource> obtenerFotoProducto(@PathVariable Integer id) {

        byte[] foto = productoRepository.getProductosTablaId(id).getPrimeraFoto();
        ByteArrayResource resource = new ByteArrayResource(foto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"foto_producto_" + id + ".jpg\"")
                .contentLength(foto.length)
                .body(resource);
    }

    @PostMapping({"AdminZonal/Productos/EliminarSolReposicion"})
    public String EliminarSolicitudReposicion(@RequestParam("productoId") Integer productoId){
        int idAdminZonal = 11;
        Optional<Usuario> az = usuarioRepository.findById(idAdminZonal);
        Producto producto = productoRepository.findById(productoId).orElseThrow(() -> new RuntimeException("Foto no encontrada"));
        Optional<Solicitudreposicion> solRepoOPT = solicitudreposicionRepository.findByIdProducto(producto);
        if (solRepoOPT.isPresent()) {
            solicitudreposicionRepository.delete(solRepoOPT.get());
        }
        return "redirect:/AdminZonal/Productos";
    }
    @GetMapping({ "AdminZonal/Perfil"})
    public String Perfil(Model model){
        //Actualizar para el entregable de sesiones
        Optional<Usuario> OptAdminZonal =  usuarioRepository.findById(3);
        if(OptAdminZonal.isPresent()){
            model.addAttribute("adminZonal",OptAdminZonal.get());
        }
        return "AdminZonal/Perfil/perfilAdminZonal";
    }


}

