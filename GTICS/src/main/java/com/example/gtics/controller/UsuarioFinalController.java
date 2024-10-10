package com.example.gtics.controller;

import com.example.gtics.dto.OrdenCarritoDto;
import com.example.gtics.dto.ProductosCarritoDto;
import com.example.gtics.dto.ProductosxOrden;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final DistritoRepository distritoRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final ProductoHasCarritocompraRepository productoHasCarritocompraRepository;
    private final CarritoCompraRepository carritoCompraRepository;

    public UsuarioFinalController(SolicitudAgenteRepository solicitudAgenteRepository,DistritoRepository distritoRepository, UsuarioRepository usuarioRepository,
                                  FotosProductoRepository fotosProductoRepository, OrdenRepository ordenRepository,
                                  EstadoOrdenRepository estadoOrdenRepository, ProductoHasCarritocompraRepository productoHasCarritocompraRepository,
                                  ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
                                  SubcategoriaRepository subcategoriaRepository, CarritoCompraRepository carritoCompraRepository,
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
        this.distritoRepository=distritoRepository;
        this.productoRepository=productoRepository;
        this.categoriaRepository=categoriaRepository;
        this.subcategoriaRepository=subcategoriaRepository;
        this.productoHasCarritocompraRepository=productoHasCarritocompraRepository;
        this.carritoCompraRepository=carritoCompraRepository;
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

    @ModelAttribute
    public void addUsuarioAndCarritoToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            if (optUsuario.isPresent()) {
                Usuario usuario = optUsuario.get();
                model.addAttribute("usuario", usuario);

                // Obtener los productos del carrito
                List<ProductosCarritoDto> productosCarrito = ordenRepository.obtenerProductosPorUsuario(usuario.getId());
                model.addAttribute("productosCarrito", productosCarrito);

                // Calcular la cantidad total de productos en el carrito
                int totalCantidadProductos = productosCarrito.stream()
                        .mapToInt(ProductosCarritoDto::getCantidadProducto)
                        .sum();
                model.addAttribute("totalCantidadProductos", totalCantidadProductos);

            }
        }
    }

    @PostMapping("/UsuarioFinal/agregarAlCarrito")
    public String agregarAlCarrito(@RequestParam("idProducto") Integer idProducto,
                                   @RequestParam("cantidad") Integer cantidad,
                                   RedirectAttributes attr) {
        // Use SecurityContextHolder to get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Debes iniciar sesión para agregar productos al carrito.");
            return "redirect:/login";
        }

        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (optUsuario.isEmpty()) {
            attr.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/login";
        }

        Usuario usuario = optUsuario.get();

        Carritocompra carrito = carritoCompraRepository.findByIdUsuario(usuario).orElseGet(() -> {
            Carritocompra nuevoCarrito = new Carritocompra();
            nuevoCarrito.setIdUsuario(usuario);
            return carritoCompraRepository.save(nuevoCarrito);
        });

        Optional<ProductoHasCarritocompra> productoEnCarritoOpt =
                productoHasCarritocompraRepository.findById_IdCarritoCompraAndId_IdProducto(carrito.getId(), idProducto);

        if (productoEnCarritoOpt.isPresent()) {
            ProductoHasCarritocompra productoEnCarrito = productoEnCarritoOpt.get();
            productoEnCarrito.setCantidadProducto(productoEnCarrito.getCantidadProducto() + cantidad);
            productoHasCarritocompraRepository.save(productoEnCarrito);
        } else {
            Producto producto = productoRepository.findById(idProducto).orElseThrow(() ->
                    new RuntimeException("Producto no encontrado"));
            ProductoHasCarritocompra nuevoProductoEnCarrito = new ProductoHasCarritocompra();
            ProductoHasCarritocompraId id = new ProductoHasCarritocompraId();
            id.setIdProducto(producto.getId());
            id.setIdCarritoCompra(carrito.getId());
            nuevoProductoEnCarrito.setId(id);
            nuevoProductoEnCarrito.setIdProducto(producto);
            nuevoProductoEnCarrito.setIdCarritoCompra(carrito);
            nuevoProductoEnCarrito.setCantidadProducto(cantidad);
            productoHasCarritocompraRepository.save(nuevoProductoEnCarrito);
        }

        attr.addFlashAttribute("msg", "Producto agregado al carrito exitosamente.");
        return "redirect:/UsuarioFinal/listaProductos";
    }


    @PostMapping("/UsuarioFinal/eliminarProductoDelCarrito")
    public ResponseEntity<?> eliminarProductoDelCarrito(@RequestParam("idProducto") Integer idProducto,
                                                        HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }

        Optional<Carritocompra> carritoOpt = carritoCompraRepository.findByIdUsuario(usuario);
        if (carritoOpt.isPresent()) {
            Carritocompra carrito = carritoOpt.get();
            // Log the action for debugging
            System.out.println("Eliminando producto " + idProducto + " del carrito " + carrito.getId());
            productoHasCarritocompraRepository.deleteById_IdCarritoCompraAndId_IdProducto(carrito.getId(), idProducto);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carrito no encontrado.");
        }
    }

    @GetMapping("/UsuarioFinal/procesoCompra")
    public String procesoComprar(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            if (optUsuario.isPresent()) {
                Usuario usuario = optUsuario.get();

                // Obtener el carrito del usuario
                Optional<Carritocompra> carritoOpt = carritoCompraRepository.findByIdUsuario(usuario);
                if (carritoOpt.isPresent()) {
                    Carritocompra carrito = carritoOpt.get();

                    // Obtener los productos en el carrito
                    List<ProductosCarritoDto> productosCarrito = productoHasCarritocompraRepository.findProductosPorCarrito(carrito.getId());

                    // Crear un mapa para asociar cada producto con su URL de imagen
                    Map<Integer, String> productoImagenUrls = new HashMap<>();

                    // Obtener la primera imagen asociada a cada producto
                    for (ProductosCarritoDto producto : productosCarrito) {
                        List<Fotosproducto> fotos = fotosProductoRepository.findByProducto_Id(producto.getIdProducto());
                        if (!fotos.isEmpty()) {
                            // Asumimos que la primera imagen es la principal
                            String urlFoto = "/UsuarioFinal/producto/foto/" + producto.getIdProducto();
                            productoImagenUrls.put(producto.getIdProducto(), urlFoto);
                        }
                    }

                    model.addAttribute("productosCarrito", productosCarrito);
                    model.addAttribute("productoImagenUrls", productoImagenUrls);
                    // Calcular el subtotal
                    double subtotal = productosCarrito.stream()
                            .mapToDouble(ProductosCarritoDto::getPrecioTotalPorProducto)
                            .sum();
                    model.addAttribute("subtotal", subtotal);

                    // Obtener el costo de envío desde la tabla de orden
                    Double costoEnvio = ordenRepository.obtenerCostoAdicionalxOrden(carrito.getId());
                    model.addAttribute("costoEnvio", costoEnvio != null ? costoEnvio : 0.0);

                    // Calcular el total
                    double total = subtotal + (costoEnvio != null ? costoEnvio : 0.0);
                    model.addAttribute("total", total);
                }
            }
        }
        return "UsuarioFinal/ProcesoCompra/proceso_compra";
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

    @GetMapping("/UsuarioFinal/producto/foto/{id}")
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

    @GetMapping("/UsuarioFinal/miPerfil")
    public String miPerfil(Model model){

        return "UsuarioFinal/Perfil/miperfil";
    }
    @GetMapping("/UsuarioFinal/listaMisOrdenes")
    public String mostrarListaMisOrdenes(Model model,
                                         @RequestParam(defaultValue = "0") int page){
        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        Page<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoUFConDto(7,pageable); // Si el usuario tiene ID=7
        model.addAttribute("listaEstadoOrden",listaEstadoOrden);
        model.addAttribute("ordenCarrito",ordenCarrito.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordenCarrito.getTotalPages());
        return "UsuarioFinal/Ordenes/listaMisOrdenes";
    }

    @PostMapping("/UsuarioFinal/listaMisOrdenes/filtro")
    public String mostrarListaMisOrdenesFiltro(Model model,
                                               @RequestParam("idEstado") Integer idEstado,
                                               @RequestParam(defaultValue = "0") int page){

        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);
        System.out.println(idEstado);

        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        Page<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoUFConDtoFiltro(7,idEstado,pageable); // Si el usuario tiene ID=7
        model.addAttribute("listaEstadoOrden",listaEstadoOrden);
        model.addAttribute("ordenCarrito",ordenCarrito.getContent());
        model.addAttribute("idEstado",idEstado);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordenCarrito.getTotalPages());
        model.addAttribute("paginaFiltro", 1);
        return "UsuarioFinal/Ordenes/listaMisOrdenes";
    }

    @GetMapping("/UsuarioFinal/detallesOrden")
    public String mostrarDetallesOrden(@RequestParam("idOrden") Integer idOrden,Model model) {

        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(idOrden);
        List<Distrito> listaDistritos = distritoRepository.findAll();
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
            model.addAttribute("listaDistritos",listaDistritos);
            model.addAttribute("usuario",usuarioRepository.findById(7).get());

            return "UsuarioFinal/Ordenes/detalleOrden";
        }else{
            return "UsuarioFinal/Ordenes/listaMisOrdenes";
        }
    }

    @PostMapping("/UsuarioFinal/editarDireccionOrden")
    public String editarOrden(Orden orden,RedirectAttributes redd,@RequestParam("idUsuario") Integer idUsuario){
        System.out.println(orden.getIdCarritoCompra().getIdUsuario().getDireccion());
        System.out.println(orden.getIdCarritoCompra().getIdUsuario().getDistrito().getId());
        System.out.println(idUsuario);

        if(orden.getEstadoorden().getId() >=3){
            redd.addAttribute("ordenEditadaError", true);
        }else{
            ordenRepository.actualizarOrdenParaUsuarioFinal(idUsuario,orden.getIdCarritoCompra().getIdUsuario().getDireccion(),orden.getIdCarritoCompra().getIdUsuario().getDistrito().getId());
            redd.addAttribute("ordenEditadaExitosamente", true);
        }
        return "redirect:/UsuarioFinal/listaMisOrdenes";

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

    @GetMapping("/UsuarioFinal/categoria/foto/{id}")
    public ResponseEntity<byte[]> obtenerFotoCategoria(@PathVariable Integer id) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);

        if (categoriaOpt.isPresent()) {
            Categoria categoria = categoriaOpt.get();
            byte[] foto = categoria.getFotoCategoria();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(foto, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/UsuarioFinal/listaProductos")
    public String mostrarListaProductos(Model model) {
        // Obtener todas las categorías
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);

        // Si deseas mostrar productos de la primera categoría o una predeterminada
        if (!categorias.isEmpty()) {
            Categoria categoria = categorias.get(0); // Primera categoría de la lista
            List<Producto> productos = productoRepository.findProductosPorCategoria(categoria.getId());
            model.addAttribute("productos", productos);
            model.addAttribute("categoria", categoria);

            // Si hay productos en la categoría, pasar el primer producto y sus detalles al modelo
            if (!productos.isEmpty()) {
                Producto producto = productos.get(0); // Primer producto de la lista
                model.addAttribute("producto", producto);
                model.addAttribute("imagenes", fotosProductoRepository.findByProducto_Id(producto.getId()));
                String fechaFormateada = productoRepository.findFechaFormateadaById(producto.getId());
                model.addAttribute("fechaFormateada", fechaFormateada);
            }
        } else {
            model.addAttribute("productos", List.of()); // Lista vacía si no hay categorías
        }

        return "UsuarioFinal/Productos/listaProductos";
    }

    @GetMapping("/UsuarioFinal/detallesProducto/{idProducto}")
    public String mostrarDetallesProducto(@PathVariable("idProducto") Integer idProducto, Model model) {
        Optional<Producto> productoOpt = productoRepository.findById(idProducto);
        String fechaFormateada = productoRepository.findFechaFormateadaById(idProducto);

        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();

            model.addAttribute("producto", producto);
            model.addAttribute("idCategoria", producto.getIdCategoria());
            model.addAttribute("nombreCategoria", producto.getIdCategoria().getNombreCategoria());
            model.addAttribute("nombreSubcategoria", producto.getIdSubcategoria().getNombreSubcategoria());
            model.addAttribute("proveedor", producto.getIdProveedor().getTienda());
            model.addAttribute("imagenes", fotosProductoRepository.findByProducto_Id(idProducto));
            model.addAttribute("fechaFormateada", fechaFormateada);

            List<Producto> productosRecomendados = productoRepository.findProductosPorCategoria(producto.getIdCategoria().getId());
            model.addAttribute("productosRecomendados", productosRecomendados);

            return "UsuarioFinal/Productos/detalleProducto";
        } else {
            return "redirect:/UsuarioFinal/listaProductos";
        }
    }

    @GetMapping("/UsuarioFinal/categorias/{idCategoria}")
    public String mostrarProductosPorCategorias(@PathVariable("idCategoria") Integer idCategoria, Model model) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(idCategoria);

        if (categoriaOpt.isPresent()) {
            Categoria categoria = categoriaOpt.get();
            List<Subcategoria> subcategorias = categoria.getSubcategorias();

            model.addAttribute("nombreCategoria", categoria.getNombreCategoria());
            model.addAttribute("subcategorias", subcategorias);
            List<Producto> productos = productoRepository.findProductosPorCategoria(idCategoria);
            model.addAttribute("productos", productos);

            long totalProductos = productoRepository.contarProductosPorCategoria(idCategoria);
            model.addAttribute("totalProductos", totalProductos);

            if (!productos.isEmpty()) {
                Producto producto = productos.get(0);
                model.addAttribute("producto", producto);
                model.addAttribute("imagenes", fotosProductoRepository.findByProducto_Id(producto.getId()));
                String fechaFormateada = productoRepository.findFechaFormateadaById(producto.getId());
                model.addAttribute("fechaFormateada", fechaFormateada);
            }
        } else {
            return "redirect:/UsuarioFinal/listaProductos";
        }

        return "UsuarioFinal/Productos/categoria";
    }

    @GetMapping("/UsuarioFinal/subcategoria/{idSubcategoria}")
    public String mostrarProductosPorSubcategoria(@PathVariable("idSubcategoria") Integer idSubcategoria, Model model) {
        Optional<Subcategoria> subcategoriaOpt = subcategoriaRepository.findById(idSubcategoria);

        if (subcategoriaOpt.isPresent()) {
            Subcategoria subcategoria = subcategoriaOpt.get();
            List<Producto> productos = productoRepository.findProductosPorSubcategoria(idSubcategoria);
            long totalProductos = productos.size();

            Categoria categoria = subcategoria.getCategoria();
            List<Subcategoria> subcategorias = categoria.getSubcategorias();

            model.addAttribute("nombreSubcategoria", subcategoria.getNombreSubcategoria());
            model.addAttribute("nombreCategoria", categoria.getNombreCategoria());
            model.addAttribute("categoria", categoria.getId());
            model.addAttribute("subcategorias", subcategorias);
            model.addAttribute("productos", productos);
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("isSubcategory", true);

            // Si hay productos en la subcategoría, pasamos el primer producto y sus detalles al modelo
            if (!productos.isEmpty()) {
                Producto producto = productos.get(0); // Primer producto de la lista
                model.addAttribute("producto", producto);
                model.addAttribute("imagenes", fotosProductoRepository.findByProducto_Id(producto.getId()));
                String fechaFormateada = productoRepository.findFechaFormateadaById(producto.getId());
                model.addAttribute("fechaFormateada", fechaFormateada);
            }
        } else {
            return "redirect:/UsuarioFinal/listaProductos";
        }

        return "UsuarioFinal/Productos/subcategoria";
    }

    @GetMapping("/UsuarioFinal/producto/quickView/{idProducto}")
    public String mostrarModalQuickView(@PathVariable("idProducto") Integer idProducto, Model model) {
        Optional<Producto> productoOpt = productoRepository.findById(idProducto);
        String fechaFormateada = productoRepository.findFechaFormateadaById(idProducto);

        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            model.addAttribute("producto", producto);
            model.addAttribute("imagenes", fotosProductoRepository.findByProducto_Id(idProducto));
            model.addAttribute("fechaFormateada", fechaFormateada);

            return "fragments/modalProducto :: modalContent";
        } else {
            return "redirect:/UsuarioFinal/listaProductos";
        }
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

        return "UsuarioFinal/Foro/preguntasFrecuentes";
    }
    @GetMapping("/UsuarioFinal/faq")
    public String preguntasFrecuentes(Model model, @ModelAttribute("preguntaForm") Foropregunta preguntaForm){
        model.addAttribute("preguntas",foroPreguntaRepository.findAll());
        model.addAttribute("respuestas",foroRespuestaRepository.findAll());
        return "UsuarioFinal/Foro/preguntasFrecuentes";
    }

    @GetMapping("/UsuarioFinal/faq/verPregunta")
    public String verPregunta(Model model, @RequestParam("id") Integer id, @ModelAttribute("respuestaForm") Fororespuesta respuestaForm){

        Optional<Foropregunta> optP = foroPreguntaRepository.findById(id);
        if(optP.isPresent()){
            Foropregunta p = optP.get();
            List<Fororespuesta> listaRespuestas = foroRespuestaRepository.findByIdPregunta(p);
            model.addAttribute("pregunta", p);
            model.addAttribute("listaRespuestas", listaRespuestas);
            return "UsuarioFinal/Foro/preguntaDetalle";
        }
        else{
            return "UsuarioFinal/Foro/preguntasFrecuentes";
        }

    }

    @PostMapping("/UsuarioFinal/faq/newPregunta")
    public String crearPregunta(@ModelAttribute("preguntaForm") Foropregunta preguntaForm) {
        Usuario user = usuarioRepository.findUsuarioById(27); //estático por ahora

        preguntaForm.setFechaCreacion(LocalDate.now());
        preguntaForm.setIdUsuario(user);


        foroPreguntaRepository.save(preguntaForm);
        return "redirect:/UsuarioFinal/faq";
    }
    @PostMapping("/UsuarioFinal/faq/newRespuesta")
    public String crearPregunta(@RequestParam("idPregunta") Integer idPregunta, @ModelAttribute("respuestaForm") Fororespuesta respuestaForm) {
        Usuario user = usuarioRepository.findUsuarioById(27);  // Ejemplo estático; a posterior se tiene que mandar este parametro por sesion
        Optional<Foropregunta> pregunta = foroPreguntaRepository.findById(idPregunta);
        pregunta.ifPresent(respuestaForm::setIdPregunta);
        respuestaForm.setFechaRespuesta(LocalDate.now());
        respuestaForm.setIdUsuario(user);  // Asignar el usuario que responde

        foroRespuestaRepository.save(respuestaForm);  // Guardar la respuesta
        return "redirect:/UsuarioFinal/faq/verPregunta?id=" + idPregunta;
    }

}
