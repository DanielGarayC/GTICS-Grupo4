package com.example.gtics.controller;

import com.example.gtics.dto.MontoTotalOrdenDto;
import com.example.gtics.dto.OrdenCarritoDto;
import com.example.gtics.dto.ProductosCarritoDto;
import com.example.gtics.dto.ProductosxOrden;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import com.example.gtics.service.ChatRoomService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
            @RequestParam("email") String email,
            RedirectAttributes attr) {

        // Actualiza el usuario
        usuarioRepository.actualizarUsuario(idDistrito, direccion, email, id);

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
        Set<String> activeRooms = chatRoomService.getActiveRoomsAsignadas(idUsuariosAsignados);
        ArrayList<List<Message>> mensajesPorSala = new ArrayList<>();
        for(String room : activeRooms){
            //Obteniendo los mensajes enviados por el agente
            List<Message> mensajesSala = messageRepository.findBySalaOrderByFechaEnvioAsc(room);
            mensajesPorSala.add(mensajesSala);
        }
        modelAndView.addObject("activeRooms", activeRooms);
        modelAndView.addObject("MensajesPorSala", mensajesPorSala);
        return modelAndView;
    }

    @GetMapping({"Agente/UsuariosAsignados"})
    public String UsuariosAsignados(Model model,
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

        Page<Usuario> usuariosAsignados = usuarioRepository.findUsuariosAsignadosAlAgente(idAgente, pageable);
        model.addAttribute("listaUsuariosAsignados", usuariosAsignados.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosAsignados.getTotalPages());



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

        // Obtener el idAgente desde la sesión
        Integer idAgente = (Integer) session.getAttribute("id");

        if (idAgente == null) {
            // Si el idAgente no está en la sesión, redirigir o manejar el error
            return "redirect:/login";
        }

        try{
            ordenRepository.autoAsignarOrden(idAgente,idOrden);
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
        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoConDto(idUsuario, pageable);
        Optional<Usuario> usr = usuarioRepository.findById(idUsuario);


        model.addAttribute("ordenCarrito",ordenCarrito.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordenCarrito.getTotalPages());
        model.addAttribute("usuario",usr.get());
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
            attr.addAttribute("editarOrdenExitoso", true);

        }catch (Exception e){
            attr.addAttribute("editarOrdenError", true);
        }




        return "redirect:/Agente/Ordenes";
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
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        if (ordenOpt.isPresent()) {
            Orden orden = ordenOpt.get();
            List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(idOrden);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 90, 55);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Reutiliza el código existente para agregar contenido al PDF
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            DecimalFormat df = new DecimalFormat("0.00");

            // Información de la orden
            document.add(new Paragraph("Orden #" + idOrden, boldFont));
            document.add(new Paragraph("Fecha de emisión: " + orden.getFechaOrden(), normalFont));
            document.add(new Paragraph("\n"));

            // Tabla de productos
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 4, 1, 2, 2});
            table.addCell(new PdfPCell(new Phrase("N°", boldFont)));
            table.addCell(new PdfPCell(new Phrase("Descripción", boldFont)));
            table.addCell(new PdfPCell(new Phrase("Cant.", boldFont)));
            table.addCell(new PdfPCell(new Phrase("Precio Unit. (S/.)", boldFont)));
            table.addCell(new PdfPCell(new Phrase("Total (S/.)", boldFont)));

            double subtotal = 0;
            int itemNumber = 1;
            for (ProductosxOrden producto : productosOrden) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(itemNumber++), normalFont)));
                table.addCell(new PdfPCell(new Phrase(producto.getNombreProducto(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(producto.getCantidadProducto()), normalFont)));
                table.addCell(new PdfPCell(new Phrase(df.format(producto.getPrecioUnidad()), normalFont)));
                double totalProducto = producto.getPrecioTotalPorProducto();
                subtotal += totalProducto;
                table.addCell(new PdfPCell(new Phrase(df.format(totalProducto), normalFont)));
            }

            // Totales
            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(emptyCell);
            table.addCell(new PdfPCell(new Phrase("Subtotal", boldFont)));
            table.addCell(emptyCell);
            table.addCell(emptyCell);
            table.addCell(new PdfPCell(new Phrase("S/." + df.format(subtotal), normalFont)));

            document.add(table);
            document.close();

            return baos.toByteArray();
        } else {
            throw new IOException("Orden no encontrada");
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

            // Reutiliza el código existente para agregar contenido al Excel
            String[] headers = {"N°", "Descripción", "Cant.", "Precio Unitario (S/.)", "Total (S/.)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowIdx = 1;
            double subtotal = 0;
            int itemNumber = 1;
            for (ProductosxOrden producto : productosOrden) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(itemNumber++);
                row.createCell(1).setCellValue(producto.getNombreProducto());
                row.createCell(2).setCellValue(producto.getCantidadProducto());
                row.createCell(3).setCellValue(producto.getPrecioUnidad());
                double totalProducto = producto.getPrecioTotalPorProducto();
                subtotal += totalProducto;
                row.createCell(4).setCellValue(totalProducto);
            }

            // Totales
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(3).setCellValue("Total a Pagar");
            totalRow.createCell(4).setCellValue(subtotal);

            workbook.write(baos);
            workbook.close();

            return baos.toByteArray();
        } else {
            throw new IOException("Orden no encontrada");
        }
    }

    private byte[] generarOrdenCSV(Integer idOrden) throws IOException {
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        if (ordenOpt.isPresent()) {
            Orden orden = ordenOpt.get();
            List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(idOrden);

            StringBuilder csvContent = new StringBuilder();
            csvContent.append("N°;Descripción;Cant.;Precio Unitario (S/.);Total (S/.)\n");

            double subtotal = 0;
            int itemNumber = 1;
            for (ProductosxOrden producto : productosOrden) {
                double totalProducto = producto.getPrecioTotalPorProducto();
                subtotal += totalProducto;

                csvContent.append(itemNumber++).append(";")
                        .append(producto.getNombreProducto()).append(";")
                        .append(producto.getCantidadProducto()).append(";")
                        .append(producto.getPrecioUnidad()).append(";")
                        .append(totalProducto).append("\n");
            }

            csvContent.append(";;;Total a Pagar;").append(subtotal).append("\n");

            return csvContent.toString().getBytes(StandardCharsets.UTF_8);
        } else {
            throw new IOException("Orden no encontrada");
        }
    }





}

