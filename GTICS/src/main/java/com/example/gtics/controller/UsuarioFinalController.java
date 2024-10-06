package com.example.gtics.controller;

import com.example.gtics.dto.OrdenCarritoDto;
import com.example.gtics.dto.ProductosxOrden;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

@Controller
public class UsuarioFinalController {
    private final UsuarioRepository usuarioRepository;
    private final SolicitudAgenteRepository solicitudAgenteRepository;
    private final FotosProductoRepository fotosProductoRepository;
    private final OrdenRepository ordenRepository;
    private final EstadoOrdenRepository estadoOrdenRepository;
    private final FotosResenaRepository fotosResenaRepository;
    private final ResenaRepository resenaRepository;
    private final ForoPreguntaRepository foroPreguntaRepository;
    private final ForoRespuestaRepository foroRespuestaRepository;

    public UsuarioFinalController(SolicitudAgenteRepository solicitudAgenteRepository, UsuarioRepository usuarioRepository,
                                  FotosProductoRepository fotosProductoRepository, OrdenRepository ordenRepository,
                                  EstadoOrdenRepository estadoOrdenRepository,
                                  FotosResenaRepository fotosResenaRepository, ResenaRepository resenaRepository,
                                  ForoPreguntaRepository foroPreguntaRepository, ForoRespuestaRepository foroRespuestaRepository) {
        this.solicitudAgenteRepository = solicitudAgenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.fotosProductoRepository = fotosProductoRepository;
        this.ordenRepository = ordenRepository;
        this.estadoOrdenRepository = estadoOrdenRepository;
        this.resenaRepository = resenaRepository;
        this.fotosResenaRepository = fotosResenaRepository;
        this.foroPreguntaRepository = foroPreguntaRepository;
        this.foroRespuestaRepository = foroRespuestaRepository;
    }

    @ModelAttribute
    public void addUsuarioToModel(Model model) {
        Optional<Usuario> optUsuario = usuarioRepository.findById(7);  // Aquí cambias el ID según el usuario que necesites
        // Usuario agregado globalmente
        optUsuario.ifPresent(usuario -> model.addAttribute("usuario", usuario));
    }

    @GetMapping({"/UsuarioFinal", "/UsuarioFinal/pagPrincipal"})
    public String mostrarPagPrincipal(Model model){
            return "UsuarioFinal/PaginaPrincipal/pagina_principalUF";
    }

    @PostMapping("/UsuarioFinal/solicitudAgente")
    public String enviarSolicitudaSerAgente(Solicitudagente solicitudagente){
        solicitudagente.setIndicadorSolicitud(0);
        //solicitudagente.setValidaciones(1);
        solicitudagente.setCodigoJurisdiccion("333");

        //solicitudagente.setValidaciones(new Validacionescodigosagente());
        /*
        System.out.println(solicitudagente.getCodigoAduana());
        System.out.println(solicitudagente.getCodigoJurisdiccion());
        System.out.println(solicitudagente.getIndicadorSolicitud());
        */
        solicitudAgenteRepository.save(solicitudagente);
        Optional<Usuario> optUsuario = usuarioRepository.findById(7);
        Solicitudagente ultimaSolicitud = solicitudAgenteRepository.findTopByOrderByIdDesc();

        if(optUsuario.isPresent()) {
            Usuario us = optUsuario.get(); // usuario random que solicita ser agente


            usuarioRepository.asignarSolictudAusuario(ultimaSolicitud.getId(), us.getId());
        }
        return "redirect:/UsuarioFinal";
    }

    @GetMapping("/productos/foto/{id}")
    public ResponseEntity<ByteArrayResource> obtenerFotoProducto(@PathVariable Integer id) {
        Fotosproducto fotosProducto = fotosProductoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foto del producto no encontrada"));

        byte[] foto = fotosProducto.getFoto();
        ByteArrayResource resource = new ByteArrayResource(foto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"foto_producto_" + id + ".jpg\"")
                .contentLength(foto.length)
                .body(resource);
    }

    @GetMapping("/UsuarioFinal/miPerfil")
    public String miPerfil(Model model){

        return "UsuarioFinal/Perfil/miperfil";
    }
    @GetMapping("/UsuarioFinal/listaMisOrdenes")
    public String mostrarListaMisOrdenes(Model model){
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        List<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoUFConDto(7); // Si el usuario tiene ID=7
        model.addAttribute("listaEstadoOrden",listaEstadoOrden);
        model.addAttribute("ordenCarrito",ordenCarrito);
        return "UsuarioFinal/Ordenes/listaMisOrdenes";
    }

