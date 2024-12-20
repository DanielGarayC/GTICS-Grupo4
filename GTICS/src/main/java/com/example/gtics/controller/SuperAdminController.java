package com.example.gtics.controller;

import com.example.gtics.ValidationGroup.AgenteValidationGroup;
import com.example.gtics.ValidationGroup.InventarioProductosValidationGroup;
import com.example.gtics.ValidationGroup.UsuarioFinalValidationGroup;
import com.example.gtics.dto.solAgente;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import com.example.gtics.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;

@Controller
public class SuperAdminController {

    private final UsuarioRepository usuarioRepository;
    private final ZonaRepository zonaRepository;
    private final RolRepository rolRepository;
    private final ProveedorRepository proveedorRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final TiendaRepository tiendaRepository;
    private final FotosProductoRepository fotosProductoRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public SuperAdminController(UsuarioRepository usuarioRepository, ZonaRepository zonaRepository,
                                RolRepository rolRepository,
                                DistritoRepository distritoRepository, ProveedorRepository proveedorRepository,
                                ProductoRepository productoRepository,
                                OrdenRepository ordenRepository,
                                CategoriaRepository categoriaRepository,
                                SubcategoriaRepository subcategoriaRepository,
                                TiendaRepository tiendaRepository, FotosProductoRepository fotosProductoRepository,
                                SolicitudAgenteRepository solicitudAgenteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.zonaRepository = zonaRepository;
        this.rolRepository = rolRepository;
        this.distritoRepository = distritoRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.ordenRepository = ordenRepository;
        this.categoriaRepository = categoriaRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.tiendaRepository = tiendaRepository;
        this.fotosProductoRepository = fotosProductoRepository;
        this.solicitudAgenteRepository = solicitudAgenteRepository;

    }


    private final DistritoRepository distritoRepository;
    private final ProductoRepository productoRepository;
    private final OrdenRepository ordenRepository;

    private final SolicitudAgenteRepository solicitudAgenteRepository;


    @GetMapping({"SuperAdmin/dashboard", "SuperAdmin"})
    public String dashboard(Model model) {
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
        model.addAttribute("CantidadUsuariosRegistrados", usuarioRepository.getCantidadRegistrados());
        model.addAttribute("CantidadUsuariosActivos", usuarioRepository.getCantidadActivos());
        // Cantidad de usuarios baneados
        model.addAttribute("CantidadUsuariosBaneados", usuarioRepository.getCantidadBaneados());
        // Cantidad de proveedores baneados
        model.addAttribute("CantidadProveedoresBaneados", proveedorRepository.countProveedoresBaneados());
        return "SuperAdmin/Dashboard/dashboard-superadmin";
    }

