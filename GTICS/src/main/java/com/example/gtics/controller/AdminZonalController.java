package com.example.gtics.controller;

import com.example.gtics.dto.Agente;
import com.example.gtics.dto.ProductoTabla;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


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
    public String CrearAgente(Model model){
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

    @PostMapping("/AdminZonal/Agentes/Guardar")
    public String guardarAgente(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellidoPaterno") String apellidoPaterno,
            @RequestParam("apellidoMaterno") String apellidoMaterno,
            @RequestParam("dni") String dni,
            @RequestParam("telefono") String telefono,
            @RequestParam("email") String email,
            @RequestParam("direccion") String direccion,
            @RequestParam("distritoId") Integer distritoId,
            @RequestParam("codigoJurisdiccion") String codigoJurisdiccion,
            @RequestParam(value = "razonSocial", required = false) String razonSocial,
            @RequestParam("codigoAduana") String codigoAduana,
            @RequestParam("ruc") String ruc,
            RedirectAttributes attr) {

        // Verifica que llega al controlador
        //VALIDACIONES: codigo ruc 11 digitos | codigo aduanero tiene 3 digitos | codigo de jurisdiccion 4-6 digitos DANIEL VALIDA ESTO MRD
        int idAdminZonal = 11;
        System.out.println("Llega al método guardarAgente");
        try {
            // Crear la nueva solicitud de agente
            Solicitudagente solicitudAgente = new Solicitudagente();
            solicitudAgente.setCodigoAduana(codigoAduana);
            solicitudAgente.setCodigoJurisdiccion(codigoJurisdiccion);
            solicitudAgente.setCodigoRuc(ruc);
            solicitudAgente.setIndicadorSolicitud(1); // Por defecto se guarda como "Por coordinador"
            solicitudAgenteRepository.save(solicitudAgente); // Guardamos la solicitud

            // Log para ver si la solicitud se ha guardado correctamente
            System.out.println("Solicitud guardada con ID: " + solicitudAgente.getId());

            // Obtener el distrito seleccionado
            Optional<Distrito> distritoOpt = distritoRepository.findById(distritoId);
            if (distritoOpt.isPresent()) {
                // Obtener el rol de "agente" con ID 4
                Optional<Rol> rolAgenteOpt = rolRepository.findById(3);
                if (rolAgenteOpt.isPresent()) {
                    // Obtener la zona del Admin Zonal (que ya fue cargado en el método `CrearAgente`)
                    Optional<Usuario> adminZonalOpt = usuarioRepository.findById(idAdminZonal); // Asegúrate de que este ID sea correcto
                    if (adminZonalOpt.isPresent()) {
                        Zona zona = adminZonalOpt.get().getZona(); // Obtener la zona del Admin Zonal

                        // Crear un nuevo usuario con los datos recibidos del formulario
                        Usuario nuevoAgente = new Usuario();
                        nuevoAgente.setNombre(nombre);
                        nuevoAgente.setApellidoPaterno(apellidoPaterno);
                        nuevoAgente.setApellidoMaterno(apellidoMaterno);
                        nuevoAgente.setDni(dni);
                        nuevoAgente.setTelefono(telefono);
                        nuevoAgente.setEmail(email);
                        nuevoAgente.setDireccion(direccion);
                        nuevoAgente.setDistrito(distritoOpt.get());
                        nuevoAgente.setAgtCodigoaduana(codigoAduana);
                        nuevoAgente.setAgtRuc(ruc);
                        nuevoAgente.setAgtRazonsocial(razonSocial != null ? razonSocial : "");
                        nuevoAgente.setAgtCodigojurisdiccion(codigoJurisdiccion);
                        nuevoAgente.setIdSolicitudAgente(solicitudAgente); // Asociar la solicitud al usuario
                        nuevoAgente.setBaneado(false); // El usuario no está baneado inicialmente
                        nuevoAgente.setRol(rolAgenteOpt.get()); // Asignar el rol de agente (ID 4)
                        nuevoAgente.setZona(zona); // Asignar la zona del Admin Zonal
                        nuevoAgente.setUCantimportaciones(String.valueOf(0));
                        nuevoAgente.setIdAdminZonal(adminZonalOpt.get());
                        nuevoAgente.setBaneado(false);
                        nuevoAgente.setActivo(0);
                        nuevoAgente.setContrasena("hola"); //CAMBIAR: Generar contraseña aleatoriamente
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
    public String Productos(Model model){
        model.addAttribute("listaProductos", productoRepository.getProductosTabla());
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
    public String Perfil(){

        return "AdminZonal/Perfil/perfilAdminZonal";
    }


}