    @PostMapping("/UsuarioFinal/listaMisOrdenes/filtro")
    public String mostrarListaMisOrdenesFiltro(Model model,@RequestParam("idEstado") Integer idEstado){
        System.out.println(idEstado);
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        List<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoUFConDtoFiltro(7,idEstado); // Si el usuario tiene ID=7
        model.addAttribute("listaEstadoOrden",listaEstadoOrden);
        model.addAttribute("ordenCarrito",ordenCarrito);
        model.addAttribute("idEstado",idEstado);
        return "UsuarioFinal/Ordenes/listaMisOrdenes";
    }

    @GetMapping("/UsuarioFinal/detallesOrden")
    public String mostrarDetallesOrden(@RequestParam("idOrden") Integer idOrden,Model model) {

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
        if(ordenOpt.isPresent()){
            model.addAttribute("costoAdicional",costoAdicional);
            model.addAttribute("subtotal",subtotal);
            model.addAttribute("maxCostoEnvio",maxCostoEnvio);
            model.addAttribute("productosOrden",productosOrden);
            model.addAttribute("orden",ordenOpt.get());

            return "UsuarioFinal/Ordenes/detalleOrden";
        }else{
            return "UsuarioFinal/Ordenes/listaMisOrdenes";
        }


    }
    @GetMapping("/UsuarioFinal/eliminarOrden")
    public String solicitarEliminarOrden(@RequestParam Integer idOrden, RedirectAttributes attr){
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        if(ordenOpt.get().getEstadoorden().getId()>=3){
            attr.addAttribute("ordenEliminadaEstadoNoValido", true);
        }else{
            ordenRepository.solicitarEliminarOrden(idOrden);
            attr.addAttribute("ordenEliminadaExitosamente", true);
        }

        return "redirect:/UsuarioFinal/listaMisOrdenes";
    }
    @GetMapping("/UsuarioFinal/solicitarApoyo")
    public String solicitarApoyoOrden(@RequestParam Integer idOrden, RedirectAttributes attr){
        ordenRepository.solicitarUnAgente(idOrden);//se le asigna la orden al agente de id = 13 -> arreglar mas adelante
        attr.addAttribute("solicitudAgenteExitosamente", true);
        return "redirect:/UsuarioFinal/listaMisOrdenes";
    }

    @GetMapping("/UsuarioFinal/listaProductos")
    public String mostrarListaProductos(){

        return "UsuarioFinal/Productos/listaProductos";
    }
    @GetMapping("/UsuarioFinal/detallesProducto")
    public String mostrarDetallesProducto(){

        return "UsuarioFinal/Productos/detalleProducto";
    }
    @GetMapping("/UsuarioFinal/categorias")
    public String mostrarCategorias(){

        return "UsuarioFinal/Productos/categoria";
    }
    @GetMapping("/UsuarioFinal/Review")
    public String mostrarReview(){

        return "UsuarioFinal/Productos/reviuw";
    }

    @PostMapping("/UsuarioFinal/Resena/GuardarFotos")
    @ResponseBody
    public ResponseEntity<?> guardarFotos(@RequestParam("fotos") List<MultipartFile> fotos) {
        List<String> nombresFotos = new ArrayList<>();

        try {
            for (MultipartFile foto : fotos) {
                if (!foto.isEmpty()) {
                    // Guardar cada archivo
                    Fotosresena fotosresena = new Fotosresena();
                    fotosresena.setFoto(foto.getBytes());


                    nombresFotos.add(foto.getOriginalFilename()); // Añadir el nombre del archivo
                }
            }
            return new ResponseEntity<>(nombresFotos, HttpStatus.OK); // Respuesta exitosa con los nombres de los archivos subidos
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error guardando las fotos", HttpStatus.INTERNAL_SERVER_ERROR); // Respuesta de error
        }
    }