    @GetMapping("SuperAdmin/listaAdminZonal")
    public String listaGestionAdminZonal(Model model, @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(value = "busqueda", required = false) String busqueda) {
        try {
            int pageSize = 6;
            Pageable pageable = PageRequest.of(page, pageSize);

            Page<Usuario> finalUsersList = usuarioRepository.findByRol_Id(2, pageable);
            if (finalUsersList.isEmpty()) {
                model.addAttribute("msg", "No hay coordinadores zonales registrados");
            } else {

                model.addAttribute("usuarios", finalUsersList.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", finalUsersList.getTotalPages());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la lista de coordinadores zonales.");
            e.printStackTrace();
        }

        return "SuperAdmin/GestionAdminZonal/admin-zonal-list";
    }

    @GetMapping("SuperAdmin/editarAdminZonal/{id}")
    public String editarAdminZonal(@PathVariable("id") Integer id, Model model) {
        try {
            Optional<Usuario> optionalAZ = usuarioRepository.findById(id);
            if (optionalAZ.isPresent() && optionalAZ.get().getRol().getId() == 2) {
                model.addAttribute("usuario", optionalAZ.get());
            } else {
                model.addAttribute("error", "Admin Zonal no encontrado o el rol no es válido");
                return "redirect:/SuperAdmin/listaAdminZonal";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el admin zonal para editar.");
            e.printStackTrace();
        }
        model.addAttribute("zonas", zonaRepository.findAll());
        return "SuperAdmin/GestionAdminZonal/admin-zonal-edit";
    }
    @GetMapping("SuperAdmin/verAdminZonal/{id}")
    public String verAdminZonal(@PathVariable("id") Integer id, Model model){
        Optional<Usuario> optionalAZ = usuarioRepository.findById(id);
        if (optionalAZ.isPresent() && optionalAZ.get().getRol().getId() == 2) {
            model.addAttribute("usuario", optionalAZ.get());
        } else {
            model.addAttribute("error", "Admin Zonal no encontrado o el rol no es válido");
            return "redirect:/SuperAdmin/listaAdminZonal";
        }
        return "SuperAdmin/GestionAdminZonal/admin-zonal-info";
    }

    @GetMapping("SuperAdmin/crearAdminZonal")
    public String crearAdminZonal(Model model) {
        System.out.println("ola");
        Usuario usuarioNuevoAdminZonal = new Usuario();
        //usuario1.setId(usuarioRepository.ultimoId()+1);
        model.addAttribute("distritos", distritoRepository.findAll());
        model.addAttribute("usuarioAdminZ", usuarioNuevoAdminZonal);
        model.addAttribute("zonas", zonaRepository.findAll());
        return "SuperAdmin/GestionAdminZonal/create-zonal-admin";
    }

    @PostMapping("/SuperAdmin/AdminZonal/guardar")
    public String guardarAdminZonal(@ModelAttribute("usuario") Usuario usuario, @RequestParam("zonaId") Integer zonaId, RedirectAttributes attr,
                                    @RequestParam("zonalAdminPhoto") MultipartFile foto) {
        System.out.println("guardar nuevo admin zonal prueba");
        //usuario.setId(null);
        //usuario.setId(null);
        String passGuardada = null;
        try {
            // Verificar si ya existen 2 coordinadores en la zona
            int cantidadCoordinadores = usuarioRepository.countCoordinadoresByZona(zonaId);
            if (usuario.getId() != null) {
                Optional<Usuario> usuarioExistente = usuarioRepository.findById(usuario.getId());
                if (usuarioExistente.isPresent() && usuarioExistente.get().getZona().getId().equals(zonaId)) {
                    cantidadCoordinadores--; // No contar al usuario actual si pertenece a esta zona
                }
            }
            if (cantidadCoordinadores >= 2) {
                attr.addFlashAttribute("error", "Ya existen 2 coordinadores en esta zona.");
                if (usuario.getId() == null) {
                    return "redirect:/SuperAdmin/crearAdminZonal";
                } else {
                    return "redirect:/SuperAdmin/editarAdminZonal/" + usuario.getId();
                }
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
            Optional<Usuario> usuario1 = Optional.of(new Usuario());
            if (usuario.getId() != null){
                usuario1 = usuarioRepository.findById(usuario.getId());
                boolean fotoActualizada = false;
                if (foto.isEmpty()){
                    usuario.setFoto(usuario1.get().getFoto());
                }else{
                    fotoActualizada = true;
                    usuario.setFoto(foto.getBytes());
                }


                ArrayList<String> datosAntiguos = new ArrayList<>();
                ArrayList<String> datosNuevos = new ArrayList<>();
                ArrayList<String> camposModificados = new ArrayList<>();

                if (!Objects.equals(usuario.getTelefono(), usuario1.get().getTelefono())) {
                    datosNuevos.add(usuario.getTelefono());
                    datosAntiguos.add(usuario1.get().getTelefono());
                    camposModificados.add("Teléfono");
                }
                if (!Objects.equals(usuario.getEmail(), usuario1.get().getEmail())) {
                    datosNuevos.add(usuario.getEmail());
                    datosAntiguos.add(usuario1.get().getEmail());
                    camposModificados.add("Email");
                }
                if (!Objects.equals(usuario.getDireccion(), usuario1.get().getDireccion())) {
                    datosNuevos.add(usuario.getDireccion());
                    datosAntiguos.add(usuario1.get().getDireccion());
                    camposModificados.add("Dirección");
                }
                if (!Objects.equals(usuario.getZona().getNombreZona(), usuario1.get().getZona().getNombreZona())) {
                    datosNuevos.add(usuario.getZona().getNombreZona());
                    datosAntiguos.add(usuario1.get().getZona().getNombreZona());
                    camposModificados.add("Zona");
                }
                if (!Objects.equals(usuario.getAzFechanacimiento(), usuario1.get().getAzFechanacimiento())) {
                    datosNuevos.add(String.valueOf(usuario.getAzFechanacimiento()));
                    datosAntiguos.add(String.valueOf(usuario1.get().getAzFechanacimiento()));
                    camposModificados.add("Fecha de nacimiento");
                }
                if(fotoActualizada){
                    camposModificados.add("La foto de perfil ha sido actualizada");
                }

                if (!datosAntiguos.isEmpty() || fotoActualizada) {
                    emailService.actualizacionInfoUserGenerico(usuario.getEmail(), usuario.getNombre(),camposModificados, datosAntiguos, datosNuevos);
                }
            }else {
                passGuardada = usuario.getContrasena();
                String encryptedPassword = passwordEncoder.encode(usuario.getContrasena());
                usuario.setContrasena(encryptedPassword);
            }
            usuario.setActivo(1);
            if (usuario.getId() == null) {
                emailService.emailParaAdminZonal(usuario.getEmail(), usuario.getNombre(), passGuardada, usuario.getZona().getNombreZona());
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

    @GetMapping("/SuperAdmin/AdminZonal/eliminar")
    public String eliminarAdminZonal(@RequestParam("idUsuarioFinal") int id, RedirectAttributes attr) {

        Optional<Usuario> optProduct = usuarioRepository.findById(id);

        if (optProduct.isPresent()) {
            System.out.println("Admin Zonal encontrado con ID: " + id);
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
    public String listaGestionAgente(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        try {
            int size = 6;
            // Configuramos el Pageable
            Pageable pageable = PageRequest.of(page, size);

            // Si tienes una consulta personalizada, la adaptamos a un método paginado
            Page<Object[]> agentesData = usuarioRepository.mostrarAgentesConPaginacion(pageable);

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
                    agente.put("agt_codigoaduana", row[6] != null ? row[6].toString() : ""); // Maneja nulos para código aduana
                    agente.put("estadoCodigoAduana", row[7] != null ? row[7].toString() : ""); // Maneja nulos para estado aduana
                    agente.put("agt_codigojurisdiccion", row[8] != null ? row[8].toString() : ""); // Maneja nulos para código jurisdicción
                    agente.put("estadoCodigoJurisdiccion", row[9] != null ? row[9].toString() : ""); // Maneja nulos para estado jurisdicción
                    agente.put("agt_razonsocial", row[10] != null ? row[10].toString() : ""); // Maneja nulos para razón social
                    agente.put("nombrezona", row[11].toString());

                    agentes.add(agente);
                }

                model.addAttribute("agentes", agentes);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", agentesData.getTotalPages());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la lista de agentes.");
            e.printStackTrace();
        }
        return "SuperAdmin/GestionAgentes/agent-list";
    }


    @GetMapping("SuperAdmin/editarAgente/{id}")
    public String editarAgente(@ModelAttribute("agente") Usuario agente, @PathVariable("id") Integer id, Model model) {
        try {
            Optional<Usuario> optionalAgente = usuarioRepository.findById(id);
            if (optionalAgente.isPresent() && optionalAgente.get().getRol().getId() == 3) {
                model.addAttribute("agente", optionalAgente.get());
                Usuario eigent = optionalAgente.get();
                System.out.println("Código de Aduana: " + eigent.getAgtCodigoaduana());
                System.out.println("Estado de Aduana: " + usuarioRepository.findEstadoAduana(eigent.getAgtCodigoaduana()));
                model.addAttribute("estado",usuarioRepository.findEstadoAduana(eigent.getAgtCodigoaduana()));
            } else {
                model.addAttribute("error", "Agente no encontrado o el rol no es válido");
                return "SuperAdmin/GestionAgentes/agent-list";  // Redirigir a la lista si no se encuentra el agente
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el agente para editar.");
            e.printStackTrace();
        }
        model.addAttribute("zonas", zonaRepository.findAll());
        return "SuperAdmin/GestionAgentes/agent-edit";
    }

    @GetMapping("SuperAdmin/verAgente/{id}")
    public String verAgente(@PathVariable("id") Integer id, Model model, @RequestParam(required = false) String origin){
        Optional<Usuario> optionalAgente = usuarioRepository.findById(id);
        if (optionalAgente.isPresent() && optionalAgente.get().getRol().getId() == 3) {
            model.addAttribute("usuario", optionalAgente.get());
            model.addAttribute("origin", origin);
        } else {
            model.addAttribute("error", "Agente no encontrado o el rol no es válido");
            return "SuperAdmin/GestionAgentes/agent-list";  // Redirigir a la lista si no se encuentra el agente
        }
        return "SuperAdmin/GestionAgentes/agent-ver-usuario";
    }

    @PostMapping("/SuperAdmin/Agente/guardar")
    public String guardarAgente(@ModelAttribute("agente") @Validated(AgenteValidationGroup.class) Usuario agente, BindingResult bindingResult, Model model, @RequestParam("agentPhoto") MultipartFile foto, @RequestParam("estado") String estado, RedirectAttributes attr) {

        System.out.println("Llega al método guardarAgente");
        if(bindingResult.hasErrors()){
            System.out.println("Errores en el formulario");
            bindingResult.getFieldErrors().forEach(error -> {
                System.out.println("Campo: " + error.getField() + " - Error: " + error.getDefaultMessage());
            });
            // Validación de DNI con prioridad
            if (bindingResult.hasFieldErrors("dni")) {
                if (bindingResult.getFieldError("dni").getCode().equals("NotBlank")) {
                    model.addAttribute("dniError", "Debe ingresar un número de DNI");
                } else if (bindingResult.getFieldError("dni").getCode().equals("Size")) {
                    model.addAttribute("dniError", "El DNI debe tener exactamente 8 dígitos");
                } else if (bindingResult.getFieldError("dni").getCode().equals("Pattern")) {
                    model.addAttribute("dniError", "El DNI debe contener solo dígitos");
                }
            }

            // Validación de Teléfono con prioridad
            if (bindingResult.hasFieldErrors("telefono")) {
                if (bindingResult.getFieldError("telefono").getCode().equals("NotBlank")) {
                    model.addAttribute("telefonoError", "Debe ingresar un número de teléfono");
                } else if (bindingResult.getFieldError("telefono").getCode().equals("Size")) {
                    model.addAttribute("telefonoError", "El teléfono debe tener exactamente 9 dígitos");
                } else if (bindingResult.getFieldError("telefono").getCode().equals("Pattern")) {
                    model.addAttribute("telefonoError", "El teléfono debe contener solo dígitos");
                }
            }

            // Prioridad en otros campos...
            if (bindingResult.hasFieldErrors("agtCodigojurisdiccion")) {
                if (bindingResult.getFieldError("agtCodigojurisdiccion").getCode().equals("NotBlank")) {
                    model.addAttribute("agtCodigojurisdiccionError", "El código de jurisdicción no puede estar vacío");
                } else if (bindingResult.getFieldError("agtCodigojurisdiccion").getCode().equals("Size")) {
                    model.addAttribute("agtCodigojurisdiccionError", "El código de jurisdicción debe tener entre 4 y 6 dígitos");
                } else if (bindingResult.getFieldError("agtCodigojurisdiccion").getCode().equals("Pattern")) {
                    model.addAttribute("agtCodigojurisdiccionError", "El código de jurisdicción debe contener solo dígitos");
                }
            }

            if (bindingResult.hasFieldErrors("agtCodigoaduana")) {
                if (bindingResult.getFieldError("agtCodigoaduana").getCode().equals("NotBlank")) {
                    model.addAttribute("agtCodigoaduanaError", "El código aduanero no puede estar vacío");
                } else if (bindingResult.getFieldError("agtCodigoaduana").getCode().equals("Size")) {
                    model.addAttribute("agtCodigoaduanaError", "El código aduanero debe tener exactamente 6 dígitos");
                } else if (bindingResult.getFieldError("agtCodigoaduana").getCode().equals("Pattern")) {
                    model.addAttribute("agtCodigoaduanaError", "El código aduanero debe contener solo dígitos");
                }
            }


            model.addAttribute("zonas", zonaRepository.findAll());
            model.addAttribute("estado", estado);
            return "SuperAdmin/GestionAgentes/agent-edit";
        }

        try {
            Usuario usuario1 = usuarioRepository.findUsuarioById(agente.getId());
            ArrayList<String> datosAntiguos = new ArrayList<>();
            ArrayList<String> datosNuevos = new ArrayList<>();
            ArrayList<String> camposModificados = new ArrayList<>();

            if (!Objects.equals(agente.getTelefono(), usuario1.getTelefono())) {
                datosNuevos.add(agente.getTelefono());
                datosAntiguos.add(usuario1.getTelefono());
                camposModificados.add("Teléfono");
            }
            if (!Objects.equals(agente.getEmail(), usuario1.getEmail())) {
                datosNuevos.add(agente.getEmail());
                datosAntiguos.add(usuario1.getEmail());
                camposModificados.add("Email");
            }
            if (!Objects.equals(agente.getAgtRazonsocial(), usuario1.getAgtRazonsocial())) {
                datosNuevos.add(agente.getAgtRazonsocial());
                datosAntiguos.add(usuario1.getAgtRazonsocial());
                camposModificados.add("Razón Social");
            }

            if (!Objects.equals(agente.getAgtCodigoaduana(), usuario1.getAgtCodigoaduana())) {
                datosNuevos.add(String.valueOf(agente.getAgtCodigoaduana()));
                datosAntiguos.add(String.valueOf(usuario1.getAgtCodigoaduana()));
                camposModificados.add("Codigo de despachador aduanero");
            }
            if (!Objects.equals(agente.getAgtCodigojurisdiccion(), usuario1.getAgtCodigojurisdiccion())) {
                datosNuevos.add(String.valueOf(agente.getAgtCodigojurisdiccion()));
                datosAntiguos.add(String.valueOf(usuario1.getAgtCodigojurisdiccion()));
                camposModificados.add("Codigo jurisdiccion");
            }
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
            agenteExistente.setDireccion(agenteExistente.getDireccion());
            agenteExistente.setDistrito(agenteExistente.getDistrito());
            agenteExistente.setAgtRuc(agenteExistente.getAgtRuc());
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
            System.out.println("Foto vacía: " + foto.isEmpty());
            System.out.println("Bytes de foto: " + (foto.isEmpty() ? "No hay bytes" : foto.getBytes().length));


            boolean fotoActualizada = false;
            if (foto != null && !foto.isEmpty()) {
                // Solo actualiza la foto si se subió una nueva
                fotoActualizada = true;
                agenteExistente.setFoto(foto.getBytes());
            } else {
                // Mantén la foto existente si no se subió ninguna nueva
                agenteExistente.setFoto(agenteExistente.getFoto());
            }
            if(fotoActualizada){
                camposModificados.add("La foto de perfil ha sido actualizada");
            }

            if (!datosAntiguos.isEmpty() || fotoActualizada) {
                emailService.actualizacionInfoUserGenerico(agente.getEmail(), agente.getNombre(),camposModificados, datosAntiguos, datosNuevos);
            }
            usuarioRepository.save(agenteExistente);
            attr.addFlashAttribute("msg", "Información del agente actualizada exitosamente");

        } catch (Exception e) {
            attr.addFlashAttribute("error", "Error al guardar el agente.");
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
                usuarioRepository.logicalDelete(id);
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
    public String listaSolicitudesAgentes(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        int size = 6;
        // Configurar la paginación
        Pageable pageable = PageRequest.of(page, size);

        // Realizar la consulta paginada
        Page<solAgente> listaUsuariosSolicitudes = usuarioRepository.mostrarSolicitudesAgenteConPaginacion(pageable);

        // Verificar si hay resultados
        if (listaUsuariosSolicitudes.isEmpty()) {
            model.addAttribute("msg", "No hay solicitudes registradas");
        } else {
            // Añadir la lista de solicitudes al modelo
            model.addAttribute("listaUsuariosSolicitudes", listaUsuariosSolicitudes.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", listaUsuariosSolicitudes.getTotalPages());
        }

        // Redireccionar a la vista correspondiente
        return "SuperAdmin/GestionAgentes/agent-request";
    }

    @PostMapping("SuperAdmin/listaSolicitudesAgentesFiltro")
    public String listaSolicitudesAgentesFiltro(Model model, @RequestParam(value = "indicador", required = false) Integer indicador) {

        if (indicador == null) {
            return "redirect:/SuperAdmin/listaSolicitudesAgentes";
        }

        List<solAgente> listaUsuariosSolicitudes = usuarioRepository.mostrarSolicitudesAgenteFiltro(indicador);

        // Añadir la lista de solicitudes al modelo
        model.addAttribute("listaUsuariosSolicitudes", listaUsuariosSolicitudes);

        // Redireccionar a la vista correspondiente
        return "SuperAdmin/GestionAgentes/agent-request";
    }


    @GetMapping("/SuperAdmin/rechazarSolicitudAgente")
    public String RechazarSolicitudAgente(@RequestParam Integer id, @RequestParam("indicador") Integer indicador,
                                          RedirectAttributes redirectAttributes) {

        Optional<Usuario> optUsuario = usuarioRepository.findById(id);

        if (optUsuario.isPresent()) {

            Usuario usuario = optUsuario.get();

            solicitudAgenteRepository.deleteByIdUsuario(usuario);

            redirectAttributes.addFlashAttribute("msg", "La solicitud ha sido rechazada exitosamente.");

            switch (indicador){
                case 0:
                    //No hace nada y elimina la solicitud
                    break;
                case 1:
                    //Eliminar cuenta
                    usuarioRepository.deleteById(id);
                    break;
            }

            return "redirect:/SuperAdmin/listaSolicitudesAgentes";

        } else {
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al rechazar la solicitud.");

            return "redirect:/SuperAdmin/listaSolicitudesAgentes";
        }

    }


    @GetMapping("SuperAdmin/verUsuarioDeSolicitud/{id}")
    public String verUsuarioDeSolicitud(Model model, @PathVariable("id") Integer idUsuarioFinal) {
        Optional<Usuario> finalUser = usuarioRepository.findById(idUsuarioFinal);
        model.addAttribute("usuario", finalUser.get());
        return "SuperAdmin/GestionAgentes/agent-ver-usuario";
    }


    @GetMapping("SuperAdmin/aceptarSolicitud")
    public String cambiarRolaAgente(Model model, @RequestParam("id") Integer id,
                                    @RequestParam("indicador") Integer indicador,
                                    @RequestParam("codigoAduana") String codigoAduana,
                                    @RequestParam("codigoJurisdiccion") String codigoJurisdiccion,
                                    RedirectAttributes redirectAttributes) {
        System.out.println("pruebaaaa");
        System.out.println("ID: " + id + ", Indicador: " + indicador + ", Codigo Aduana: " + codigoAduana + ", Codigo Jurisdicción: " + codigoJurisdiccion);
        if (id == null || indicador == null) {
            redirectAttributes.addFlashAttribute("error", "Faltan parámetros obligatorios (ID o Indicador).");
            return "redirect:/SuperAdmin/listaSolicitudesAgentes";
        }
        String passG = null;
        try {
            // Obtener el usuario solicitante por ID
            Usuario usuario = usuarioRepository.findUsuarioById(id);
            Integer zonaUsuario = usuario.getDistrito().getZona().getId();
            Integer idAdminZonal = usuarioRepository.obtenerUnAdminZonalDesuZona(zonaUsuario);
            usuarioRepository.asignarAdminZonalAUsuario(idAdminZonal, id);
            // Verificar si el Admin Zonal ya tiene 3 agentes activos asignados
            Integer cantidadAgentes = usuarioRepository.contarAgentesPorAdminZonal(idAdminZonal);
            System.out.println(cantidadAgentes);

            if (cantidadAgentes >= 3) {
                // Usar RedirectAttributes para que el mensaje se vea después del redirect
                redirectAttributes.addFlashAttribute("error", "Este Administrador Zonal ya tiene el máximo de 3 agentes activos.");
                return "redirect:/SuperAdmin/listaSolicitudesAgentes";
            }
            Usuario agente= usuarioRepository.findById(id).orElseThrow();
            Usuario AdminZonal = usuarioRepository.findById(idAdminZonal).orElseThrow();

            String passEncriptada = passwordEncoder.encode(agente.getContrasena());
            passG = agente.getContrasena();

            usuarioRepository.actualizarPasswordAAgente(id, passEncriptada);

            // Cambiar el rol del usuario o activar la cuenta, según el indicador
            switch (indicador) {
                case 0:
                    usuarioRepository.actualizarRolAAgente(id, codigoAduana, codigoJurisdiccion);
                    break;
                case 1:
                    usuarioRepository.activarCuenta(id);
                    break;
            }

            // Eliminar la solicitud
            solicitudAgenteRepository.deleteByIdUsuario(usuario);
            emailService.emailParaAgente(agente.getEmail(),agente.getNombre(),passG,agente.getZona().getNombreZona(), AdminZonal.getNombre(),AdminZonal.getApellidoPaterno());
            redirectAttributes.addFlashAttribute("msg", "Solicitud aceptada con éxito!");
            return "redirect:/SuperAdmin/listaSolicitudesAgentes";
        }catch(Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error al procesar la solicitud.");
            return "redirect:/SuperAdmin/listaSolicitudesAgentes";

        }
    }






    @GetMapping("SuperAdmin/listaUsuarioFinal")
    public String listaGestionUsuarioFinal(@RequestParam(defaultValue = "0") int page,
                                           Model model) {

        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Usuario> finalUsersList = usuarioRepository.findByRol_Id(4, pageable);

        model.addAttribute("finalUsersList", finalUsersList.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", finalUsersList.getTotalPages());

        return "SuperAdmin/GestionUsuarioFinal/final-users-list";
    }

    @GetMapping("SuperAdmin/listaUsuariosBaneados")
    public String listaUsuariosBaneados(@RequestParam(defaultValue = "0") int page,
                                        Model model){

        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Usuario> BannedUsersList = usuarioRepository.find_users_banned(pageable);
        model.addAttribute("bannedUsersList", BannedUsersList.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", BannedUsersList.getTotalPages());
        return "SuperAdmin/banned-users-list";

    }

    @GetMapping("SuperAdmin/desbanearUsuario/{id}")
    public String desbanearUsuario(@PathVariable("id") Integer idUsuario, Model model) {
        usuarioRepository.quitarBanUsuario(idUsuario);
        return "redirect:/SuperAdmin/listaUsuariosBaneados";
    }

    @GetMapping("SuperAdmin/crearUsuarioFinal")
    public String crearUsuarioFinal() {

        return "SuperAdmin/GestionUsuarioFinal/create-final-user";
    }

    @GetMapping("/SuperAdmin/editarUsuarioFinal/{id}")
    public String editarUsuarioFinal(Model model, @PathVariable("id") Integer idUsuarioFinal) {
        Optional<Usuario> finalUser = usuarioRepository.findById(idUsuarioFinal);
        Usuario u = new Usuario();
        if(finalUser.isPresent()){
            u = finalUser.get();
            model.addAttribute("finalUser", u);
            model.addAttribute("idUsuarioPOST", u.getId());

        }
        List<Distrito> listaDistritos = distritoRepository.findAll();
        model.addAttribute("listaDistritos", listaDistritos);
        return "SuperAdmin/GestionUsuarioFinal/final-user-edit";
    }

    @GetMapping("/SuperAdmin/verUsuarioFinal/{id}")
    public String verUsuarioFinal(Model model, @PathVariable("id") Integer idUsuarioFinal) {
        Optional<Usuario> finalUser = usuarioRepository.findById(idUsuarioFinal);
        model.addAttribute("finalUser", finalUser.get());
        return "SuperAdmin/GestionUsuarioFinal/final-user-info";
    }

    @PostMapping("/SuperAdmin/UsuarioFinal/Actualizar")
    public String actualizarUsuarioFinal(@ModelAttribute("usuario") @Validated(UsuarioFinalValidationGroup.class) Usuario usuario,
                                         BindingResult bindingResult,
                                         Model model,
                                         @RequestParam("UserPhoto") MultipartFile foto, RedirectAttributes attr) throws IOException {

        System.out.println("Hola: " + usuario.getNombre());
        if (bindingResult.hasErrors()) {
            System.out.println("Errores de validación:");
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println(error.getDefaultMessage());
            });

            // Agrega el objeto usuario al modelo con el nombre esperado por la vista
            model.addAttribute("finalUser", usuario);

            // Validación de DNI con prioridad
            if (bindingResult.hasFieldErrors("dni")) {
                if (bindingResult.getFieldError("dni").getCode().equals("NotBlank")) {
                    model.addAttribute("dniError", "Debe ingresar un número de DNI");
                } else if (bindingResult.getFieldError("dni").getCode().equals("Size")) {
                    model.addAttribute("dniError", "El DNI debe tener exactamente 8 dígitos");
                } else if (bindingResult.getFieldError("dni").getCode().equals("Pattern")) {
                    model.addAttribute("dniError", "El DNI debe contener solo dígitos");
                }
            }

            // Validación de Teléfono con prioridad
            if (bindingResult.hasFieldErrors("telefono")) {
                if (bindingResult.getFieldError("telefono").getCode().equals("NotBlank")) {
                    model.addAttribute("telefonoError", "Debe ingresar un número de teléfono");
                } else if (bindingResult.getFieldError("telefono").getCode().equals("Size")) {
                    model.addAttribute("telefonoError", "El teléfono debe tener exactamente 9 dígitos");
                } else if (bindingResult.getFieldError("telefono").getCode().equals("Pattern")) {
                    model.addAttribute("telefonoError", "El teléfono debe contener solo dígitos");
                }
            }

            // Agrega la lista de distritos
            List<Distrito> listaDistritos = distritoRepository.findAll();
            model.addAttribute("listaDistritos", listaDistritos);

            return "SuperAdmin/GestionUsuarioFinal/final-user-edit";
        }
        boolean fotoActualizada= false;
        // Actualización de datos del usuario
        Optional<Usuario> usuario1 = usuarioRepository.findById(usuario.getId());

        ArrayList<String> datosAntiguos = new ArrayList<>();
        ArrayList<String> datosNuevos = new ArrayList<>();
        ArrayList<String> camposModificados = new ArrayList<>();

        if (!Objects.equals(usuario.getTelefono(), usuario1.get().getTelefono())) {
            datosNuevos.add(usuario.getTelefono());
            datosAntiguos.add(usuario1.get().getTelefono());
            camposModificados.add("Teléfono");
        }
        if (!Objects.equals(usuario.getEmail(), usuario1.get().getEmail())) {
            datosNuevos.add(usuario.getEmail());
            datosAntiguos.add(usuario1.get().getEmail());
            camposModificados.add("Email");
        }
        if (!Objects.equals(usuario.getDireccion(), usuario1.get().getDireccion())) {
            datosNuevos.add(usuario.getDireccion());
            datosAntiguos.add(usuario1.get().getDireccion());
            camposModificados.add("Dirección");
        }
        if (!Objects.equals(usuario.getDistrito().getNombre(), usuario1.get().getDistrito().getNombre())) {
            datosNuevos.add(String.valueOf(usuario.getDistrito().getNombre()));
            datosAntiguos.add(String.valueOf(usuario1.get().getDistrito().getNombre()));
            camposModificados.add("Distrito");
        }
        usuario.setNombre(usuario1.get().getNombre());
        usuario.setApellidoPaterno(usuario1.get().getApellidoPaterno());
        usuario.setApellidoMaterno(usuario1.get().getApellidoMaterno());
        if (foto.isEmpty()) {

            Usuario finalUser = usuarioRepository.findById(usuario.getId()).orElseThrow();
            emailService.actualizacionInfoUserGenerico(usuario.getEmail(), usuario.getNombre(),camposModificados, datosAntiguos, datosNuevos);
            usuarioRepository.actualizarUsuarioFinal(usuario.getDni(), usuario.getNombre(), usuario.getApellidoPaterno(), usuario.getApellidoMaterno(), usuario.getEmail(), usuario.getDireccion(), usuario.getTelefono(), usuario.getDistrito().getId(), finalUser.getFoto(), usuario.getId());
        } else {
            fotoActualizada = true;
            try {
                byte[] fotoBytes = foto.getBytes();
                usuario.setFoto(fotoBytes);
                camposModificados.add("La foto de perfil ha sido actualizada");
                emailService.actualizacionInfoUserGenerico(usuario.getEmail(), usuario.getNombre(),camposModificados, datosAntiguos, datosNuevos);
                usuarioRepository.actualizarUsuarioFinal(usuario.getDni(), usuario.getNombre(), usuario.getApellidoPaterno(), usuario.getApellidoMaterno(), usuario.getEmail(), usuario.getDireccion(), usuario.getTelefono(), usuario.getDistrito().getId(), usuario.getFoto(), usuario.getId());
            } catch (IOException ignored) {
            }
        }
        attr.addFlashAttribute("msg", "Información del usuario final actualizada exitosamente");

        return "redirect:/SuperAdmin/listaUsuarioFinal";
    }

    @GetMapping("/usuarioFinal/{id}")
    public ResponseEntity<ByteArrayResource> obtenerFotoUsuarioFinal(@PathVariable Integer id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        byte[] foto = usuario.getFoto();
        ByteArrayResource resource = new ByteArrayResource(foto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"foto_" + usuario.getApellidoPaterno() + "_" + usuario.getNombre() + ".jpg\"")
                .contentLength(foto.length)
                .body(resource);
    }

    @PostMapping("/SuperAdmin/banearUsuario")
    public String banearUsuario(
            @RequestParam("idUsuarioFinal") Integer idUsuarioFinal,
            @RequestParam("razon") String razon, RedirectAttributes attr) {
        System.out.println(idUsuarioFinal);
        System.out.println(razon);
        usuarioRepository.banUsuario(idUsuarioFinal, razon);
        Usuario usuario = usuarioRepository.findById(idUsuarioFinal).orElseThrow();
        switch (usuario.getRol().getId()){
            case 4:
                attr.addFlashAttribute("msg", "Usuario eliminado exitosamente");
                return "redirect:/SuperAdmin/listaUsuarioFinal";
            case 3:
                attr.addFlashAttribute("msg", "Agente eliminado exitosamente");
                return "redirect:/SuperAdmin/listaAgente";
            case 2:
                attr.addFlashAttribute("msg", "Admin Zonal eliminado exitosamente");
                return "redirect:/SuperAdmin/listaAdminZonal";
            default:
                return "redirect:/SuperAdmin";
        }

    }


    @GetMapping("SuperAdmin/agregarProducto")
    public String agregarProducto(@ModelAttribute("producto") Producto producto,Model model, @RequestParam(value = "idCategoria", required = false) Integer idCategoria) {
        List<Categoria> categorias = categoriaRepository.findAll();
        List<Proveedor> proveedores = proveedorRepository.findAll();
        List<Zona> zonas = zonaRepository.findAll();
        List<Subcategoria> subcategorias;

        if (idCategoria != null) {
            subcategorias = subcategoriaRepository.findByCategoria_Id(idCategoria);
        } else {
            subcategorias = List.of();
        }

        Map<Integer, Integer> productoZonas = new HashMap<>();

        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categorias);
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("zonas", zonas);
        model.addAttribute("subcategorias", subcategorias);
        model.addAttribute("productoZonas", productoZonas);

        return "SuperAdmin/add-product";
    }

    @PostMapping("/SuperAdmin/subirFotos")
    public String subirFotosProducto(@RequestParam("fotos") MultipartFile[] fotos,
                                     @RequestParam("idProducto") Integer idProducto,
                                     RedirectAttributes attr) {
        if (fotos == null || fotos.length == 0 || fotos[0].isEmpty()) {
            attr.addFlashAttribute("error", "No se han seleccionado fotos para subir.");
            return "redirect:/SuperAdmin/agregarProducto";
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        for (MultipartFile foto : fotos) {
            if (!foto.isEmpty()) {
                try {
                    Fotosproducto fotosProducto = new Fotosproducto();
                    fotosProducto.setFoto(foto.getBytes());
                    fotosProducto.setFotoNombre(foto.getOriginalFilename());
                    fotosProducto.setFotoContentType(foto.getContentType());
                    fotosProducto.setProducto(producto);
                    fotosProductoRepository.save(fotosProducto);
                } catch (IOException e) {
                    e.printStackTrace();
                    attr.addFlashAttribute("error", "Ocurrió un error al subir la imagen.");
                    return "redirect:/SuperAdmin/agregarProducto";
                }
            }
        }
        attr.addFlashAttribute("msg", "Fotos subidas correctamente.");
        return "redirect:/SuperAdmin/productos";
    }

    @GetMapping("/SuperAdmin/producto/foto/{id}")
    public ResponseEntity<byte[]> obtenerFotoProducto(@PathVariable Integer id) {
        List<Fotosproducto> fotosProductos = fotosProductoRepository.findByProducto_Id(id);

        if (!fotosProductos.isEmpty()) {
            Fotosproducto fotoProducto = fotosProductos.get(0);
            byte[] imagenComoBytes = fotoProducto.getFoto();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(fotoProducto.getFotoContentType()));

            return new ResponseEntity<>(
                    imagenComoBytes,
                    httpHeaders,
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/SuperAdmin/crearProducto")
    public String guardarcrearProducto(@ModelAttribute("producto")  Producto producto,
                                       BindingResult bindingResult, Model model,
                                       @RequestParam Map<String, String> allParams,
                                       @RequestParam(value = "idCategoria", required = false) Integer idCategoria,
                                       @RequestParam("fotoPrincipal") MultipartFile fotoPrincipal,
                                       @RequestParam("fotosExtras") MultipartFile[] fotosExtras,
                                       RedirectAttributes attr) {
        try {
            // Generar código único para el producto
            String codigoProducto = generarCodigoUnico();
            producto.setCodigoProducto(codigoProducto);

            if (producto.getBorrado() == null) {
                producto.setBorrado(0);
            }
            if (producto.getCantVentas() == null) {
                producto.setCantVentas(0);
            }
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("zona-")) {
                    String zonaKey = entry.getKey();
                    String cantidadStr = entry.getValue();

                    if (cantidadStr == null || cantidadStr.isEmpty()) {
                        continue;
                    }

                    Integer zonaId = Integer.parseInt(zonaKey.split("-")[1]);
                    Integer cantidad = Integer.parseInt(cantidadStr);
                    if (cantidad > 0) {
                        Optional<Zona> optionalZona = zonaRepository.findById(zonaId);
                        if (optionalZona.isPresent()) {
                            Zona zona = optionalZona.get();
                            Producto nuevoProductoPorZona = new Producto();
                            nuevoProductoPorZona.setNombreProducto(producto.getNombreProducto());
                            nuevoProductoPorZona.setDescripcion(producto.getDescripcion());
                            nuevoProductoPorZona.setPrecio(producto.getPrecio());
                            nuevoProductoPorZona.setCostoEnvio(producto.getCostoEnvio());
                            nuevoProductoPorZona.setModelo(producto.getModelo());
                            nuevoProductoPorZona.setColor(producto.getColor());
                            nuevoProductoPorZona.setIdCategoria(producto.getIdCategoria());
                            nuevoProductoPorZona.setIdProveedor(producto.getIdProveedor());
                            nuevoProductoPorZona.setIdSubcategoria(producto.getIdSubcategoria());
                            nuevoProductoPorZona.setCantidadDisponible(cantidad);
                            nuevoProductoPorZona.setBorrado(producto.getBorrado());
                            nuevoProductoPorZona.setDisponibilidad("En stock");
                            nuevoProductoPorZona.setZona(zona);
                            nuevoProductoPorZona.setCodigoProducto(codigoProducto);

                            Producto savedProducto = productoRepository.save(nuevoProductoPorZona);
                            if (fotoPrincipal != null && !fotoPrincipal.isEmpty()) {
                                Fotosproducto fotoPrincipalEntity = new Fotosproducto();
                                fotoPrincipalEntity.setFoto(fotoPrincipal.getBytes());
                                fotoPrincipalEntity.setFotoNombre(fotoPrincipal.getOriginalFilename());
                                fotoPrincipalEntity.setFotoContentType(fotoPrincipal.getContentType());
                                fotoPrincipalEntity.setProducto(savedProducto);
                                fotosProductoRepository.save(fotoPrincipalEntity);
                            }

                            // Guardar las fotos extras si no están vacías
                            if (fotosExtras != null && fotosExtras.length > 0) {
                                for (MultipartFile fotoExtra : fotosExtras) {
                                    if (!fotoExtra.isEmpty()) {
                                        Fotosproducto fotosProducto = new Fotosproducto();
                                        fotosProducto.setFoto(fotoExtra.getBytes());
                                        fotosProducto.setFotoNombre(fotoExtra.getOriginalFilename());
                                        fotosProducto.setFotoContentType(fotoExtra.getContentType());
                                        fotosProducto.setProducto(savedProducto);
                                        fotosProductoRepository.save(fotosProducto);
                                    }
                                }
                            }
                        }
                    }
                    attr.addFlashAttribute("msg", "Producto creado exitosamente.");                }
            } } catch (Exception e) {
            attr.addFlashAttribute("error", "Ocurrió un error al crear el producto.");
            e.printStackTrace();
            }


        return "redirect:/SuperAdmin/productos";
    }

    // Método para generar código único
    private String generarCodigoUnico() {
        String codigoProducto;
        do {
            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            codigoProducto = uuid.substring(0, 8); // Ajusta la longitud según tus necesidades
        } while (productoRepository.existsByCodigoProducto(codigoProducto));
        return codigoProducto;
    }


    @PostMapping("/SuperAdmin/guardarProducto")
    public String guardareditarProducto(@ModelAttribute("producto") Producto producto,
                                        @RequestParam("zonaId") Integer zonaId,
                                        @RequestParam("cantidadZona") Integer cantidadZona,
                                        @RequestParam("fotoPrincipal") MultipartFile fotoPrincipal,
                                        @RequestParam("fotosExtras") MultipartFile[] fotosExtras,
                                        RedirectAttributes attr) {

        try {
            if (producto.getBorrado() == null) {
                producto.setBorrado(0);
            }
            Optional<Zona> optionalZona = zonaRepository.findById(zonaId);
            if (optionalZona.isPresent()) {
                Zona zona = optionalZona.get();
                Optional<Producto> productoEnZona = productoRepository.findByIdAndZona(producto.getId(), zona);
                if (productoEnZona.isPresent()) {
                    Producto productoActualizado = productoEnZona.get();

                    // Mantener el código existente
                    producto.setCodigoProducto(productoActualizado.getCodigoProducto());

                    productoActualizado.setDescripcion(producto.getDescripcion());
                    productoActualizado.setNombreProducto(producto.getNombreProducto());
                    productoActualizado.setPrecio(producto.getPrecio());
                    productoActualizado.setCostoEnvio(producto.getCostoEnvio());
                    productoActualizado.setModelo(producto.getModelo());
                    productoActualizado.setColor(producto.getColor());
                    productoActualizado.setIdCategoria(producto.getIdCategoria());
                    productoActualizado.setIdProveedor(producto.getIdProveedor());
                    productoActualizado.setIdSubcategoria(producto.getIdSubcategoria());
                    productoActualizado.setCantidadDisponible(cantidadZona);
                    productoActualizado.setBorrado(producto.getBorrado());
                    productoRepository.save(productoActualizado);

                    if (fotoPrincipal != null && !fotoPrincipal.isEmpty()) {
                        List<Fotosproducto> existingFotos = fotosProductoRepository.findByProducto_Id(productoActualizado.getId());
                        if (!existingFotos.isEmpty()) {
                            Fotosproducto existingFoto = existingFotos.get(0);
                            existingFoto.setFoto(fotoPrincipal.getBytes());
                            existingFoto.setFotoNombre(fotoPrincipal.getOriginalFilename());
                            existingFoto.setFotoContentType(fotoPrincipal.getContentType());
                            fotosProductoRepository.save(existingFoto);
                        } else {
                            Fotosproducto nuevaFoto = new Fotosproducto();
                            nuevaFoto.setFoto(fotoPrincipal.getBytes());
                            nuevaFoto.setFotoNombre(fotoPrincipal.getOriginalFilename());
                            nuevaFoto.setFotoContentType(fotoPrincipal.getContentType());
                            nuevaFoto.setProducto(productoActualizado);
                            fotosProductoRepository.save(nuevaFoto);
                        }
                    }

                    if (fotosExtras != null && fotosExtras.length > 0) {
                        for (MultipartFile fotoExtra : fotosExtras) {
                            if (!fotoExtra.isEmpty()) {
                                Fotosproducto nuevaFotoExtra = new Fotosproducto();
                                nuevaFotoExtra.setFoto(fotoExtra.getBytes());
                                nuevaFotoExtra.setFotoNombre(fotoExtra.getOriginalFilename());
                                nuevaFotoExtra.setFotoContentType(fotoExtra.getContentType());
                                nuevaFotoExtra.setProducto(productoActualizado);
                                fotosProductoRepository.save(nuevaFotoExtra);
                            }
                        }
                    }
                    attr.addFlashAttribute("msg", "Producto actualizado exitosamente.");
                } else {
                    attr.addFlashAttribute("error", "Producto no encontrado en la zona seleccionada.");
                }
            } else {
                attr.addFlashAttribute("error", "Zona no encontrada.");
            }
        } catch (Exception e) {
            attr.addFlashAttribute("error", "Ocurrió un error al actualizar el producto.");
            e.printStackTrace();
        }
        return "redirect:/SuperAdmin/productos";
    }

    @GetMapping("/SuperAdmin/editarProducto/{id}")
    public String editarProducto(@PathVariable("id") Integer id,
                                 @RequestParam(value = "zonaId", required = false) Integer zonaId,
                                 Model model) {

        Optional<Producto> optionalProducto = productoRepository.findById(id);
        if (optionalProducto.isEmpty()) {
            model.addAttribute("error", "Producto no encontrado.");
            return "redirect:/SuperAdmin/productos";
        }

        Producto producto = optionalProducto.get();
        List<Categoria> categorias = categoriaRepository.findAll();
        List<Proveedor> proveedores = proveedorRepository.findAll();
        List<Zona> zonas = zonaRepository.findAll();
        List<Subcategoria> subcategorias = subcategoriaRepository.findAll();

        // Make sure producto is added to the model
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categorias);
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("zonas", zonas);
        model.addAttribute("subcategorias", subcategorias);
        model.addAttribute("zonaSeleccionada", zonaId);
        model.addAttribute("cantidadEnZona", producto.getCantidadDisponible());

        return "SuperAdmin/edit-product";
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
    public String productos(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "0") Integer categoriaId) {

        int size = 2; // Tamaño de página
        Pageable pageable = PageRequest.of(page, size);

        Page<Producto> productosPage;

        if (categoriaId != 0) {
            // Filtrar por categoría
            productosPage = productoRepository.findProductosPorCategoriaConPaginacion(categoriaId, pageable);
        } else {
            // Mostrar todos los productos
            productosPage = productoRepository.findAllActiveConpaginacion(pageable);
        }

        int totalPages = productosPage.getTotalPages();
        int currentPage = page;

        // Calcular el rango de páginas visibles
        int startPage = Math.max(0, currentPage - 1);
        int endPage = Math.min(currentPage + 1, totalPages - 1);

        if (currentPage == 0) {
            // Si es la primera página, mostrar solo 2 páginas
            endPage = Math.min(1, totalPages - 1);
        } else if (currentPage == totalPages - 1) {
            // Si es la última página, ajustar el rango para mostrar las últimas 2
            startPage = Math.max(totalPages - 2, 0);
        }

        // Añadir atributos al modelo
        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalItems", productosPage.getTotalElements());
        model.addAttribute("selectedCategory", categoriaId);

        return "SuperAdmin/productos";
    }



    //
    @GetMapping("SuperAdmin/proveedores")
    public String proveedores(Model model,
                              @RequestParam(defaultValue = "0") int page) {
        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Tienda> tiendas = tiendaRepository.findAll(pageable);

        int totalPages = tiendas.getTotalPages();
        int currentPage = page;

        // Calcular el rango de páginas visibles
        int startPage = Math.max(0, currentPage - 1);
        int endPage = Math.min(currentPage + 1, totalPages - 1);

        if (currentPage == 0) {
            // Si es la primera página, mostrar solo 2 páginas
            endPage = Math.min(1, totalPages - 1);
        } else if (currentPage == totalPages - 1) {
            // Si es la última página, mostrar las últimas 2 páginas
            startPage = Math.max(totalPages - 2, 0);
        }

        model.addAttribute("tiendas", tiendas.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "SuperAdmin/GestionProveedores/vendor-grid";
    }


    @GetMapping("/SuperAdmin/listaProveedores")
    public String listaProveedores(Model model,
                                   @RequestParam(defaultValue = "0") int page) {
        int pageSize = 3;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Proveedor> proveedores = proveedorRepository.findAll(pageable);

        int totalPages = proveedores.getTotalPages();
        int currentPage = page;

        // Calcular el rango de páginas visibles
        int startPage = Math.max(0, currentPage - 1);
        int endPage = Math.min(currentPage + 1, totalPages - 1);

        if (currentPage == 0) {
            // Si es la primera página, mostrar solo las primeras 2 páginas
            endPage = Math.min(1, totalPages - 1);
        } else if (currentPage == totalPages - 1) {
            // Si es la última página, mostrar solo las últimas 2 páginas
            startPage = Math.max(totalPages - 2, 0);
        }

        // Añadir atributos al modelo
        model.addAttribute("proveedores", proveedores.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "SuperAdmin/GestionProveedores/vendor-list";
    }


    @GetMapping("/SuperAdmin/borrar")
    public String borrar(@RequestParam("id") int id, RedirectAttributes attr) {
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

    @GetMapping("/SuperAdmin/eliminarProveedor")
    public String eliminarProveedor(@RequestParam("id") int id, RedirectAttributes attr) {

        Optional<Proveedor> optProveedor = proveedorRepository.findById(id);

        if (optProveedor.isPresent()) {
            try {
                optProveedor.get().setBaneado(Boolean.TRUE);
                attr.addFlashAttribute("msg", "El Proveedor ha sido eliminado exitosamente");
            } catch (Exception e) {
                e.printStackTrace();
                attr.addFlashAttribute("error", "El Proveedor no se pudo borrar correctamente =(.");
            }
        }
        return "redirect:/SuperAdmin/listaProveedores";

    }

    @GetMapping("SuperAdmin/editarProveedor/{id}")
    public String editarProveedor(@PathVariable("id") Integer id, Model model) {
        try {
            Optional<Proveedor> optionalAZ = proveedorRepository.findById(id);
            if (optionalAZ.isPresent()) {
                List<Tienda> tiendas = tiendaRepository.findAll();
                System.out.println(tiendas.size());
                model.addAttribute("tiendas", tiendas);
                model.addAttribute("proveedor", optionalAZ.get());
            } else {
                model.addAttribute("error", "Proveedor no encontrado");
                return "redirect:/SuperAdmin/listaProveedores";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar proveedor para editar.");
            e.printStackTrace();
        }

        return "SuperAdmin/GestionProveedores/vendor-edit";
    }

    @GetMapping("/SuperAdmin/crearProveedor")
    public String crearProveedor(Model model) {
        Proveedor proveedor= new Proveedor();

        model.addAttribute("tiendas", tiendaRepository.findAll());
        model.addAttribute("proveedor", proveedor);

        return "SuperAdmin/GestionProveedores/vendor-create";
    }

    @GetMapping("/SuperAdmin/agregarTienda")
    public String agregarTienda() {

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


            if (proveedor.getId() == null) {
                proveedor.setBaneado(false);
                proveedorRepository.save(proveedor);
                attr.addFlashAttribute("msg", "Proveedor creado exitosamente");
            } else {
                proveedorRepository.save(proveedor);
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
    public String perfil() {

        return "SuperAdmin/perfilSuperAdmin";
    }
    @ModelAttribute
    public void addUsuarioToModel(HttpServletRequest request, Model model) {
        // Obtiene la URI o nombre de la vista actual
        String uri = request.getRequestURI();

        // Excluye la vista específica
        if (uri.contains("/SuperAdmin/crearAdminZonal") || uri.contains("/SuperAdmin/AdminZonal/guardar") || uri.contains("/SuperAdmin/UsuarioFinal/Actualizar")) { // Cambia "/vista-excluida" por la URI que quieres excluir
            return; // No ejecutar el código si la vista es la excluida
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName(); // Obtener el email del usuario autenticado
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            optUsuario.ifPresent(usuario -> model.addAttribute("usuario", usuario));
        }
    }

}
