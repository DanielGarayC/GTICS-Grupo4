package com.example.gtics.controller;

import com.example.gtics.ValidationGroup.AdminZonalValidationGroup;
import com.example.gtics.ValidationGroup.AgenteValidationGroup;
import com.example.gtics.dto.Agente;
import com.example.gtics.dto.ProductoTabla;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.boot.Banner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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

    @ModelAttribute
    public void addUsuarioToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName(); // Obtener el email del usuario autenticado
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            optUsuario.ifPresent(usuario -> model.addAttribute("usuario", usuario));
        }
    }



    @GetMapping({"AdminZonal", "AdminZonal/Dashboard"})
    public String Dashboard(Model model, HttpSession session){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            if (optUsuario.isPresent()) {

                Usuario usuario = optUsuario.get();
                Integer idAdminZonal = usuario.getId();

                // Almacenar el idAdminZonal en la sesión
                session.setAttribute("idAdminZonal", idAdminZonal);

                Usuario AdmZonal = usuarioRepository.findUsuarioById(idAdminZonal);
                int idZona = AdmZonal.getZona().getId();
                List<Agente> listaAgentes = usuarioRepository.findAgentesByAdminZonal(idAdminZonal);
                ArrayList<String> labelsAgenteChart = new ArrayList();
                ArrayList<Integer> usuariosPorAgente = new ArrayList();
                for (Agente agente : listaAgentes) {
                    String nombre = agente.getNombre() + ' ' + agente.getApellidoPaterno();
                    labelsAgenteChart.add(nombre);
                    usuariosPorAgente.add(agente.getCantidadUsuarios());
                }
                //Primera parte
                model.addAttribute("ProductosMenorStock", productoRepository.prductosPocoStockZona(idZona));
                //G1
                model.addAttribute("CantidadUsuariosRegistrados", usuarioRepository.getCantidadRegistradosZona(idZona));
                model.addAttribute("CantidadUsuariosActivos", usuarioRepository.getCantidadActivosZona(idZona));
                //G2
                model.addAttribute("labelsAgentes", labelsAgenteChart);
                model.addAttribute("usuariosPorAgente", usuariosPorAgente);
                //LI (arreglar la db csm)
                model.addAttribute("ProductosMasImportados", productoRepository.findProductosRelevantesZona(idZona));
                model.addAttribute("TotalVentas", productoRepository.getCantidadProductosZona(idZona));

                //LD
                model.addAttribute("topImportadores", usuarioRepository.getTopImportadores());
            }

        }
        return "AdminZonal/Dashboard/dashboard";
    }
    @GetMapping({"AdminZonal/Agentes"})
    public String Agentes(Model model,
                          HttpSession session){

        Integer idAdminZonal = (Integer) session.getAttribute("idAdminZonal");

        boolean crearAgente = true;
        int cantAgentes = usuarioRepository.contarAgentesPorAdminZonal(idAdminZonal);
        System.out.println(cantAgentes);
        if(cantAgentes>2){
            crearAgente = false;
        }
        model.addAttribute("crearAgente", crearAgente);
        model.addAttribute("listaAgentes", usuarioRepository.findAgentesByAdminZonal(idAdminZonal));
        return "AdminZonal/GestionAgentes/agentes";
    }
    @GetMapping({ "AdminZonal/Agentes/Crear"})
    public String CrearAgente(@ModelAttribute("agente") Usuario agente, Model model,
                              HttpSession session){

        Integer idAdminZonal = (Integer) session.getAttribute("idAdminZonal");
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
    public String guardarAgente(@ModelAttribute("agente") @Validated(AgenteValidationGroup.class) Usuario agente, BindingResult bindingResult, Model model, RedirectAttributes attr,
                                HttpSession session){

        Integer idAdminZonal = (Integer) session.getAttribute("idAdminZonal");
        System.out.println("Llega al método guardarAgente");
        if(bindingResult.hasErrors()){
            if (bindingResult.hasFieldErrors("dni")) {
                if (bindingResult.getFieldError("dni").getCode().equals("NotBlank")) {
                    model.addAttribute("dniError", "Debe ingresar un número de DNI");
                } else if (bindingResult.getFieldError("dni").getCode().equals("Size")) {
                    model.addAttribute("dniError", "El DNI debe tener exactamente 8 dígitos");
                } else if (bindingResult.getFieldError("dni").getCode().equals("Pattern")) {
                    model.addAttribute("dniError", "El DNI debe contener solo dígitos");
                }
            }

            // Validación de Teléfono
            if (bindingResult.hasFieldErrors("telefono")) {
                if (bindingResult.getFieldError("telefono").getCode().equals("NotBlank")) {
                    model.addAttribute("telefonoError", "Debe ingresar un número de teléfono");
                } else if (bindingResult.getFieldError("telefono").getCode().equals("Size")) {
                    model.addAttribute("telefonoError", "El teléfono debe tener exactamente 9 dígitos");
                } else if (bindingResult.getFieldError("telefono").getCode().equals("Pattern")) {
                    model.addAttribute("telefonoError", "El teléfono debe contener solo dígitos");
                }
            }
            if (bindingResult.hasFieldErrors("agtCodigojurisdiccion")) {
                if (bindingResult.getFieldError("agtCodigojurisdiccion").getCode().equals("NotBlank")) {
                    model.addAttribute("agtCodigojurisdiccionError", "El código de jurisdicción no puede estar vacío");
                } else if (bindingResult.getFieldError("agtCodigojurisdiccion").getCode().equals("Size")) {
                    model.addAttribute("agtCodigojurisdiccionError", "El código de jurisdicción debe tener entre 4 y 6 dígitos");
                } else if (bindingResult.getFieldError("agtCodigojurisdiccion").getCode().equals("Pattern")) {
                    model.addAttribute("agtCodigojurisdiccionError", "El código de jurisdicción debe contener solo dígitos");
                }
            }
            // Validación de Código Aduanero
            if (bindingResult.hasFieldErrors("agtCodigoaduana")) {
                if (bindingResult.getFieldError("agtCodigoaduana").getCode().equals("NotBlank")) {
                    model.addAttribute("agtCodigoaduanaError", "El código aduanero no puede estar vacío");
                } else if (bindingResult.getFieldError("agtCodigoaduana").getCode().equals("Size")) {
                    model.addAttribute("agtCodigoaduanaError", "El código aduanero debe tener exactamente 6 dígitos");
                } else if (bindingResult.getFieldError("agtCodigoaduana").getCode().equals("Pattern")) {
                    model.addAttribute("agtCodigoaduanaError", "El código aduanero debe contener solo dígitos");
                }
            }

            if (bindingResult.hasFieldErrors("agtRuc")) {
                if (bindingResult.getFieldError("agtRuc").getCode().equals("NotBlank")) {
                    model.addAttribute("agtRucError", "El RUC no puede estar vacío");
                } else if (bindingResult.getFieldError("agtRuc").getCode().equals("Size")) {
                    model.addAttribute("agtRucError", "El RUC debe tener exactamente 10 dígitos");
                } else if (bindingResult.getFieldError("agtRuc").getCode().equals("Pattern")) {
                    model.addAttribute("agtRucError", "El RUC debe contener solo dígitos");
                }
            }
            Optional<Usuario> optUsuario = usuarioRepository.findById(idAdminZonal);
            if (bindingResult.hasFieldErrors("dni")) {
                if (bindingResult.getFieldError("dni").getCode().equals("NotBlank")) {
                    model.addAttribute("dniError", "Debe ingresar un número de DNI");
                } else if (bindingResult.getFieldError("dni").getCode().equals("Size")) {
                    model.addAttribute("dniError", "El DNI debe tener exactamente 8 dígitos");
                } else if (bindingResult.getFieldError("dni").getCode().equals("Pattern")) {
                    model.addAttribute("dniError", "El DNI debe contener solo dígitos");
                }
            }

            // Validación de Teléfono
            if (bindingResult.hasFieldErrors("telefono")) {
                if (bindingResult.getFieldError("telefono").getCode().equals("NotBlank")) {
                    model.addAttribute("telefonoError", "Debe ingresar un número de teléfono");
                } else if (bindingResult.getFieldError("telefono").getCode().equals("Size")) {
                    model.addAttribute("telefonoError", "El teléfono debe tener exactamente 9 dígitos");
                } else if (bindingResult.getFieldError("telefono").getCode().equals("Pattern")) {
                    model.addAttribute("telefonoError", "El teléfono debe contener solo dígitos");
                }
            }
            if (bindingResult.hasFieldErrors("agtCodigojurisdiccion")) {
                if (bindingResult.getFieldError("agtCodigojurisdiccion").getCode().equals("NotBlank")) {
                    model.addAttribute("agtCodigojurisdiccionError", "El código de jurisdicción no puede estar vacío");
                } else if (bindingResult.getFieldError("agtCodigojurisdiccion").getCode().equals("Size")) {
                    model.addAttribute("agtCodigojurisdiccionError", "El código de jurisdicción debe tener entre 4 y 6 dígitos");
                } else if (bindingResult.getFieldError("agtCodigojurisdiccion").getCode().equals("Pattern")) {
                    model.addAttribute("agtCodigojurisdiccionError", "El código de jurisdicción debe contener solo dígitos");
                }
            }
            // Validación de Código Aduanero
            if (bindingResult.hasFieldErrors("agtCodigoaduana")) {
                if (bindingResult.getFieldError("agtCodigoaduana").getCode().equals("NotBlank")) {
                    model.addAttribute("agtCodigoaduanaError", "El código aduanero no puede estar vacío");
                } else if (bindingResult.getFieldError("agtCodigoaduana").getCode().equals("Size")) {
                    model.addAttribute("agtCodigoaduanaError", "El código aduanero debe tener exactamente 6 dígitos");
                } else if (bindingResult.getFieldError("agtCodigoaduana").getCode().equals("Pattern")) {
                    model.addAttribute("agtCodigoaduanaError", "El código aduanero debe contener solo dígitos");
                }
            }

            if (bindingResult.hasFieldErrors("agtRuc")) {
                if (bindingResult.getFieldError("agtRuc").getCode().equals("NotBlank")) {
                    model.addAttribute("agtRucError", "El RUC no puede estar vacío");
                } else if (bindingResult.getFieldError("agtRuc").getCode().equals("Size")) {
                    model.addAttribute("agtRucError", "El RUC debe tener exactamente 10 dígitos");
                } else if (bindingResult.getFieldError("agtRuc").getCode().equals("Pattern")) {
                    model.addAttribute("agtRucError", "El RUC debe contener solo dígitos");
                }
            }
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
                int idUsuarioSol = 0;
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
                            idUsuarioSol = savedAgente.getId();
                            // Log para ver si el usuario se ha guardado correctamente
                            System.out.println("Usuario (Agente) guardado con ID: " + idUsuarioSol);

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


                // Crear la nueva solicitud de agente
                Solicitudagente solicitudAgente = new Solicitudagente();
                if (idUsuarioSol > 0) {
                    Usuario createdAgent = usuarioRepository.findUsuarioById(idUsuarioSol);
                    solicitudAgente.setIdUsuario(createdAgent);
                }
                solicitudAgente.setCodigoAduana(agente.getAgtCodigoaduana());
                solicitudAgente.setCodigoJurisdiccion(agente.getAgtCodigojurisdiccion());
                solicitudAgente.setCodigoRuc(agente.getAgtRuc());
                solicitudAgente.setIndicadorSolicitud(1); // Por defecto se guarda como "Por coordinador"
                solicitudAgenteRepository.save(solicitudAgente); // Guardamos la solicitud

                // Log para ver si la solicitud se ha guardado correctamente
                System.out.println("Solicitud guardada con ID: " + solicitudAgente.getId());


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



    @GetMapping("/AdminZonal/Productos")
    public String Productos(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "3") int size,
                            Model model) {
        // Obtener todos los productos (sin paginar)
        List<ProductoTabla> todosLosProductos = productoRepository.getProductosTabla();

        // Calcular el total de productos
        int totalProductos = todosLosProductos.size();

        // Calcular el número total de páginas
        int totalPages = (int) Math.ceil((double) totalProductos / size);

        // Calcular el índice inicial y final para la sublista (productos de la página actual)
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalProductos);

        // Obtener la sublista de productos para la página actual
        List<ProductoTabla> productosPaginados = todosLosProductos.subList(fromIndex, toIndex);

        // Añadir la lista paginada de productos al modelo
        model.addAttribute("listaProductos", productosPaginados);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);

        // Calcular el rango de páginas para la paginación
        int visiblePages = 3;  // Número de botones de página visibles
        int startPage = Math.max(0, page - (visiblePages / 2));
        int endPage = Math.min(totalPages - 1, page + (visiblePages / 2));

        // Ajustar el rango si el número de páginas visibles es menor que 3
        if (endPage - startPage + 1 < visiblePages) {
            if (startPage == 0) {
                endPage = Math.min(totalPages - 1, startPage + visiblePages - 1);
            } else if (endPage == totalPages - 1) {
                startPage = Math.max(0, endPage - visiblePages + 1);
            }
        }

        // Añadir los valores de inicio y fin de página al modelo
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "AdminZonal/GestionProductos/productos";
    }


    @PostMapping({"AdminZonal/Productos/guardarFecha"})
    public String GuardarFecha(@RequestParam("productoId") Integer productoId,
                               @RequestParam("fechaEntrega") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaEntrega, RedirectAttributes attr){
        Optional<Producto> productoOPT = productoRepository.findById(productoId);
        System.out.println("ID: " + productoId);
        if(productoOPT.isPresent()){
            Producto producto = productoOPT.get();
            producto.setFechaArribo(fechaEntrega);
            productoRepository.save(producto);
            attr.addFlashAttribute("msg", "La fecha de entrega se guardó correctamente.");
        }else {
            attr.addFlashAttribute("error", "No se encontró el producto para guardar la fecha de entrega.");
        }

        return "redirect:/AdminZonal/Productos";
    }

    @PostMapping({"AdminZonal/Productos/EliminarFechaArribo"})
    public String EliminarFechaArribo(@RequestParam("productoId") Integer productoId, RedirectAttributes attr) {
        Optional<Producto> productoOPT = productoRepository.findById(productoId);


        if (productoOPT.isPresent()) {
            Producto producto = productoOPT.get();
            if (producto.getFechaArribo() != null) {
                producto.setFechaArribo(null);
                productoRepository.save(producto);
                attr.addFlashAttribute("msg", "La fecha de arribo se eliminó correctamente.");
            } else {
                attr.addFlashAttribute("error", "No se encontró una fecha de arribo para eliminar.");
            }
        } else {
            attr.addFlashAttribute("error", "No se encontró el producto para eliminar la fecha de arribo.");
        }

        return "redirect:/AdminZonal/Productos";
    }

    @PostMapping({"AdminZonal/Productos/GuardarSolReposicion"})
    public String GuardarSolReposicion(@RequestParam("productoId") Integer productoId,
                                       @RequestParam("cantidad") Integer cantSolicitada, RedirectAttributes redirectAttributes){
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
                redirectAttributes.addFlashAttribute("msg", "La solicitud de reposición se editó correctamente.");

            }else{
                newSol = new Solicitudreposicion();
                newSol.setIdProducto(producto);
                newSol.setCantidadSolicitada(String.valueOf(cantSolicitada));
                newSol.setIdUsuario(az.get());
                redirectAttributes.addFlashAttribute("msg", "La solicitud de reposición se creó correctamente.");
            }
            solicitudreposicionRepository.save(newSol);

        }else {
            // En caso de error
            redirectAttributes.addFlashAttribute("error", "Error al procesar la solicitud.");
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
    public String EliminarSolicitudReposicion(@RequestParam("productoId") Integer productoId, RedirectAttributes attr){
        int idAdminZonal = 11;
        Optional<Usuario> az = usuarioRepository.findById(idAdminZonal);
        Producto producto = productoRepository.findById(productoId).orElseThrow(() -> new RuntimeException("Foto no encontrada"));
        Optional<Solicitudreposicion> solRepoOPT = solicitudreposicionRepository.findByIdProducto(producto);
        if (solRepoOPT.isPresent()) {
            solicitudreposicionRepository.delete(solRepoOPT.get());
            attr.addFlashAttribute("msg", "La solicitud de reposición se eliminó correctamente.");

        }else {
            attr.addFlashAttribute("error", "No se encontró la solicitud de reposición para eliminar.");
        }
        return "redirect:/AdminZonal/Productos";
    }
    @GetMapping({ "AdminZonal/Perfil"})
    public String Perfil(Model model, HttpSession session){
        //Actualizar para el entregable de sesiones
        Integer idAdminZonal = (Integer) session.getAttribute("idAdminZonal");

        Optional<Usuario> OptAdminZonal =  usuarioRepository.findById(idAdminZonal);
        if(OptAdminZonal.isPresent()){
            model.addAttribute("adminZonal",OptAdminZonal.get());
        }
        return "AdminZonal/Perfil/perfilAdminZonal";
    }


}

