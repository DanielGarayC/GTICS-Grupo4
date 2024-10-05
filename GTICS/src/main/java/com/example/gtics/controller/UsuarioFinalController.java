package com.example.gtics.controller;

import com.example.gtics.dto.OrdenCarritoDto;
import com.example.gtics.dto.ProductosxOrden;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class UsuarioFinalController {
    private final UsuarioRepository usuarioRepository;
    private final SolicitudAgenteRepository solicitudAgenteRepository;
    private final FotosProductoRepository fotosProductoRepository;
    private final OrdenRepository ordenRepository;
    private final EstadoOrdenRepository estadoOrdenRepository;

    public UsuarioFinalController(SolicitudAgenteRepository solicitudAgenteRepository, UsuarioRepository usuarioRepository,
                                  FotosProductoRepository fotosProductoRepository, OrdenRepository ordenRepository, EstadoOrdenRepository estadoOrdenRepository) {
        this.solicitudAgenteRepository = solicitudAgenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.fotosProductoRepository = fotosProductoRepository;
        this.ordenRepository = ordenRepository;
        this.estadoOrdenRepository = estadoOrdenRepository;
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
        List<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoConDto(7); // Si el usuario tiene ID=7
        model.addAttribute("listaEstadoOrden",listaEstadoOrden);
        model.addAttribute("ordenCarrito",ordenCarrito);
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
            model.addAttribute("orden",ordenOpt.get());

            return "UsuarioFinal/Ordenes/detalleOrden";
        }else{
            return "UsuarioFinal/Ordenes/listaMisOrdenes";
        }


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
    public String preguntasFrecuentes(){

        return "UsuarioFinal/Foro/preguntasFrecuentes";
    }
}