    @PostMapping("/UsuarioFinal/Resena/GuardarDatos")
    @Transactional // Ensure both the review and photos are saved within the same transaction
    public String guardarResena(@ModelAttribute("resena") Resena resena,
                                @RequestParam("reviewTitle") String reviewTitle,
                                @RequestParam("reviewOpinion") String reviewOpinion,
                                @RequestParam("calificacionAtencion") int calificacionAtencion,
                                @RequestParam("calificacionCalidad") int calificacionCalidad,
                                @RequestParam("uploadedPhotos") String uploadedPhotos, // Receive the uploaded photos
                                BindingResult bindingResult,
                                RedirectAttributes attr,
                                Model model) {

        // Set review attributes
        resena.setTema(reviewTitle);
        resena.setOpinion(reviewOpinion);

        // Assigning quality rating
        Calidad calidad = new Calidad();
        calidad.setId(calificacionCalidad);
        resena.setIdCalidad(calidad);

        // Assigning agent attention rating
        Atencion atencion = new Atencion();
        atencion.setId(calificacionAtencion);
        resena.setIdAtencion(atencion);

        // Check if product is associated, otherwise set default product
        if (resena.getProducto() == null) {
            Producto productoDefault = new Producto();
            productoDefault.setId(1); // Set the default product ID
            resena.setProducto(productoDefault);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Por favor, revisa los campos e inténtalo nuevamente.");
            return "UsuarioFinal/Resena/crearResena";
        }

        try {
            // First, save the review and get the generated ID
            Resena nuevaResena = resenaRepository.save(resena);
            if (nuevaResena.getId() == null) {
                throw new RuntimeException("Error al guardar la reseña. El ID es nulo.");
            }

            System.out.println("Reseña guardada con éxito. ID: " + nuevaResena.getId());

            // Save photos if there are any uploaded
            if (!uploadedPhotos.isEmpty()) {
                String[] fotos = uploadedPhotos.split(",");
                for (String foto : fotos) {
                    Fotosresena fotosresena = new Fotosresena();
                    fotosresena.setFoto(foto.getBytes()); // Adjust this to properly handle file content
                    fotosresena.setIdResena(nuevaResena); // Associate the photos with the newly saved review
                    fotosResenaRepository.save(fotosresena);
                    System.out.println("Foto asociada a la reseña guardada correctamente.");
                }
            }

            attr.addFlashAttribute("msg", "Reseña creada exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            attr.addFlashAttribute("error", "Ocurrió un error al guardar la reseña o las fotos.");
        }

        return "redirect:/UsuarioFinal/foro";
    }
    @GetMapping("/UsuarioFinal/procesoCompra")
    public String procesoComprar(){

        return "UsuarioFinal/ProcesoCompra/proceso_compra";
    }
    @GetMapping("/UsuarioFinal/chatbot")
    public String interactuarChatBot(){

        return "UsuarioFinal/ProcesoCompra/chatbot";
    }
    @GetMapping("/UsuarioFinal/chatSoporte")
    public String chatSoporte(){

        return "UsuarioFinal/ProcesoCompra/chatSoporte";
    }
    @GetMapping("/UsuarioFinal/foro")
    public String verForo(){

        return "UsuarioFinal/Foro/foro";
    }
    @GetMapping("/UsuarioFinal/faq")
    public String preguntasFrecuentes(Model model){
        model.addAttribute("preguntas",foroPreguntaRepository.findAll());
        model.addAttribute("respuestas",foroRespuestaRepository.findAll());
        return "UsuarioFinal/Foro/preguntasFrecuentes";
    }
    @PostMapping("/UsuarioFinal/faq/newPregunta")
    public String crearPregunta(@RequestParam("pregunta") String pregunta) {
        Usuario user = usuarioRepository.findUsuarioById(2); //estático por ahora
        Foropregunta question = new Foropregunta();
        question.setPregunta(pregunta);
        question.setFechaCreacion(LocalDate.now());
        question.setIdUsuario(user);
        foroPreguntaRepository.save(question);
        return "redirect:/UsuarioFinal/faq";
    }
    @PostMapping("/UsuarioFinal/faq/newRespuesta")
    public String crearPregunta(@RequestParam("idPregunta") int idPregunta,@RequestParam("contenidoRespuesta") String contenidoRespuesta) {
        Usuario user = usuarioRepository.findUsuarioById(2);  // Ejemplo estático
        Optional<Foropregunta> pregunta = foroPreguntaRepository.findById(idPregunta);
        System.out.println(pregunta.get().getPregunta());
        System.out.println(contenidoRespuesta);
        Fororespuesta respuesta = new Fororespuesta();
        respuesta.setIdPregunta(pregunta.get());
        respuesta.setContenidoRespuesta(contenidoRespuesta);
        respuesta.setFechaRespuesta(LocalDate.now());
        respuesta.setIdUsuario(user);  // Asignar el usuario que responde

        foroRespuestaRepository.save(respuesta);  // Guardar la respuesta
        return "redirect:/UsuarioFinal/faq";
    }
}
