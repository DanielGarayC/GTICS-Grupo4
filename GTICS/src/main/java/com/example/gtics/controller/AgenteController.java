package com.example.gtics.controller;

import com.example.gtics.dto.MontoTotalOrdenDto;
import com.example.gtics.dto.OrdenCarritoDto;
import com.example.gtics.dto.ProductosCarritoDto;
import com.example.gtics.dto.ProductosxOrden;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import com.example.gtics.service.ChatRoomService;
import jakarta.servlet.http.HttpSession;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public String DescargarReporte(){

        return "Agente/Reportes/descargarReporte";
    }


}

