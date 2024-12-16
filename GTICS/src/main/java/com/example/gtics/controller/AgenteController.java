package com.example.gtics.controller;

import com.example.gtics.dto.MontoTotalOrdenDto;
import com.example.gtics.dto.OrdenCarritoDto;
import com.example.gtics.dto.ProductosCarritoDto;
import com.example.gtics.dto.ProductosxOrden;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import com.example.gtics.service.ChatRoomService;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller

public class AgenteController {
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    DistritoRepository distritoRepository;
    @Autowired
    OrdenRepository ordenRepository;
    @Autowired
    ControlOrdenRepository controlOrdenRepository;
    @Autowired
    EstadoOrdenRepository estadoOrdenRepository;
    @Autowired
    private FotosProductoRepository fotosProductoRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private MessageRepository messageRepository;
    @ModelAttribute
    public void addUsuarioToModel(Model model ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName(); // Obtener el email del usuario autenticado
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            optUsuario.ifPresent(usuario -> model.addAttribute("usuario", usuario));
        }
    }

    @GetMapping({"/Agente/perfil"})
    public String pefil(Model model, HttpSession session, @ModelAttribute("product") Usuario usuario){
        Integer idAgente = (Integer) session.getAttribute("id");
        Optional<Usuario> OptAdminZonal =  usuarioRepository.findById(idAgente);
        List<Distrito> listaDistritos = distritoRepository.findAll();

        if(OptAdminZonal.isPresent()){
            model.addAttribute("listaDistritos", listaDistritos);

            model.addAttribute("usuarioLogueado",OptAdminZonal.get());
        }
        return "Agente/miperfil";
    }

    @PostMapping("/Agente/savePerfil")
    public String guardarPerfil(
            @RequestParam("id") String id,
            @RequestParam("distrito") String idDistrito, // Suponiendo que usas el ID del distrito
            @RequestParam("direccion") String direccion,
            @RequestParam("email") String email,@RequestParam(value  = "fotoPerfil",required = false) MultipartFile foto,
            RedirectAttributes attr) {

        // Actualiza el usuario
        usuarioRepository.actualizarUsuario(idDistrito, direccion, email, id);

        Optional<Usuario> uOpt = usuarioRepository.findById(Integer.parseInt(id));
        if(uOpt.isPresent()) {
            Usuario usuario = uOpt.get();
            if (foto != null && !foto.isEmpty()) {
                try {
                    usuario.setFoto(foto.getBytes());
                } catch (IOException e) {
                    attr.addFlashAttribute("error", "Error al procesar la foto de perfil.");
                    return "redirect:/Agente/perfil";
                }
            }


        }

        // Añade un mensaje de éxito
        attr.addFlashAttribute("mensaje", "Perfil actualizado con éxito.");

        // Redirige a la página de perfil
        return "redirect:/Agente/perfil";
    }
    @GetMapping({"Agente"})
    public String Inicio(Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            if (optUsuario.isPresent()) {
                Usuario usuario = optUsuario.get();

                Integer idAgente = usuario.getId();

                // Almacenar el idAgente en la sesión
                session.setAttribute("id", idAgente);

                List<OrdenCarritoDto> listaOrdenesSinAsignar = ordenRepository.ultimasOrdenesSinAsignar();
                List<OrdenCarritoDto> listaOrdenesPendientes = ordenRepository.ultimasOrdenesPendientes(idAgente);
                List<OrdenCarritoDto> listaOrdenesEnProceso = ordenRepository.ultimasOrdenesenProceso(idAgente);
                List<OrdenCarritoDto> listaOrdenesResueltas = ordenRepository.ultimasOrdenesResueltas(idAgente);

                model.addAttribute("listaOrdenesSinAsignar", listaOrdenesSinAsignar);
                model.addAttribute("listaOrdenesPendientes", listaOrdenesPendientes);
                model.addAttribute("listaOrdenesEnProceso", listaOrdenesEnProceso);
                model.addAttribute("listaOrdenesResueltas", listaOrdenesResueltas);
            }
        }

        return "Agente/inicio";
    }
    @GetMapping({"Agente/Chat"})
    public ModelAndView chatAgente(HttpSession session) {

        Integer idAgente = (Integer) session.getAttribute("id");
        if (idAgente == null) {
            // Si el idAgente no está en la sesión, redirigir o manejar el error
            return new ModelAndView("redirect:/login");
        }
        List<Usuario> usuariosAsignados = usuarioRepository.findUsuariosAsignadosAlAgenteNoPageable(idAgente);
        ArrayList<Integer> idUsuariosAsignados = new ArrayList<>();
        for(Usuario u : usuariosAsignados){
            idUsuariosAsignados.add(u.getId());
        }
        ModelAndView modelAndView = new ModelAndView("Agente/chatVistaAgente");

        List<String> allRooms = messageRepository.findRooms();
        List<String> activeRooms = new ArrayList<>();;
        for (String room : allRooms) {
            // Verificar que la sala siga el formato "room_<idUsuario>"
            if (room.startsWith("room_")) {
                try {
                    Integer roomId = Integer.parseInt(room.substring(5));
                    // Comprobar si el idUsuario está en la lista de usuarios asignados
                    if (idUsuariosAsignados.contains(roomId)) {
                        activeRooms.add(room);
                    }
                } catch (NumberFormatException e) {
                    // Si no es un número válido, ignoramos esta sala
                    continue;
                }
            }
        }
        //Obtener las activeRooms mediante services: Set<String> activeRooms = chatRoomService.getActiveRoomsAsignadas(idUsuariosAsignados);
        ArrayList<List<Message>> mensajesPorSala = new ArrayList<>();
        for(String room : activeRooms){
            //Obteniendo los mensajes enviados por el agente
            List<Message> mensajesSala = messageRepository.findBySalaOrderByFechaEnvioAsc(room);
            mensajesPorSala.add(mensajesSala);
        }
        modelAndView.addObject("activeRooms", activeRooms);
        for (String room: activeRooms){
            System.out.println(room);
        }
        modelAndView.addObject("MensajesPorSala", mensajesPorSala);
        modelAndView.addObject("idSender", idAgente);
        System.out.println(idAgente);
        return modelAndView;
    }

    @GetMapping({"Agente/UsuariosAsignados"})
    public String UsuariosAsignados(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String busqueda,
            HttpSession session) {

        int pageSize = 2;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Obtener el idAgente desde la sesión
        Integer idAgente = (Integer) session.getAttribute("id");

        if (idAgente == null) {
            // Redirigir al login si no hay idAgente en sesión
            return "redirect:/login";
        }

        // Llamar al repositorio con búsqueda y paginación
        Page<Usuario> usuariosAsignados = usuarioRepository.findUsuariosAsignados2(idAgente, busqueda, pageable);

        model.addAttribute("listaUsuariosAsignados", usuariosAsignados.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosAsignados.getTotalPages());
        model.addAttribute("busqueda", busqueda);

        return "Agente/UsuariosAsignados/usuariosAsignados";
    }



    @GetMapping("/Agente/banear")
    public String banearUsuario(@RequestParam("idUsuario") Integer idUsuario,
                                RedirectAttributes redirectAttributes,@RequestParam("razonBaneado") String razonBaneado) {

        Optional<Usuario> optUsuario = usuarioRepository.findById(idUsuario);

        if (optUsuario.isPresent()) {
            usuarioRepository.banUsuario(idUsuario,razonBaneado);
            redirectAttributes.addAttribute("usuarioBaneado", true);

            return "redirect:/Agente/UsuariosAsignados";

        }else {
            return "redirect:/Agente/UsuariosAsignados";
        }

    }

    @GetMapping("/Agente/verUsuarioFinal")
    public String verUsuarioFinal(Model model, @RequestParam Integer id) {
        Optional<Usuario> optUsuario = usuarioRepository.findById(id);

        if (optUsuario.isPresent()) {
            model.addAttribute("usuario", optUsuario.get());
            return "Agente/UsuariosAsignados/verUsuario";
        } else {
            return "redirect:/Agente/UsuariosAsignados";
        }
    }

    @GetMapping("/Agente/verPerfilDesdeDetalle")
    public String verPerfilDesdeDetalle(Model model, @RequestParam Integer id) {
        Optional<Usuario> optUsuario = usuarioRepository.findById(id);

        if (optUsuario.isPresent()) {
            model.addAttribute("usuario", optUsuario.get());
            return "Agente/OrdenesDeUsuario/verPerfilUsuario";
        } else {
            return "redirect:/Agente/Ordenes";
        }
    }

    @GetMapping({"Agente/Ordenes"})
    public String Ordenes(Model model,
                          @RequestParam(defaultValue = "0") int page,
                          HttpSession session) {

        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Obtener el idAgente desde la sesión
        Integer idAgente = (Integer) session.getAttribute("id");

        if (idAgente == null) {
            // Si el idAgente no está en la sesión, redirigir o manejar el error
            return "redirect:/login";
        }

        // Cargar los datos asociados al idAgente
        List<ControlOrden> listaControlOrden = controlOrdenRepository.findAll();
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        List<MontoTotalOrdenDto> listaMonto = ordenRepository.obtenerMontoTotalConDto(idAgente);
        Page<Orden> ordenesLista = ordenRepository.buscarMisOrdenesYOrdenesSinAsignarPage(idAgente, pageable);

        // Agregar los datos al modelo para la vista
        model.addAttribute("ordenesLista", ordenesLista.getContent());
        model.addAttribute("listaControlOrden", listaControlOrden);
        model.addAttribute("listaEstadoOrden", listaEstadoOrden);
        model.addAttribute("listaMonto", listaMonto);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordenesLista.getTotalPages());

        return "Agente/OrdenesDeUsuario/ordeneslista";
    }





    @RequestMapping(value = "Agente/OrdenesPost", method = {RequestMethod.GET, RequestMethod.POST})
    public String OrdenesFiltro(Model model,
                                @RequestParam(value = "idEstado", defaultValue = "0") Integer idEstado,
                                @RequestParam(value = "idControl", defaultValue = "0") Integer idControl,
                                @RequestParam(defaultValue = "0") int page,
                                HttpSession session) {

        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Obtener el idAgente desde la sesión
        Integer idAgente = (Integer) session.getAttribute("id");

        if (idAgente == null) {
            // Si el idAgente no está en la sesión, redirigir o manejar el error
            return "redirect:/login";
        }

        Page<Orden> ordenesLista;
        List<MontoTotalOrdenDto> listaMonto;

        if (idEstado == 0 && idControl == 0) {
            ordenesLista = ordenRepository.buscarMisOrdenesYOrdenesSinAsignarPage(idAgente, pageable);
            listaMonto = ordenRepository.obtenerMontoTotalMisOrdenesYOrdenesSinAsignar(idAgente);
        }
        else if (idEstado > 0 && idControl > 0) {
            ordenesLista = ordenRepository.findOrdenesByEstadoAndControl(idEstado, idControl,idAgente, pageable);
            listaMonto = ordenRepository.obtenerMontoTotalDeOrdenesByEstadoAndControl(idEstado, idControl,idAgente);
        }
        else if (idEstado > 0 && idControl == 0 ) {
            ordenesLista = ordenRepository.findOrdenesByEstado(idEstado,idAgente, pageable);
            listaMonto = ordenRepository.obtenerMontoTotalDeOrdenesByEstado(idEstado,idAgente);
        }
        else if (idControl > 0&& idEstado == 0) {
            if(idControl==1){
                ordenesLista = ordenRepository.findOrdenesSinAsignar(idControl, pageable);
                listaMonto = ordenRepository.obtenerMontoTotalOrdenesSinAsignar(idAgente);
            }else{
                ordenesLista = ordenRepository.findOrdenesByControl(idControl,idAgente, pageable);
                listaMonto = ordenRepository.obtenerMontoTotalDeOrdenesByControl(idControl,idAgente);
            }

        } else {
            ordenesLista = ordenRepository.buscarMisOrdenesYOrdenesSinAsignarPage(idAgente, pageable);
            listaMonto = ordenRepository.obtenerMontoTotalMisOrdenesYOrdenesSinAsignar(idAgente);
        }

        List<ControlOrden> listaControlOrden = controlOrdenRepository.findAll();
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        model.addAttribute("ordenesLista", ordenesLista.getContent());
        model.addAttribute("listaControlOrden",listaControlOrden);
        model.addAttribute("listaEstadoOrden",listaEstadoOrden);
        model.addAttribute("idEstado",idEstado);
        model.addAttribute("idControl",idControl);
        model.addAttribute("listaMonto",listaMonto);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordenesLista.getTotalPages());

        return "Agente/OrdenesDeUsuario/ordenesListaPost";
    }


    @GetMapping({"/Agente/Ordenes/AsignarOrden"})
    public String AutoAsignarOrden(Model model,
                                   @RequestParam("idOrden") Integer idOrden,
                                   RedirectAttributes attr,
                                   HttpSession session){
        System.out.println("prueba owowowo");
        // Obtener el idAgente desde la sesión
        Integer idAgente = (Integer) session.getAttribute("id");
        Integer idDuenoOrden = ordenRepository.obtenerUsuarioDuenoDeOrden(idOrden);
        if (idAgente == null) {
            // Si el idAgente no está en la sesión, redirigir o manejar el error
            return "redirect:/login";
        }

        try{
            ordenRepository.autoAsignarOrden(idAgente,idOrden);
            ordenRepository.solicitarUnAgenteParaUsuario(idAgente,idDuenoOrden );
            attr.addAttribute("autoasignacionExito",true);
        }catch (Exception e){
            attr.addAttribute("autoasignacionError",true);
        }


        return "redirect:/Agente/Ordenes";
    }

    @GetMapping({"Agente/Ordenes/Usuario"})
    public String OrdenesUsuario(@RequestParam("idUsuario") Integer idUsuario,
                                 Model model,
                                 @RequestParam(defaultValue = "0") int page){
        if (idUsuario == null) {
            throw new IllegalArgumentException("El parámetro idUsuario no puede ser null");
        }
        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoConDto(idUsuario, pageable);
        Optional<Usuario> usr = usuarioRepository.findById(idUsuario);

        if (usr.isEmpty()) {
            throw new EntityNotFoundException("No se encontró el usuario con id " + idUsuario);
        }

        model.addAttribute("ordenCarrito", ordenCarrito.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordenCarrito.getTotalPages());
        model.addAttribute("usuario", usr.get());
        return "Agente/OrdenesDeUsuario/ordenesDeUsuario";
    }




    @PostMapping({"/Agente/Orden/editarOrden"})
    public String editarOrden(Orden orden,RedirectAttributes attr){

        System.out.println( "nueva fecha: " + orden.getFechaOrden());
        System.out.println("nuevo distrito: " + orden.getIdCarritoCompra().getIdUsuario().getDistrito().getNombre());
        System.out.println( "nueva direccion: " + orden.getIdCarritoCompra().getIdUsuario().getDireccion());

        /*
        orden.setSolicitarCancelarOrden(0);
        orden.setOrdenEliminada(0);

         */
        try {
            ordenRepository.save(orden);
            attr.addAttribute("msg", "La orden se ha actualizado exitosamente!");
            return "redirect:/Agente/Ordenes";

        }catch (Exception e){
            attr.addAttribute("error", "Ha ocurrido un error al editar la orden.");
            return "redirect:/Agente/Ordenes";
        }

    }

    @GetMapping({"Agente/Ordenes/DetallesOrdenSinAsignar"})
    public String detalleOrdenSinAsignar(@RequestParam("idOrden")Integer idOrden,
                                         @RequestParam("numOrden") Integer numOrden,
                                         Model model,
                                         @RequestParam("indicadorVistaARegresar") Integer indicadorVistaARegresar){

        List<Distrito> listaDistritos   = distritoRepository.findAll();
        List<ControlOrden> listaControlOrden = controlOrdenRepository.findAll();
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(idOrden);
        Double costoAdicional = ordenRepository.obtenerCostoAdicionalxOrden(idOrden);

        // Calcular el subtotal sumando precioTotalPorProducto
        double subtotal = productosOrden.stream()
                .mapToDouble(ProductosxOrden::getPrecioTotalPorProducto)
                .sum();

        // Encontrar el costo de envío más alto
        double maxCostoEnvio = productosOrden.stream()
                .mapToDouble(ProductosxOrden::getCostoEnvio)
                .max()
                .orElse(0.0);

        if (ordenOpt.isPresent()) {
            model.addAttribute("orden", ordenOpt.get());
            model.addAttribute("listaDistritos", listaDistritos);
            model.addAttribute("numOrden", numOrden);
            model.addAttribute("listaControlOrden", listaControlOrden);
            model.addAttribute("listaEstadoOrden", listaEstadoOrden);
            model.addAttribute("indicadorVistaARegresar", indicadorVistaARegresar);
            model.addAttribute("productosOrden", productosOrden);
            model.addAttribute("subtotal", subtotal);  // Enviar subtotal al modelo
            model.addAttribute("maxCostoEnvio", maxCostoEnvio);  // Enviar costo de envío más alto
            model.addAttribute("costoAdicional", costoAdicional);  // Enviar costo adicional

            return "Agente/OrdenesDeUsuario/detalleOrdenSinAsignar";
        } else {
            return "redirect:/Agente/Ordenes";
        }


    }

    @GetMapping({"Agente/Ordenes/Detalles"})
    public String DetalleOrden(@RequestParam("idOrden") Integer idOrden,
                               @RequestParam("numOrden") Integer numOrden,
                               Model model,
                               @RequestParam("indicadorVistaARegresar") Integer indicadorVistaARegresar){

        List<Distrito> listaDistritos   = distritoRepository.findAll();
        List<ControlOrden> listaControlOrden = controlOrdenRepository.findAll();
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(idOrden);
        Double costoAdicioal = ordenRepository.obtenerCostoAdicionalxOrden(idOrden);
        // Calcular el subtotal sumando precioTotalPorProducto
        double subtotal = productosOrden.stream()
                .mapToDouble(ProductosxOrden::getPrecioTotalPorProducto)
                .sum();
        // Encontrar el costo de envío más alto
        double maxCostoEnvio = productosOrden.stream()
                .mapToDouble(ProductosxOrden::getCostoEnvio)
                .max()
                .orElse(0.0);

        if(ordenOpt.isPresent()){
            model.addAttribute("orden",ordenOpt.get());
            model.addAttribute("listaDistritos",listaDistritos);
            model.addAttribute("numOrden",numOrden);
            model.addAttribute("listaControlOrden",listaControlOrden);
            model.addAttribute("listaEstadoOrden",listaEstadoOrden);
            model.addAttribute("indicadorVistaARegresar",indicadorVistaARegresar);
            model.addAttribute("productosOrden", productosOrden);
            model.addAttribute("subtotal", subtotal);  // Enviar subtotal al modelo
            model.addAttribute("maxCostoEnvio", maxCostoEnvio);  // Enviar costo de envío más alto
            model.addAttribute("costoAdicional", costoAdicioal);  // Enviar costo de envío más alto


            return "Agente/OrdenesDeUsuario/detalleOrden";

        }else{
            return "redirect:/Agente/Ordenes";
        }




    }
    @GetMapping("/productos/{id}")
    public ResponseEntity<ByteArrayResource> obtenerFotoProducto(@PathVariable Integer id) {
        List<Fotosproducto> fopt = fotosProductoRepository.findByProducto_Id(id);
        Fotosproducto fotosproducto =fopt.get(0);//.orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        byte[] foto = fotosproducto.getFoto();
        ByteArrayResource resource = new ByteArrayResource(foto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"foto_producto_" + id + ".jpg\"")
                .contentLength(foto.length)
                .body(resource);
    }

    @GetMapping({"Agente/Ordenes/tracking"})
    public String trackingUsuario(@RequestParam("idOrden") Integer idOrden,Model model){

        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        if(ordenOpt.isPresent()){
            model.addAttribute("orden",ordenOpt.get());
        }

        return "Agente/OrdenesDeUsuario/trackingOrdenUsuario";
    }



    @GetMapping({"Agente/Ordenes/EliminarOrden"})
    public String eliminadoLogicoDeOrden(RedirectAttributes attr,@RequestParam("idOrden") Integer idOrden,@RequestParam("razonEliminacion") String razonEliminacion  ){
        System.out.println(idOrden);
        System.out.println(razonEliminacion);
        try{
            ordenRepository.eliminadoLogicoDeOrden(idOrden,razonEliminacion);
            attr.addAttribute("ordenEliminadaExitosamente",true);

        }catch (Exception e){
            attr.addAttribute("ordenEliminadaEstadoNoValido",true);

        }
        return "redirect:/Agente/Ordenes";
    }


    @GetMapping({"Agente/Ordenes/Descargar"})
    public String DescargarOrden(){

        return "Agente/OrdenesDeUsuario/DescargarOrdenesPorUsuario";
    }


    @GetMapping({"Agente/Reportes/Descargar"})
    public String DescargarReporte(Model model){
        // Obtén los usuarios con id_rol = 4 usando el método en UsuarioRepository
        List<Usuario> usuariosRol4 = usuarioRepository.findUsuariosByRol4();

        // Pasa la lista al modelo para ser usada en el HTML
        model.addAttribute("usuariosRol4", usuariosRol4);
        return "Agente/Reportes/descargarReporte";
    }
    @GetMapping("/Agente/ordenes/porUsuario/{usuarioId}")
    @ResponseBody
    public List<OrdenCarritoDto> obtenerOrdenesPorUsuario(@PathVariable Integer usuarioId, Pageable pageable) {
        // Utiliza el método en OrdenRepository para obtener las órdenes del usuario
        return ordenRepository.obtenerCarritoConDto(usuarioId, pageable).getContent();
    }
    @GetMapping("/descargarOrdenPDF")
    public void descargarOrdenPDF(@RequestParam("idOrden") Integer idOrden, HttpServletResponse response) throws IOException, DocumentException {
        try {
            byte[] pdfBytes = generarOrdenPDF(idOrden);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Orden_" + idOrden + ".pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }



    @GetMapping("/descargarOrdenExcel")
    public void descargarOrdenExcel(@RequestParam("idOrden") Integer idOrden, HttpServletResponse response) throws IOException {
        try {
            byte[] excelBytes = generarOrdenExcel(idOrden);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=Orden_" + idOrden + ".xlsx");
            response.getOutputStream().write(excelBytes);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }






    @GetMapping("/descargarOrdenCSV")
    public void descargarOrdenCSV(@RequestParam("idOrden") Integer idOrden, HttpServletResponse response) throws IOException {
        try {
            byte[] csvBytes = generarOrdenCSV(idOrden);
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=Orden_" + idOrden + ".csv");
            response.getOutputStream().write(csvBytes);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }


    @GetMapping("/descargarOrdenZIP")
    public void descargarOrdenZIP(@RequestParam("idOrden") Integer idOrden,
                                  @RequestParam("formatos") String formatos,
                                  HttpServletResponse response) throws IOException, DocumentException {
        System.out.println("prueba error pdf aaaaaa");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            String[] formatosArray = formatos.split(",");
            for (String formato : formatosArray) {
                formato = formato.trim();
                byte[] fileBytes = null;
                String fileName = "Orden_" + idOrden;

                if (formato.equalsIgnoreCase("PDF")) {
                    fileBytes = generarOrdenPDF(idOrden);
                    fileName += ".pdf";
                } else if (formato.equalsIgnoreCase("Excel")) {
                    fileBytes = generarOrdenExcel(idOrden);
                    fileName += ".xlsx";
                } else if (formato.equalsIgnoreCase("CSV")) {
                    fileBytes = generarOrdenCSV(idOrden);
                    fileName += ".csv";
                }

                if (fileBytes != null) {
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    zos.write(fileBytes);
                    zos.closeEntry();
                }
            }

            zos.close();

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=Orden_" + idOrden + ".zip");
            response.getOutputStream().write(baos.toByteArray());
        } catch (IOException | DocumentException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el archivo ZIP: " + e.getMessage());
        }
    }



    private byte[] generarOrdenPDF(Integer idOrden) throws IOException, DocumentException {
        // Buscar la orden
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        if (!ordenOpt.isPresent()) {
            throw new IOException("Orden no encontrada");
        }

        Orden orden = ordenOpt.get();
        List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(idOrden);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 90, 55);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        // Agregar encabezado y pie de página
        writer.setPageEvent(new HeaderFooterPageEvent());

        try {
            document.open();

            // Cargar el logo
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream logoStream = classLoader.getResourceAsStream("static/images/logo/logoGTICSv2.jpeg");
            if (logoStream == null) {
                throw new IOException("No se pudo cargar el logo.");
            }
            Image logo = Image.getInstance(IOUtils.toByteArray(logoStream));
            logo.scaleToFit(100, 50);
            logo.setAlignment(Image.ALIGN_LEFT);
            document.add(logo);

            // Título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
            Paragraph title = new Paragraph("Detalle de la Orden #" + idOrden, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Información de la orden
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.DARK_GRAY);
            Paragraph info = new Paragraph(
                    "Fecha de emisión: " + orden.getFechaOrden() + "\n" +
                            "Cliente: " + orden.getIdCarritoCompra().getIdUsuario().getNombre() + "\n" +
                            "Dirección: " + orden.getIdCarritoCompra().getIdUsuario().getDireccion(),
                    infoFont);
            info.setSpacingAfter(20);
            document.add(info);

            // Tabla de productos
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 4, 1, 2, 2});

            // Estilos para la tabla
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
            BaseColor headerColor = new BaseColor(0, 121, 107); // Verde oscuro
            BaseColor evenRowColor = new BaseColor(224, 242, 241); // Verde claro
            BaseColor oddRowColor = BaseColor.WHITE;

            // Encabezados de la tabla
            String[] headers = {"N°", "Descripción", "Cant.", "Precio Unit.", "Total"};
            for (String header : headers) {
                agregarCelda(table, header, headerFont, headerColor, Element.ALIGN_CENTER, true);
            }

            // Contenido de la tabla
            double subtotal = 0;
            int itemNumber = 1;
            boolean isEvenRow = true;
            DecimalFormat df = new DecimalFormat("0.00");
            for (ProductosxOrden producto : productosOrden) {
                BaseColor rowColor = isEvenRow ? evenRowColor : oddRowColor;

                agregarCelda(table, String.valueOf(itemNumber++), cellFont, rowColor, Element.ALIGN_CENTER, false);
                agregarCelda(table, producto.getNombreProducto(), cellFont, rowColor, Element.ALIGN_LEFT, false);
                agregarCelda(table, String.valueOf(producto.getCantidadProducto()), cellFont, rowColor, Element.ALIGN_CENTER, false);
                agregarCelda(table, "S/ " + df.format(producto.getPrecioUnidad()), cellFont, rowColor, Element.ALIGN_RIGHT, false);

                double totalProducto = producto.getPrecioTotalPorProducto();
                subtotal += totalProducto;
                agregarCelda(table, "S/ " + df.format(totalProducto), cellFont, rowColor, Element.ALIGN_RIGHT, false);

                isEvenRow = !isEvenRow;
            }

            // Fila de subtotal
            agregarCelda(table, "", cellFont, BaseColor.WHITE, Element.ALIGN_LEFT, false, 3);
            agregarCelda(table, "Subtotal", headerFont, headerColor, Element.ALIGN_RIGHT, true);
            agregarCelda(table, "S/ " + df.format(subtotal), cellFont, BaseColor.WHITE, Element.ALIGN_RIGHT, false);

            document.add(table);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    // Método auxiliar para agregar celdas a la tabla
    private void agregarCelda(PdfPTable table, String texto, Font font, BaseColor backgroundColor, int alignment, boolean bold, int colSpan) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        cell.setColspan(colSpan);
        table.addCell(cell);
    }

    // Sobrecarga para usar valores por defecto
    private void agregarCelda(PdfPTable table, String texto, Font font, BaseColor backgroundColor, int alignment, boolean bold) {
        agregarCelda(table, texto, font, backgroundColor, alignment, bold, 1);
    }


    // Clase para agregar encabezado y pie de página
    class HeaderFooterPageEvent extends PdfPageEventHelper {
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            // Pie de página
            PdfPTable footer = new PdfPTable(2);
            try {
                footer.setWidths(new int[]{24, 24});
                footer.setTotalWidth(527);
                footer.setLockedWidth(true);
                footer.getDefaultCell().setFixedHeight(20);
                footer.getDefaultCell().setBorder(Rectangle.TOP);
                footer.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

                footer.addCell(new Phrase("Reporte generado por ExpressDeals", footerFont));
                footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                footer.addCell(new Phrase(String.format("Página %d", writer.getPageNumber()), footerFont));

                footer.writeSelectedRows(0, -1, 34, 50, writer.getDirectContent());
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }
    }

    private byte[] generarOrdenExcel(Integer idOrden) throws IOException {
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        if (ordenOpt.isPresent()) {
            Orden orden = ordenOpt.get();
            List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(idOrden);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Orden");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            // Usar la clase Font de Apache POI
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("S/ #,##0.00"));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Encabezados
            String[] headers = {"N°", "Descripción", "Cant.", "Precio Unitario (S/)", "Total (S/)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Contenido
            int rowIdx = 1;
            double subtotal = 0;
            int itemNumber = 1;
            for (ProductosxOrden producto : productosOrden) {
                Row row = sheet.createRow(rowIdx++);

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(itemNumber++);
                cell0.setCellStyle(cellStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(producto.getNombreProducto());
                cell1.setCellStyle(cellStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(producto.getCantidadProducto());
                cell2.setCellStyle(cellStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(producto.getPrecioUnidad());
                cell3.setCellStyle(currencyStyle);

                double totalProducto = producto.getPrecioTotalPorProducto();
                subtotal += totalProducto;
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(totalProducto);
                cell4.setCellStyle(currencyStyle);
            }

            // Fila de subtotal
            Row totalRow = sheet.createRow(rowIdx);
            Cell cellLabel = totalRow.createCell(3);
            cellLabel.setCellValue("Total a Pagar");
            cellLabel.setCellStyle(headerStyle);

            Cell cellTotal = totalRow.createCell(4);
            cellTotal.setCellValue(subtotal);
            cellTotal.setCellStyle(currencyStyle);

            // Autoajustar anchos de columna
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Aplicar filtros
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.length - 1));

            workbook.write(baos);
            workbook.close();

            return baos.toByteArray();
        } else {
            throw new IOException("Orden no encontrada");
        }
    }

    @GetMapping(value = "/previsualizarReporte", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String previsualizarReporte(@RequestParam("idOrden") Integer idOrden) {
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        if (ordenOpt.isPresent()) {
            Orden orden = ordenOpt.get();
            List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(idOrden);

            // Generar el HTML del reporte
            StringBuilder htmlContent = new StringBuilder();

            // Construir el contenido HTML sin estilos en línea
            htmlContent.append("<div class='reporte-content'>");
            htmlContent.append("<h3 class='reporte-header'>Detalle de la Orden #").append(idOrden).append("</h3>");
            htmlContent.append("<p><strong>Fecha de emisión:</strong> ").append(orden.getFechaOrden()).append("</p>");
            htmlContent.append("<p><strong>Cliente:</strong> ").append(orden.getIdCarritoCompra().getIdUsuario().getNombre()).append("</p>");
            htmlContent.append("<p><strong>Dirección:</strong> ").append(orden.getIdCarritoCompra().getIdUsuario().getDireccion()).append("</p>");

            htmlContent.append("<div class='table-responsive'>");
            htmlContent.append("<table class='table table-striped table-bordered'><thead><tr>")
                    .append("<th scope='col'>N°</th>")
                    .append("<th scope='col'>Descripción</th>")
                    .append("<th scope='col'>Cant.</th>")
                    .append("<th scope='col'>Precio Unitario (S/)</th>")
                    .append("<th scope='col'>Total (S/)</th>")
                    .append("</tr></thead><tbody>");

            int itemNumber = 1;
            double subtotal = 0;
            DecimalFormat df = new DecimalFormat("0.00");
            for (ProductosxOrden producto : productosOrden) {
                double totalProducto = producto.getPrecioTotalPorProducto();
                subtotal += totalProducto;

                htmlContent.append("<tr>")
                        .append("<th scope='row'>").append(itemNumber++).append("</th>")
                        .append("<td>").append(producto.getNombreProducto()).append("</td>")
                        .append("<td>").append(producto.getCantidadProducto()).append("</td>")
                        .append("<td>").append("S/ ").append(df.format(producto.getPrecioUnidad())).append("</td>")
                        .append("<td>").append("S/ ").append(df.format(totalProducto)).append("</td>")
                        .append("</tr>");
            }

            htmlContent.append("</tbody></table>");
            htmlContent.append("</div>"); // Cierre de table-responsive

            htmlContent.append("<p class='reporte-total'><strong>Total a Pagar: S/ ").append(df.format(subtotal)).append("</strong></p>");
            htmlContent.append("</div>");

            return htmlContent.toString();
        } else {
            return "<p>Error: Orden no encontrada.</p>";
        }
    }




    private byte[] generarOrdenCSV(Integer idOrden) throws IOException {
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        if (ordenOpt.isPresent()) {
            Orden orden = ordenOpt.get();
            List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(idOrden);

            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Orden #").append(idOrden).append("\n");
            csvContent.append("Fecha de emisión:;").append(orden.getFechaOrden()).append("\n");
            csvContent.append("Cliente:;").append(orden.getIdCarritoCompra().getIdUsuario().getNombre()).append("\n");
            csvContent.append("Dirección:;").append(orden.getIdCarritoCompra().getIdUsuario().getDireccion()).append("\n\n");

            csvContent.append("N°;Descripción;Cant.;Precio Unitario (S/);Total (S/)\n");

            double subtotal = 0;
            int itemNumber = 1;
            DecimalFormat df = new DecimalFormat("0.00");
            for (ProductosxOrden producto : productosOrden) {
                double totalProducto = producto.getPrecioTotalPorProducto();
                subtotal += totalProducto;

                csvContent.append(itemNumber++).append(";")
                        .append(producto.getNombreProducto()).append(";")
                        .append(producto.getCantidadProducto()).append(";")
                        .append(df.format(producto.getPrecioUnidad())).append(";")
                        .append(df.format(totalProducto)).append("\n");
            }

            csvContent.append(";;;Total a Pagar;").append(df.format(subtotal)).append("\n");

            return csvContent.toString().getBytes(StandardCharsets.UTF_8);
        } else {
            throw new IOException("Orden no encontrada");
        }
    }

    @GetMapping("/descargarReporteTotalOrdenesZIP")
    public void descargarReporteTotalOrdenesZIP(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam("formatos") String formatos,
            HttpServletResponse response) throws IOException, DocumentException {
        System.out.println("prueba");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            // Ajustar fechaFin si es necesario
            // Si el método es inclusivo, no es necesario sumar un día
            // LocalDate fechaFinInclusive = fechaFin;
            // Si es exclusivo, sumar un día
            LocalDate fechaFinInclusive = fechaFin.plusDays(1);

            // Obtener las órdenes en el rango de fechas ajustado
            List<Orden> ordenes = ordenRepository.findOrdenesByFechaOrdenBetween(fechaInicio, fechaFinInclusive);

            if (ordenes.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron órdenes en el rango de fechas seleccionado.");
                return;
            }

            String[] formatosArray = formatos.split(",");
            for (String formato : formatosArray) {
                formato = formato.trim();
                byte[] fileBytes = null;
                String fileName = "Reporte_Total_Ordenes_" + fechaInicio + "_al_" + fechaFin;

                if (formato.equalsIgnoreCase("PDF")) {
                    fileBytes = generarReporteTotalOrdenesPDF(ordenes, fechaInicio, fechaFin);
                    fileName += ".pdf";
                } else if (formato.equalsIgnoreCase("Excel")) {
                    fileBytes = generarReporteTotalOrdenesExcel(ordenes, fechaInicio, fechaFin);
                    fileName += ".xlsx";
                } else if (formato.equalsIgnoreCase("CSV")) {
                    fileBytes = generarReporteTotalOrdenesCSV(ordenes, fechaInicio, fechaFin);
                    fileName += ".csv";
                }

                if (fileBytes != null) {
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    zos.write(fileBytes);
                    zos.closeEntry();
                }
            }

            zos.close();

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=Reporte_Total_Ordenes.zip");
            response.getOutputStream().write(baos.toByteArray());
        } catch (IOException | DocumentException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el archivo ZIP: " + e.getMessage());
        }
    }
    @GetMapping("/descargarReporteTotalOrdenesPDF")
    public void descargarReporteTotalOrdenesPDF(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletResponse response) throws IOException, DocumentException {

        try {
            // Ajustar fechaFin si es necesario
            LocalDate fechaFinInclusive = fechaFin.plusDays(1);

            // Obtener las órdenes en el rango de fechas ajustado
            List<Orden> ordenes = ordenRepository.findOrdenesByFechaOrdenBetween(fechaInicio, fechaFinInclusive);

            if (ordenes.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron órdenes en el rango de fechas seleccionado.");
                return;
            }

            // Generar el reporte en PDF
            byte[] pdfBytes = generarReporteTotalOrdenesPDF(ordenes, fechaInicio, fechaFin);

            // Configurar la respuesta para enviar el PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Reporte_Total_Ordenes_" + fechaInicio + "_al_" + fechaFin + ".pdf");
            response.getOutputStream().write(pdfBytes);

        } catch (IOException | DocumentException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el reporte PDF: " + e.getMessage());
        }
    }

    @GetMapping("/descargarReporteTotalOrdenesCSV")
    public void descargarReporteTotalOrdenesCSV(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletResponse response) throws IOException {

        try {
            // Ajustar fechaFin si es necesario
            LocalDate fechaFinInclusive = fechaFin.plusDays(1);

            // Obtener las órdenes en el rango de fechas ajustado
            List<Orden> ordenes = ordenRepository.findOrdenesByFechaOrdenBetween(fechaInicio, fechaFinInclusive);

            if (ordenes.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron órdenes en el rango de fechas seleccionado.");
                return;
            }

            // Generar el reporte en CSV
            byte[] csvBytes = generarReporteTotalOrdenesCSV(ordenes, fechaInicio, fechaFin);

            // Configurar la respuesta para enviar el archivo CSV
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=Reporte_Total_Ordenes_" + fechaInicio + "_al_" + fechaFin + ".csv");
            response.getOutputStream().write(csvBytes);

        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el reporte CSV: " + e.getMessage());
        }
    }

    @GetMapping("/descargarReporteTotalOrdenesExcel")
    public void descargarReporteTotalOrdenesExcel(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletResponse response) throws IOException {

        try {
            // Ajustar fechaFin si es necesario
            LocalDate fechaFinInclusive = fechaFin.plusDays(1);

            // Obtener las órdenes en el rango de fechas ajustado
            List<Orden> ordenes = ordenRepository.findOrdenesByFechaOrdenBetween(fechaInicio, fechaFinInclusive);

            if (ordenes.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron órdenes en el rango de fechas seleccionado.");
                return;
            }

            // Generar el reporte en Excel (XLSX)
            byte[] excelBytes = generarReporteTotalOrdenesExcel(ordenes, fechaInicio, fechaFin);

            // Configurar la respuesta para enviar el archivo Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=Reporte_Total_Ordenes_" + fechaInicio + "_al_" + fechaFin + ".xlsx");
            response.getOutputStream().write(excelBytes);

        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el reporte Excel: " + e.getMessage());
        }
    }







    // Agrega este método en tu AgenteController
    private byte[] generarReporteTotalOrdenesPDF(List<Orden> ordenes, LocalDate fechaInicio, LocalDate fechaFin) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 90, 55);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();

        // Título
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
        Paragraph title = new Paragraph("Reporte Total de Órdenes", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Información general
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.DARK_GRAY);
        Paragraph info = new Paragraph(
                "Total de órdenes desde: " + fechaInicio + " hasta: " + fechaFin,
                infoFont);
        info.setSpacingAfter(20);
        document.add(info);

        // Tabla
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2, 2});

        // Estilos
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        BaseColor headerColor = new BaseColor(0, 121, 107); // Verde oscuro

        // Encabezados
        String[] headers = {"N°", "Fecha", "Cliente", "Monto Total", "Estado"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Contenido
        int itemNumber = 1;
        DecimalFormat df = new DecimalFormat("0.00");
        for (Orden orden : ordenes) {
            // Obtener el monto total de la orden
            double montoTotal = calcularMontoTotalOrden(orden);

            // Agregar filas
            PdfPCell cell1 = new PdfPCell(new Phrase(String.valueOf(itemNumber++), cellFont));
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setPadding(5);
            table.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(new Phrase(orden.getFechaOrden().toString(), cellFont));
            cell2.setPadding(5);
            table.addCell(cell2);

            PdfPCell cell3 = new PdfPCell(new Phrase(orden.getIdCarritoCompra().getIdUsuario().getNombre(), cellFont));
            cell3.setPadding(5);
            table.addCell(cell3);

            PdfPCell cell4 = new PdfPCell(new Phrase("S/ " + df.format(montoTotal), cellFont));
            cell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell4.setPadding(5);
            table.addCell(cell4);

            PdfPCell cell5 = new PdfPCell(new Phrase(orden.getEstadoorden().getNombreEstado(), cellFont));
            cell5.setPadding(5);
            table.addCell(cell5);
        }

        document.add(table);
        document.close();

        return baos.toByteArray();
    }


    private byte[] generarReporteTotalOrdenesExcel(List<Orden> ordenes, LocalDate fechaInicio, LocalDate fechaFin) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte Total de Órdenes");

        int rowIdx = 0;

        // Información general
        Row infoRow = sheet.createRow(rowIdx++);
        infoRow.createCell(0).setCellValue("Total de órdenes desde: " + fechaInicio + " hasta: " + fechaFin);

        // Saltar una fila
        rowIdx++;

        // Encabezados
        String[] headers = {"N°", "Fecha", "Cliente", "Monto Total (S/)", "Estado"};
        Row headerRow = sheet.createRow(rowIdx++);

        // Estilos
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);

        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("S/ #,##0.00"));
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

        // Agregar los encabezados a la hoja
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Contenido de las órdenes
        int itemNumber = 1;
        DecimalFormat df = new DecimalFormat("0.00");
        for (Orden orden : ordenes) {
            Row row = sheet.createRow(rowIdx++);

            double montoTotal = calcularMontoTotalOrden(orden);

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(itemNumber++);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(orden.getFechaOrden().toString());

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(orden.getIdCarritoCompra().getIdUsuario().getNombre());

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(montoTotal);
            cell3.setCellStyle(currencyStyle);

            Cell cell4 = row.createCell(4);
            cell4.setCellValue(orden.getEstadoorden().getNombreEstado());
        }

        // Autoajustar anchos de columna
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    private byte[] generarReporteTotalOrdenesCSV(List<Orden> ordenes, LocalDate fechaInicio, LocalDate fechaFin) throws IOException {
        StringBuilder csvContent = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.00");

        // Información general
        csvContent.append("Total de órdenes desde: ").append(fechaInicio).append(" hasta: ").append(fechaFin).append("\n\n");

        // Encabezados
        csvContent.append("N°;Fecha;Cliente;Monto Total (S/);Estado\n");

        int itemNumber = 1;
        for (Orden orden : ordenes) {
            double montoTotal = calcularMontoTotalOrden(orden);

            csvContent.append(itemNumber++).append(";")
                    .append(orden.getFechaOrden()).append(";")
                    .append(orden.getIdCarritoCompra().getIdUsuario().getNombre()).append(";")
                    .append(df.format(montoTotal)).append(";")
                    .append(orden.getEstadoorden().getNombreEstado()).append("\n");
        }

        return csvContent.toString().getBytes(StandardCharsets.UTF_8);
    }
    @GetMapping(value = "/previsualizarReporteTotalOrdenes", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String previsualizarReporteTotalOrdenes(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        // Ajustar fechaFin si es necesario
        LocalDate fechaFinInclusive = fechaFin.plusDays(1);

        // Obtener las órdenes en el rango de fechas ajustado
        List<Orden> ordenes = ordenRepository.findOrdenesByFechaOrdenBetween(fechaInicio, fechaFinInclusive);

        if (ordenes.isEmpty()) {
            return "<p>No se encontraron órdenes en el rango de fechas seleccionado.</p>";
        }

        StringBuilder htmlContent = new StringBuilder();

        // Construir el contenido HTML
        htmlContent.append("<div class='reporte-content'>");
        htmlContent.append("<h3 class='reporte-header'>Reporte Total de Órdenes</h3>");
        htmlContent.append("<p><strong>Desde:</strong> ").append(fechaInicio).append("</p>");
        htmlContent.append("<p><strong>Hasta:</strong> ").append(fechaFin).append("</p>");

        htmlContent.append("<div class='table-responsive'>");
        htmlContent.append("<table class='table table-striped table-bordered'><thead><tr>")
                .append("<th scope='col'>N°</th>")
                .append("<th scope='col'>Fecha</th>")
                .append("<th scope='col'>Cliente</th>")
                .append("<th scope='col'>Monto Total (S/)</th>")
                .append("<th scope='col'>Estado</th>")
                .append("</tr></thead><tbody>");

        int itemNumber = 1;
        DecimalFormat df = new DecimalFormat("0.00");
        for (Orden orden : ordenes) {
            double montoTotal = calcularMontoTotalOrden(orden);

            htmlContent.append("<tr>")
                    .append("<th scope='row'>").append(itemNumber++).append("</th>")
                    .append("<td>").append(orden.getFechaOrden()).append("</td>")
                    .append("<td>").append(orden.getIdCarritoCompra().getIdUsuario().getNombre()).append("</td>")
                    .append("<td>").append("S/ ").append(df.format(montoTotal)).append("</td>")
                    .append("<td>").append(orden.getEstadoorden().getNombreEstado()).append("</td>")
                    .append("</tr>");
        }

        htmlContent.append("</tbody></table>");
        htmlContent.append("</div>"); // Cierre de table-responsive
        htmlContent.append("</div>");

        return htmlContent.toString();
    }


    private double calcularMontoTotalOrden(Orden orden) {
        List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(orden.getId());
        double subtotal = productosOrden.stream()
                .mapToDouble(ProductosxOrden::getPrecioTotalPorProducto)
                .sum();
        double maxCostoEnvio = productosOrden.stream()
                .mapToDouble(ProductosxOrden::getCostoEnvio)
                .max()
                .orElse(0.0);
        double costosAdicionales = orden.getCostosAdicionales() != null ? orden.getCostosAdicionales() : 0.0;
        return subtotal + maxCostoEnvio + costosAdicionales;
    }



    @GetMapping("/descargarReporteOrdenesPorZonaZIP")
    public void descargarReporteOrdenesPorZonaZIP(
            @RequestParam("formatos") String formatos,
            HttpSession session,
            HttpServletResponse response) throws IOException, DocumentException {

        // Obtener idAgente de la sesión
        Integer idAgente = (Integer) session.getAttribute("id");

        if (idAgente == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario no autenticado");
            return;
        }

        // Obtener el agente actual
        Optional<Usuario> optAgente = usuarioRepository.findById(idAgente);
        if (!optAgente.isPresent()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Agente no encontrado");
            return;
        }
        Usuario agenteActual = optAgente.get();

        // Obtener idZona del agente actual
        Integer idZona = agenteActual.getDistrito().getZona().getId();

        // Obtener todos los agentes con idRol=3 en esa zona
        Integer idRol = 3;
        List<Usuario> agentesEnZona = usuarioRepository.findByDistrito_Zona_IdAndRol_Id(idZona, idRol);

        if (agentesEnZona.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron agentes con rol 3 en esta zona");
            return;
        }

        // Obtener los IDs de los agentes
        List<Integer> idAgentes = agentesEnZona.stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());

        // Obtener todas las órdenes asignadas a estos agentes
        List<Orden> ordenes = ordenRepository.findByIdAgente_IdIn(idAgentes);

        if (ordenes.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron órdenes asignadas a los agentes en esta zona");
            return;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            String[] formatosArray = formatos.split(",");
            for (String formato : formatosArray) {
                formato = formato.trim();
                byte[] fileBytes = null;
                String fileName = "Reporte_Ordenes_Zona_" + idZona;

                if (formato.equalsIgnoreCase("PDF")) {
                    fileBytes = generarReporteOrdenesPorZonaPDF(ordenes, idZona);
                    fileName += ".pdf";
                } else if (formato.equalsIgnoreCase("Excel")) {
                    fileBytes = generarReporteOrdenesPorZonaExcel(ordenes, idZona);
                    fileName += ".xlsx";
                } else if (formato.equalsIgnoreCase("CSV")) {
                    fileBytes = generarReporteOrdenesPorZonaCSV(ordenes, idZona);
                    fileName += ".csv";
                }

                if (fileBytes != null) {
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    zos.write(fileBytes);
                    zos.closeEntry();
                }
            }

            zos.close();

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=Reporte_Ordenes_Zona_" + idZona + ".zip");
            response.getOutputStream().write(baos.toByteArray());
        } catch (IOException | DocumentException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el archivo ZIP: " + e.getMessage());
        }
    }
    @GetMapping("/descargarReporteOrdenesPorZonaPDF")
    public void descargarReporteOrdenesPorZonaPDF(
            HttpSession session,
            HttpServletResponse response) throws IOException, DocumentException {

        // Obtener idAgente de la sesión
        Integer idAgente = (Integer) session.getAttribute("id");

        if (idAgente == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario no autenticado");
            return;
        }

        // Obtener el agente actual
        Optional<Usuario> optAgente = usuarioRepository.findById(idAgente);
        if (!optAgente.isPresent()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Agente no encontrado");
            return;
        }
        Usuario agenteActual = optAgente.get();

        // Obtener idZona del agente actual
        Integer idZona = agenteActual.getDistrito().getZona().getId();

        // Obtener todos los agentes con idRol=3 en esa zona
        Integer idRol = 3;
        List<Usuario> agentesEnZona = usuarioRepository.findByDistrito_Zona_IdAndRol_Id(idZona, idRol);

        if (agentesEnZona.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron agentes con rol 3 en esta zona");
            return;
        }

        // Obtener los IDs de los agentes
        List<Integer> idAgentes = agentesEnZona.stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());

        // Obtener todas las órdenes asignadas a estos agentes
        List<Orden> ordenes = ordenRepository.findByIdAgente_IdIn(idAgentes);

        if (ordenes.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron órdenes asignadas a los agentes en esta zona");
            return;
        }

        try {
            // Generar el reporte en PDF
            byte[] pdfBytes = generarReporteOrdenesPorZonaPDF(ordenes, idZona);

            // Configurar la respuesta para enviar el PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Reporte_Ordenes_Zona_" + idZona + ".pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException | DocumentException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el reporte PDF: " + e.getMessage());
        }
    }
    @GetMapping("/descargarReporteOrdenesPorZonaExcel")
    public void descargarReporteOrdenesPorZonaExcel(
            HttpSession session,
            HttpServletResponse response) throws IOException {

        // Obtener idAgente de la sesión
        Integer idAgente = (Integer) session.getAttribute("id");

        if (idAgente == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario no autenticado");
            return;
        }

        // Obtener el agente actual
        Optional<Usuario> optAgente = usuarioRepository.findById(idAgente);
        if (!optAgente.isPresent()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Agente no encontrado");
            return;
        }
        Usuario agenteActual = optAgente.get();

        // Obtener idZona del agente actual
        Integer idZona = agenteActual.getDistrito().getZona().getId();

        // Obtener todos los agentes con idRol=3 en esa zona
        Integer idRol = 3;
        List<Usuario> agentesEnZona = usuarioRepository.findByDistrito_Zona_IdAndRol_Id(idZona, idRol);

        if (agentesEnZona.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron agentes con rol 3 en esta zona");
            return;
        }

        // Obtener los IDs de los agentes
        List<Integer> idAgentes = agentesEnZona.stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());

        // Obtener todas las órdenes asignadas a estos agentes
        List<Orden> ordenes = ordenRepository.findByIdAgente_IdIn(idAgentes);

        if (ordenes.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron órdenes asignadas a los agentes en esta zona");
            return;
        }

        try {
            // Generar el reporte en Excel
            byte[] excelBytes = generarReporteOrdenesPorZonaExcel(ordenes, idZona);

            // Configurar la respuesta para enviar el archivo Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");  // Tipo MIME correcto para archivos .xlsx
            response.setHeader("Content-Disposition", "attachment; filename=Reporte_Ordenes_Zona_" + idZona + ".xlsx");  // Nombre correcto para el archivo
            response.getOutputStream().write(excelBytes);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el reporte Excel: " + e.getMessage());
        }
    }


    @GetMapping("/descargarReporteOrdenesPorZonaCSV")
    public void descargarReporteOrdenesPorZonaCSV(
            HttpSession session,
            HttpServletResponse response) throws IOException {

        // Obtener idAgente de la sesión
        Integer idAgente = (Integer) session.getAttribute("id");

        if (idAgente == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario no autenticado");
            return;
        }

        // Obtener el agente actual
        Optional<Usuario> optAgente = usuarioRepository.findById(idAgente);
        if (!optAgente.isPresent()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Agente no encontrado");
            return;
        }
        Usuario agenteActual = optAgente.get();

        // Obtener idZona del agente actual
        Integer idZona = agenteActual.getDistrito().getZona().getId();

        // Obtener todos los agentes con idRol=3 en esa zona
        Integer idRol = 3;
        List<Usuario> agentesEnZona = usuarioRepository.findByDistrito_Zona_IdAndRol_Id(idZona, idRol);

        if (agentesEnZona.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron agentes con rol 3 en esta zona");
            return;
        }

        // Obtener los IDs de los agentes
        List<Integer> idAgentes = agentesEnZona.stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());

        // Obtener todas las órdenes asignadas a estos agentes
        List<Orden> ordenes = ordenRepository.findByIdAgente_IdIn(idAgentes);

        if (ordenes.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron órdenes asignadas a los agentes en esta zona");
            return;
        }

        try {
            // Generar el reporte en CSV
            byte[] csvBytes = generarReporteOrdenesPorZonaCSV(ordenes, idZona);

            // Configurar la respuesta para enviar el archivo CSV
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=Reporte_Ordenes_Zona_" + idZona + ".csv");
            response.getOutputStream().write(csvBytes);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el reporte CSV: " + e.getMessage());
        }
    }







    // Método para generar el reporte en PDF
    private byte[] generarReporteOrdenesPorZonaPDF(List<Orden> ordenes, Integer idZona) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 90, 55);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();

        // Título
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
        Paragraph title = new Paragraph("Reporte de Órdenes por Zona y Agente", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Información general
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.DARK_GRAY);
        Paragraph info = new Paragraph("Zona ID: " + idZona + " | Rol de Agente: 3", infoFont);
        info.setSpacingAfter(20);
        document.add(info);

        // Tabla
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2, 3, 2, 2, 2});

        // Estilos
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        BaseColor headerColor = new BaseColor(0, 121, 107); // Verde oscuro

        // Encabezados
        String[] headers = {"N°", "Fecha", "Cliente", "Monto Total (S/)", "Estado", "Agente Asignado"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Contenido
        int itemNumber = 1;
        DecimalFormat df = new DecimalFormat("0.00");
        for (Orden orden : ordenes) {
            double montoTotal = calcularMontoTotalOrden(orden);
            String agenteAsignado = orden.getIdAgente().getNombre(); // Asegúrate de que el campo 'nombre' existe

            PdfPCell cell1 = new PdfPCell(new Phrase(String.valueOf(itemNumber++), cellFont));
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setPadding(5);
            table.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(new Phrase(orden.getFechaOrden().toString(), cellFont));
            cell2.setPadding(5);
            table.addCell(cell2);

            PdfPCell cell3 = new PdfPCell(new Phrase(orden.getIdCarritoCompra().getIdUsuario().getNombre(), cellFont));
            cell3.setPadding(5);
            table.addCell(cell3);

            PdfPCell cell4 = new PdfPCell(new Phrase("S/ " + df.format(montoTotal), cellFont));
            cell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell4.setPadding(5);
            table.addCell(cell4);

            PdfPCell cell5 = new PdfPCell(new Phrase(orden.getEstadoorden().getNombreEstado(), cellFont));
            cell5.setPadding(5);
            table.addCell(cell5);

            PdfPCell cell6 = new PdfPCell(new Phrase(agenteAsignado, cellFont));
            cell6.setPadding(5);
            table.addCell(cell6);
        }

        document.add(table);
        document.close();

        return baos.toByteArray();
    }



    private byte[] generarReporteOrdenesPorZonaExcel(List<Orden> ordenes, Integer idZona) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte Órdenes Zona");

        int rowIdx = 0;

        // Información general
        Row infoRow = sheet.createRow(rowIdx++);
        infoRow.createCell(0).setCellValue("Reporte de Órdenes por Zona y Agente");
        Row infoRow2 = sheet.createRow(rowIdx++);
        infoRow2.createCell(0).setCellValue("Zona ID: " + idZona + " | Rol de Agente: 3");

        // Saltar una fila
        rowIdx++;

        // Encabezados
        String[] headers = {"N°", "Fecha", "Cliente", "Monto Total (S/)", "Estado", "Agente Asignado"};
        Row headerRow = sheet.createRow(rowIdx++);

        // Estilos
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("S/ #,##0.00"));
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

        // Agregar los encabezados
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Contenido
        int itemNumber = 1;
        DecimalFormat df = new DecimalFormat("0.00");
        for (Orden orden : ordenes) {
            Row row = sheet.createRow(rowIdx++);

            double montoTotal = calcularMontoTotalOrden(orden);
            String agenteAsignado = orden.getIdAgente().getNombre();

            row.createCell(0).setCellValue(itemNumber++);
            row.createCell(1).setCellValue(orden.getFechaOrden().toString());
            row.createCell(2).setCellValue(orden.getIdCarritoCompra().getIdUsuario().getNombre());

            Cell montoCell = row.createCell(3);
            montoCell.setCellValue(montoTotal);
            montoCell.setCellStyle(currencyStyle);

            row.createCell(4).setCellValue(orden.getEstadoorden().getNombreEstado());
            row.createCell(5).setCellValue(agenteAsignado);
        }

        // Autoajustar anchos de columna
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }


    private byte[] generarReporteOrdenesPorZonaCSV(List<Orden> ordenes, Integer idZona) throws IOException {
        StringBuilder csvContent = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.00");

        // Información general
        csvContent.append("Reporte de Órdenes por Zona y Agente\n");
        csvContent.append("Zona ID:;").append(idZona).append(";Rol de Agente:;3\n\n");

        // Encabezados
        csvContent.append("N°;Fecha;Cliente;Monto Total (S/);Estado;Agente Asignado\n");

        int itemNumber = 1;
        for (Orden orden : ordenes) {
            double montoTotal = calcularMontoTotalOrden(orden);
            String agenteAsignado = orden.getIdAgente().getNombre();

            csvContent.append(itemNumber++).append(";")
                    .append(orden.getFechaOrden()).append(";")
                    .append(orden.getIdCarritoCompra().getIdUsuario().getNombre()).append(";")
                    .append(df.format(montoTotal)).append(";")
                    .append(orden.getEstadoorden().getNombreEstado()).append(";")
                    .append(agenteAsignado).append("\n");
        }

        return csvContent.toString().getBytes(StandardCharsets.UTF_8);
    }


    @GetMapping(value = "/previsualizarReporteOrdenesPorZona", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String previsualizarReporteOrdenesPorZona(HttpSession session) {
        // Obtener idAgente de la sesión
        Integer idAgente = (Integer) session.getAttribute("id");

        if (idAgente == null) {
            return "<p>Error: Usuario no autenticado.</p>";
        }

        // Obtener el agente actual
        Optional<Usuario> optAgente = usuarioRepository.findById(idAgente);
        if (!optAgente.isPresent()) {
            return "<p>Error: Agente no encontrado.</p>";
        }
        Usuario agenteActual = optAgente.get();

        // Obtener idZona del agente actual
        Integer idZona = agenteActual.getDistrito().getZona().getId();

        // Obtener todos los agentes con idRol=3 en esa zona
        Integer idRol = 3;
        List<Usuario> agentesEnZona = usuarioRepository.findByDistrito_Zona_IdAndRol_Id(idZona, idRol);

        if (agentesEnZona.isEmpty()) {
            return "<p>No se encontraron agentes con rol 3 en esta zona.</p>";
        }

        // Obtener los IDs de los agentes
        List<Integer> idAgentes = agentesEnZona.stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());

        // Obtener todas las órdenes asignadas a estos agentes
        List<Orden> ordenes = ordenRepository.findByIdAgente_IdIn(idAgentes);

        if (ordenes.isEmpty()) {
            return "<p>No se encontraron órdenes asignadas a los agentes en esta zona.</p>";
        }

        // Construcción del contenido HTML del reporte
        StringBuilder htmlContent = new StringBuilder();

        // Construir el contenido HTML
        htmlContent.append("<div class='reporte-content'>");
        htmlContent.append("<h3 class='reporte-header'>Reporte de Órdenes por Zona y Agente</h3>");
        htmlContent.append("<p><strong>Zona ID:</strong> ").append(idZona).append("</p>");
        htmlContent.append("<p><strong>Rol de Agente:</strong> 3</p>");

        htmlContent.append("<div class='table-responsive'>");
        htmlContent.append("<table class='table table-striped table-bordered'><thead><tr>")
                .append("<th scope='col'>N°</th>")
                .append("<th scope='col'>Fecha</th>")
                .append("<th scope='col'>Cliente</th>")
                .append("<th scope='col'>Monto Total (S/)</th>")
                .append("<th scope='col'>Estado</th>")
                .append("<th scope='col'>Agente Asignado</th>")
                .append("</tr></thead><tbody>");

        int itemNumber = 1;
        DecimalFormat df = new DecimalFormat("0.00");
        for (Orden orden : ordenes) {
            double montoTotal = calcularMontoTotalOrden(orden);
            String agenteAsignado = orden.getIdAgente().getNombre(); // Uso correcto del getter

            htmlContent.append("<tr>")
                    .append("<th scope='row'>").append(itemNumber++).append("</th>")
                    .append("<td>").append(orden.getFechaOrden()).append("</td>")
                    .append("<td>").append(orden.getIdCarritoCompra().getIdUsuario().getNombre()).append("</td>")
                    .append("<td>").append("S/ ").append(df.format(montoTotal)).append("</td>")
                    .append("<td>").append(orden.getEstadoorden().getNombreEstado()).append("</td>")
                    .append("<td>").append(agenteAsignado).append("</td>")
                    .append("</tr>");
        }

        htmlContent.append("</tbody></table>");
        htmlContent.append("</div>"); // Cierre de table-responsive
        htmlContent.append("</div>");

        return htmlContent.toString();
    }



}

