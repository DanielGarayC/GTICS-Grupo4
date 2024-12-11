package com.example.gtics.controller;

import com.example.gtics.DNIAPI;
import com.example.gtics.dto.*;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import com.example.gtics.service.ChatRoomService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.gtics.entity.Tarjeta;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UsuarioFinalController {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioFinalController.class);
    private final UsuarioRepository usuarioRepository;
    private final DireccionRepository direccionRepository;
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
    private final TarjetaRepository tarjetaRepository;
    private final ControlOrdenRepository controlOrdenRepository;

    // Aquí usaremos un HashMap en memoria (o una caché) para simular la relación
    private final Map<Integer, Set<String>> usuariosLikes = new HashMap<>();
    private final ZonaRepository zonaRepository;
    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private DNIAPI DNIapi;
    private final MessageRepository messageRepository;

    private boolean usuarioYaDioLike(Resena resena, Usuario usuario) {
        return usuariosLikes.containsKey(resena.getId()) && usuariosLikes.get(resena.getId()).contains(usuario.getEmail());
    }

    private void registrarUsuarioQueDioLike(Resena resena, Usuario usuario) {
        usuariosLikes.computeIfAbsent(resena.getId(), k -> new HashSet<>()).add(usuario.getEmail());
    }

    private void eliminarUsuarioDeLike(Resena resena, Usuario usuario) {
        if (usuariosLikes.containsKey(resena.getId())) {
            usuariosLikes.get(resena.getId()).remove(usuario.getEmail());
            if (usuariosLikes.get(resena.getId()).isEmpty()) {
                usuariosLikes.remove(resena.getId());
            }
        }
    }

    public UsuarioFinalController(SolicitudAgenteRepository solicitudAgenteRepository,DistritoRepository distritoRepository, UsuarioRepository usuarioRepository,
                                  FotosProductoRepository fotosProductoRepository, OrdenRepository ordenRepository,
                                  EstadoOrdenRepository estadoOrdenRepository, ProductoHasCarritocompraRepository productoHasCarritocompraRepository,
                                  ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
                                  SubcategoriaRepository subcategoriaRepository, CarritoCompraRepository carritoCompraRepository,
                                  FotosResenaRepository fotosResenaRepository, ResenaRepository resenaRepository,
                                  ControlOrdenRepository controlOrdenRepository, TarjetaRepository tarjetaRepository,
                                  ForoPreguntaRepository foroPreguntaRepository, ForoRespuestaRepository foroRespuestaRepository,DireccionRepository direccionRepository,ZonaRepository zonaRepository,EtiquetaRepository etiquetaRepository, MessageRepository messageRepository) {
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
        this.direccionRepository=direccionRepository;
        this.zonaRepository=zonaRepository;
        this.messageRepository = messageRepository;
        this.controlOrdenRepository = controlOrdenRepository;
        this.tarjetaRepository = tarjetaRepository;
    }

    @GetMapping("/consulta-dni/{dni}")
    public ResponseEntity<?> consultarDNI(@PathVariable String dni) {
        List<String> datos = DNIapi.getDni(dni);
        if (datos.isEmpty()) {
            return ResponseEntity.badRequest().body("DNI no encontrado");
        }
        return ResponseEntity.ok(datos);
    }

    @GetMapping("/existe-dni/{dni}")
    public ResponseEntity<Boolean> existeDNI(@PathVariable String dni) {
        boolean existe = usuarioRepository.findAll()
                .stream()
                .anyMatch(user -> user.getDni().equals(dni));
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/consulta-email/{email}")
    public ResponseEntity<Boolean> consultarEmail(@PathVariable String email) {
        boolean existe = usuarioRepository.findAll()
                .stream()
                .anyMatch(user -> user.getEmail().equals(email));
        if (existe){
            System.out.println("El correo: "+email+ " ya existe en el sistema");
        }
        return ResponseEntity.ok(existe);
    }


    @ModelAttribute
    public void addUsuarioToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName(); // Obtener el email del usuario autenticado
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            if (optUsuario.isPresent()) {
                Usuario usuario = optUsuario.get();
                model.addAttribute("usuario", usuario);

                // Obtener el carrito activo del usuario
                Optional<Carritocompra> carritoOpt = carritoCompraRepository.findByIdUsuarioAndActivoTrue(usuario);
                if (carritoOpt.isPresent()) {
                    Carritocompra carrito = carritoOpt.get();

                    // Obtener los productos del carrito utilizando el repositorio correcto
                    List<ProductosCarritoDto> productosCarrito = productoHasCarritocompraRepository.findProductosPorCarrito(carrito.getId());
                    model.addAttribute("productosCarrito", productosCarrito);

                    // Calcular la cantidad total de productos en el carrito
                    Integer totalCantidadProductos = productoHasCarritocompraRepository.sumCantidadById_IdCarritoCompra(carrito.getId());
                    model.addAttribute("totalCantidadProductos", totalCantidadProductos != null ? totalCantidadProductos : 0);
                } else {
                    // Si no hay carrito activo, establecer valores por defecto
                    model.addAttribute("productosCarrito", Collections.emptyList());
                    model.addAttribute("totalCantidadProductos", 0);
                }
            }
        }
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);
    }


    @Transactional
    @PostMapping("/UsuarioFinal/agregarAlCarrito")
    public ResponseEntity<Map<String, Object>> agregarAlCarrito(
            @RequestParam("idProducto") Integer idProducto,
            @RequestParam("cantidad") Integer cantidad,
            HttpServletRequest request,
            HttpSession session) {

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica si el usuario está autenticado
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("mensaje", "Debes iniciar sesión para agregar productos al carrito.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            } else {
                // Respuesta tradicional
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", "/ExpressDealsLogin")
                        .build();
            }
        }

        // Obtiene el usuario autenticado por su email
        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (!optUsuario.isPresent()) {
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("mensaje", "Usuario no encontrado.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                // Respuesta tradicional
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", "/ExpressDealsLogin")
                        .build();
            }
        }

        Usuario usuario = optUsuario.get();
        Integer idUsuarioFinal = usuario.getId();
        session.setAttribute("idUsuarioFinal", idUsuarioFinal);

        // Busca un carrito activo del usuario o crea uno nuevo
        Carritocompra carrito = carritoCompraRepository.findByIdUsuarioAndActivoTrue(usuario)
                .orElseGet(() -> {
                    Carritocompra nuevoCarrito = new Carritocompra();
                    nuevoCarrito.setIdUsuario(usuario);
                    nuevoCarrito.setActivo(true); // Marca el carrito como activo
                    return carritoCompraRepository.save(nuevoCarrito);
                });
        logger.info("Carrito de compra encontrado o creado: idCarritoCompra={}", carrito.getId());
        // Busca si el producto ya está en el carrito
        Optional<ProductoHasCarritocompra> productoEnCarritoOpt =
                productoHasCarritocompraRepository.findById_IdCarritoCompraAndId_IdProducto(carrito.getId(), idProducto);

        // Si el producto ya está en el carrito, incrementa la cantidad
        if (productoEnCarritoOpt.isPresent()) {
            ProductoHasCarritocompra productoEnCarrito = productoEnCarritoOpt.get();
            productoEnCarrito.setCantidadProducto(productoEnCarrito.getCantidadProducto() + cantidad);
            productoHasCarritocompraRepository.save(productoEnCarrito);
            logger.info("Cantidad de producto actualizada en el carrito: idProducto={}, nuevaCantidad={}", idProducto, productoEnCarrito.getCantidadProducto());
        } else {
            // Si el producto no está en el carrito, lo agrega como un nuevo elemento
            Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            ProductoHasCarritocompra nuevoProductoEnCarrito = new ProductoHasCarritocompra();
            ProductoHasCarritocompraId id = new ProductoHasCarritocompraId();
            id.setIdProducto(producto.getId());
            id.setIdCarritoCompra(carrito.getId());
            nuevoProductoEnCarrito.setId(id);
            nuevoProductoEnCarrito.setIdProducto(producto);
            nuevoProductoEnCarrito.setIdCarritoCompra(carrito);
            nuevoProductoEnCarrito.setCantidadProducto(cantidad);
            productoHasCarritocompraRepository.save(nuevoProductoEnCarrito);
            logger.info("Nuevo producto agregado al carrito: idProducto={}, cantidad={}", idProducto, cantidad);
        }

        if (isAjax) {
            // Obtener el nuevo total de artículos en el carrito (suma de todas las cantidades)
            Integer  totalArticulos = productoHasCarritocompraRepository.sumCantidadById_IdCarritoCompra(carrito.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Producto agregado al carrito exitosamente.");
            response.put("totalArticulos", totalArticulos);
            logger.info("Respuesta AJAX enviada: success=true, totalArticulos={}", totalArticulos);

            return ResponseEntity.ok(response);
        } else {
            // Respuesta tradicional
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/UsuarioFinal/listaProductos")
                    .build();
        }
    }
    @GetMapping("/UsuarioFinal/distritos/{zonaId}")
    @ResponseBody
    public List<DistritoDTO> obtenerDistritosPorZona(@PathVariable Integer zonaId) {
        List<Distrito> distritos = distritoRepository.findByZonaId(zonaId);

        // Convertimos cada entidad Distrito a DTO para evitar problemas de serialización
        List<DistritoDTO> distritoDTOs = new ArrayList<>();
        for (Distrito distrito : distritos) {
            distritoDTOs.add(new DistritoDTO(distrito.getId(), distrito.getNombre()));
        }

        return distritoDTOs; // Devolvemos la lista de DTOs
    }

    @Transactional
    @DeleteMapping("/UsuarioFinal/eliminarProductoCarrito/{idProducto}")
    public String eliminarProductoCarrito(@PathVariable("idProducto") Integer idProducto,
                                          Authentication authentication,
                                          RedirectAttributes attr) {

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }

        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();

            Optional<Carritocompra> carritoOpt = carritoCompraRepository.findByIdUsuarioAndActivo(usuario, true);
            if (carritoOpt.isPresent()) {
                Carritocompra carrito = carritoOpt.get();

                Optional<ProductoHasCarritocompra> productoEnCarritoOpt =
                        productoHasCarritocompraRepository.findById_IdCarritoCompraAndId_IdProducto(carrito.getId(), idProducto);

                if (productoEnCarritoOpt.isPresent()) {
                    productoHasCarritocompraRepository.deleteById_IdCarritoCompraAndId_IdProducto(carrito.getId(), idProducto);
                }
            }
        } else {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }
        return "redirect:/UsuarioFinal/listaProductos";
    }
    @DeleteMapping("/UsuarioFinal/eliminarProductoCarritoAjax/{idProducto}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarProductoCarritoAjax(
            @PathVariable("idProducto") Integer idProducto,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        // Verificar autenticación
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            response.put("success", false);
            response.put("mensaje", "Usuario no autenticado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (!optUsuario.isPresent()) {
            response.put("success", false);
            response.put("mensaje", "Usuario no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Usuario usuario = optUsuario.get();
        Optional<Carritocompra> carritoOpt = carritoCompraRepository.findByIdUsuarioAndActivo(usuario, true);

        if (!carritoOpt.isPresent()) {
            response.put("success", false);
            response.put("mensaje", "Carrito no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Carritocompra carrito = carritoOpt.get();
        Optional<ProductoHasCarritocompra> productoOpt =
                productoHasCarritocompraRepository.findById_IdCarritoCompraAndId_IdProducto(carrito.getId(), idProducto);

        if (!productoOpt.isPresent()) {
            response.put("success", false);
            response.put("mensaje", "Producto no encontrado en el carrito.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Eliminar el producto del carrito
        productoHasCarritocompraRepository.delete(productoOpt.get());

        // Obtener el nuevo total de artículos
        Integer  totalArticulos = productoHasCarritocompraRepository.sumCantidadById_IdCarritoCompra(carrito.getId());


        // Preparar respuesta
        response.put("success", true);
        response.put("mensaje", "Producto eliminado correctamente.");
        response.put("totalArticulos", totalArticulos);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/UsuarioFinal/actualizarCantidadCarritoAjax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarCantidadCarritoAjax(
            @RequestParam("idProducto") Integer idProducto,
            @RequestParam("nuevaCantidad") Integer nuevaCantidad,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        // Verificar autenticación
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            response.put("success", false);
            response.put("mensaje", "Usuario no autenticado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (!optUsuario.isPresent()) {
            response.put("success", false);
            response.put("mensaje", "Usuario no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Usuario usuario = optUsuario.get();
        Optional<Carritocompra> carritoOpt = carritoCompraRepository.findByIdUsuarioAndActivo(usuario, true);

        if (!carritoOpt.isPresent()) {
            response.put("success", false);
            response.put("mensaje", "Carrito no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Carritocompra carrito = carritoOpt.get();
        Optional<ProductoHasCarritocompra> productoOpt =
                productoHasCarritocompraRepository.findById_IdCarritoCompraAndId_IdProducto(carrito.getId(), idProducto);

        if (!productoOpt.isPresent()) {
            response.put("success", false);
            response.put("mensaje", "Producto no encontrado en el carrito.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Validar la nueva cantidad
        if (nuevaCantidad < 1) {
            response.put("success", false);
            response.put("mensaje", "La cantidad debe ser al menos 1.");
            return ResponseEntity.badRequest().body(response);
        }

        // Actualizar la cantidad
        ProductoHasCarritocompra producto = productoOpt.get();
        producto.setCantidadProducto(nuevaCantidad);
        productoHasCarritocompraRepository.save(producto);

        // Obtener el nuevo total de artículos
        int totalArticulos = productoHasCarritocompraRepository.sumCantidadById_IdCarritoCompra(carrito.getId());

        // Preparar respuesta
        response.put("success", true);
        response.put("mensaje", "Cantidad actualizada correctamente.");
        response.put("totalArticulos", totalArticulos);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/UsuarioFinal/actualizarCantidadCarrito")
    public String actualizarCantidadCarrito(
            @RequestParam("idProducto") Integer idProducto,
            @RequestParam("nuevaCantidad") Integer nuevaCantidad,
            RedirectAttributes attr,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }

        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();
            Optional<Carritocompra> carritoOpt = carritoCompraRepository.findByIdUsuarioAndActivo(usuario, true);

            if (carritoOpt.isPresent()) {
                Carritocompra carrito = carritoOpt.get();

                Optional<ProductoHasCarritocompra> productoEnCarritoOpt =
                        productoHasCarritocompraRepository.findById_IdCarritoCompraAndId_IdProducto(carrito.getId(), idProducto);

                if (productoEnCarritoOpt.isPresent()) {
                    ProductoHasCarritocompra productoEnCarrito = productoEnCarritoOpt.get();
                    productoEnCarrito.setCantidadProducto(nuevaCantidad);
                    productoHasCarritocompraRepository.save(productoEnCarrito);

                    attr.addFlashAttribute("msg", "Cantidad actualizada correctamente.");
                }
            }
        }
        return "redirect:/UsuarioFinal/listaProductos";
    }

    @GetMapping("/UsuarioFinal/procesoCompra")
    public String procesoComprar(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);
        List<Zona> zonas = zonaRepository.findAll();
        model.addAttribute("zonas", zonas);
        Orden orden = new Orden();
        model.addAttribute("orden", orden);

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            if (optUsuario.isPresent()) {
                Usuario usuario = optUsuario.get();

                // Usar directamente la dirección del usuario
                String direccion = usuario.getDireccion();
                model.addAttribute("direccion", direccion);
                model.addAttribute("user",usuario);
                // Buscar el carrito activo del usuario
                Optional<Carritocompra> carritoOpt = carritoCompraRepository.findByIdUsuarioAndActivo(usuario, true);
                if (carritoOpt.isPresent()) {
                    Carritocompra carrito = carritoOpt.get();
                    model.addAttribute("carritoId", carrito.getId());

                    // Obtener los productos en el carrito
                    List<ProductosCarritoDto> productosCarrito = productoHasCarritocompraRepository.findProductosPorCarrito(carrito.getId());

                    // Asociar la URL de imagen de cada producto
                    Map<Integer, String> productoImagenUrls = new HashMap<>();
                    for (ProductosCarritoDto producto : productosCarrito) {
                        List<Fotosproducto> fotos = fotosProductoRepository.findByProducto_Id(producto.getIdProducto());
                        if (!fotos.isEmpty()) {
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

                    // **Nuevo cálculo del costo de envío**
                    // Encontrar el costo de envío más alto entre los productos en el carrito
                    double maxCostoEnvio = productosCarrito.stream()
                            .mapToDouble(ProductosCarritoDto::getCostoEnvio)
                            .max()
                            .orElse(0.0);
                    model.addAttribute("costoEnvio", maxCostoEnvio);

                    // Calcular el total
                    double total = subtotal + maxCostoEnvio;
                    model.addAttribute("total", total);

                    // **Nuevo cálculo del tiempo de envío**
                    LocalDate fechaCompra = LocalDate.now();
                    LocalDate fechaEnvio = fechaCompra.plusMonths(1);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    String fechaEnvioFormateada = fechaEnvio.format(formatter);

                    // Pasar la fecha al modelo
                    model.addAttribute("fechaEnvioEstimada", fechaEnvioFormateada);

                } else {
                    model.addAttribute("error", "No tienes un carrito activo para proceder con la compra.");
                    return "redirect:/UsuarioFinal/listaProductos";
                }
            }
        }
        return "UsuarioFinal/ProcesoCompra/proceso_compra";
    }

    @GetMapping("/UsuarioFinal/editarDireccion")
    public String editarDireccion(Model model) {
        // Obtener la autenticación del usuario actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            // Obtener el usuario con su dirección
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
            if (optUsuario.isPresent()) {
                Usuario usuario = optUsuario.get();

                // Obtener todas las zonas
                List<Zona> zonas = zonaRepository.findAll();
                model.addAttribute("zonas", zonas);

                List<Distrito> distritos = distritoRepository.findAll();
                model.addAttribute("distritos", distritos);

                model.addAttribute("user", usuario);

                if (usuario.getDireccion() != null) {
                    Zona zona = usuario.getZona();
                    Distrito distrito = usuario.getDistrito();

                    // Cargar las zonas y distritos seleccionados
                    model.addAttribute("zonaSeleccionada", zona);
                    model.addAttribute("distritoSeleccionado", distrito);
                }

                // Aquí podrías cargar más datos si es necesario
                return "UsuarioFinal/ProcesoCompra/proceso_compra";
            }
        }

        // Si no se encuentra el usuario o no está autenticado
        return "redirect:/UsuarioFinal/login";
    }

    @PostMapping("/UsuarioFinal/guardar-y-validar-tarjeta")
    @ResponseBody
    public String guardarYValidarTarjeta(@RequestParam("numeroTarjeta") String numeroTarjeta,
                                         @RequestParam("mesExpiracion") String mesExpiracion,
                                         @RequestParam("anioExpiracion") String anioExpiracion,
                                         @RequestParam("nombreTitular") String nombreTitular,
                                         @RequestParam(value = "idTarjeta", required = false) Integer idTarjeta) {
        // Validaciones iniciales
        if (!numeroTarjeta.matches("\\d{16}")) {
            return "El número de tarjeta debe contener 16 dígitos.";
        }
        if (!mesExpiracion.matches("(0[1-9]|1[0-2])") || !anioExpiracion.matches("\\d{2}")) {
            return "Fecha de expiración inválida. El formato debe ser MM/AA.";
        }
        if (nombreTitular.isEmpty() || !nombreTitular.matches("[a-zA-Z ]{1,100}")) {
            return "Nombre del titular inválido.";
        }

        // Convertir fecha de expiración a LocalDate
        int mesExp = Integer.parseInt(mesExpiracion);
        int anioExp = Integer.parseInt(anioExpiracion) + 2000; // Convertir a año con 4 dígitos
        LocalDate fechaExpiracion = LocalDate.of(anioExp, mesExp, 1);

        // Validar que la fecha de expiración no sea pasada
        LocalDate now = LocalDate.now();
        if (fechaExpiracion.isBefore(now)) {
            return "La tarjeta ya ha expirado o está próxima a vencer.";
        }

        // Validar que no sea el mismo mes y año actuales
        if (anioExp == now.getYear() && mesExp == now.getMonthValue()) {
            return "La tarjeta no puede expirar este mes.";
        }

        // Proceso para guardar una nueva tarjeta
        if (idTarjeta == null) {
            // Hash del número de tarjeta
            String numeroTarjetaHash = hashearNumeroTarjeta(numeroTarjeta);

            // Últimos 4 dígitos de la tarjeta
            String ultimosDigitos = numeroTarjeta.substring(numeroTarjeta.length() - 4);

            // Crear nueva tarjeta
            Tarjeta tarjeta = new Tarjeta();
            tarjeta.setNombreTitular(nombreTitular);
            tarjeta.setNumeroTarjetaHash(numeroTarjetaHash);
            tarjeta.setUltimosDigitos(ultimosDigitos);
            tarjeta.setFechaExpiracion(mesExpiracion + "/" + anioExpiracion);

            // Guardar la tarjeta en la base de datos
            tarjetaRepository.save(tarjeta);

            return "Tarjeta guardada con éxito.";
        } else {
            // Validar tarjeta existente
            Tarjeta tarjeta = tarjetaRepository.findById(idTarjeta)
                    .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

            // Validar los últimos 4 dígitos
            String ultimosDigitosIngresados = numeroTarjeta.substring(numeroTarjeta.length() - 4);
            if (!tarjeta.getUltimosDigitos().equals(ultimosDigitosIngresados)) {
                return "El número de tarjeta no coincide con los últimos 4 dígitos.";
            }

            // Validar que la fecha de expiración de la tarjeta no sea pasada
            String[] partesFecha = tarjeta.getFechaExpiracion().split("/");
            int mesTarjeta = Integer.parseInt(partesFecha[0]);
            int anioTarjeta = Integer.parseInt(partesFecha[1]) + 2000;
            LocalDate fechaExpiracionTarjeta = LocalDate.of(anioTarjeta, mesTarjeta, 1);

            if (fechaExpiracionTarjeta.isBefore(now)) {
                return "La tarjeta ya ha expirado o está a punto de expirar.";
            }

            return "Tarjeta válida.";
        }
    }


    private String hashearNumeroTarjeta(String numeroTarjeta) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(numeroTarjeta.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear el número de tarjeta", e);
        }
    }

    @GetMapping("/UsuarioFinal/tarjetas")
    public String listarTarjetas(Model model) {
        List<Tarjeta> tarjetas = tarjetaRepository.findAll(); // Recupera todas las tarjetas
        model.addAttribute("tarjetas", tarjetas);
        return "UsuarioFinal/Tarjetas/lista_tarjetas";
    }



    @PostMapping("/UsuarioFinal/procesarOrden")
    public String procesarOrden(@RequestParam("idCarritoCompra") Integer idCarritoCompra,
                                @RequestParam(name = "comentarioOrden", required = false) String comentarioOrden,
                                Model model) {

        // Paso 1: Obtener el carrito de compras activo por idCarritoCompra
        Optional<Carritocompra> carritoOpt = carritoCompraRepository.findById(idCarritoCompra);
        if (carritoOpt.isEmpty()) {
            model.addAttribute("error", "El carrito no existe o no está activo.");
            return "UsuarioFinal/Error";
        }

        Carritocompra carrito = carritoOpt.get();

        // Paso 2: Verificar si el carrito está activo
        if (!carrito.getActivo()) {
            model.addAttribute("error", "El carrito ya no está activo.");
            return "UsuarioFinal/Error";
        }

        // Paso 3: Obtener los productos del carrito usando el repositorio
        List<ProductosCarritoDto> productosCarrito = productoHasCarritocompraRepository.findProductosPorCarrito(idCarritoCompra);
        if (productosCarrito.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío.");
            return "UsuarioFinal/Error";
        }

        if (comentarioOrden == null || comentarioOrden.isEmpty()) {
            comentarioOrden = "Sin comentarios";
        }

        LocalDate fechaLlegada = LocalDate.now().plusDays(30);

        Estadoorden estadoOrden = estadoOrdenRepository.findById(1).orElseThrow(() -> new RuntimeException("Estado de orden no encontrado"));
        ControlOrden controlOrden = controlOrdenRepository.findById(1).orElseThrow(() -> new RuntimeException("ControlOrden no encontrado"));

        // Paso 4: Crear una nueva orden
        Orden orden = new Orden();
        orden.setIdCarritoCompra(carrito);
        orden.setEstadoorden(estadoOrden); // Asignamos la entidad Estadoorden
        orden.setFechaOrden(LocalDate.now()); // Fecha actual de la orden
        orden.setFechaLlegada(fechaLlegada);
        orden.setControlOrden(controlOrden);
        orden.setComentarioOrden(comentarioOrden);
        orden.setCostosAdicionales(0.0); // Si hay costos adicionales, se pueden agregar aquí
        orden.setSolicitarCancelarOrden(0); // Si la orden no requiere cancelación
        orden.setOrdenEliminada(0); // Marca que la orden no está eliminada

        // Paso 5: Guardar la orden
        Orden nuevaOrden = ordenRepository.save(orden);
        System.out.println("pruebaaa");
        // Paso 6: Actualizar los productos y cantidades disponibles
        for (ProductosCarritoDto productoCarrito : productosCarrito) {
            Producto producto = productoRepository.findById(productoCarrito.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            int cantidadComprada = productoCarrito.getCantidadProducto();

            // Configurar la clave primaria compuesta
            ProductoHasCarritocompraId productoHasCarritoId = new ProductoHasCarritocompraId();
            productoHasCarritoId.setIdProducto(producto.getId());
            productoHasCarritoId.setIdCarritoCompra(carrito.getId());

            // Configurar la entidad ProductoHasCarritocompra
            ProductoHasCarritocompra productoHasCarritocompra = new ProductoHasCarritocompra();
            productoHasCarritocompra.setId(productoHasCarritoId); // Asignar la clave compuesta
            productoHasCarritocompra.setIdProducto(producto);
            productoHasCarritocompra.setIdCarritoCompra(carrito);
            productoHasCarritocompra.setCantidadProducto(cantidadComprada);
            productoHasCarritocompra.setResenaCreada(false);

            // Guardar la relación producto-carrito
            productoHasCarritocompraRepository.save(productoHasCarritocompra);

            // Reducir la cantidad de producto disponible
            if (producto.getCantidadDisponible() >= cantidadComprada) {
                producto.setCantidadDisponible(producto.getCantidadDisponible() - cantidadComprada);
                productoRepository.save(producto);  // Actualizar el inventario del producto
            } else {
                model.addAttribute("error", "No hay suficiente stock para algunos productos.");
                return "UsuarioFinal/Error";
            }

            // Eliminar el producto del carrito de compras
            //productoHasCarritocompraRepository.deleteById_IdCarritoCompraAndId_IdProducto(
              //      idCarritoCompra, productoCarrito.getIdProducto());
        }

        // Paso 7: Desactivar el carrito
        carrito.setActivo(false);
        carritoCompraRepository.save(carrito);

        model.addAttribute("mensajeCompraRealizada", "Compra realizada con éxito");
        model.addAttribute("orden", nuevaOrden);
        return "UsuarioFinal/PaginaPrincipal/pagina_principalUF";
    }


    @GetMapping({"/UsuarioFinal", "/UsuarioFinal/pagPrincipal"})
    public String mostrarPagPrincipal(Model model, Authentication authentication){
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();  // Obtener el email del usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                Pageable pageable = PageRequest.of(0, 5); // Página 0 con 5 órdenes

                // Obtener las órdenes más recientes del usuario usando el DTO que ya tienes
                Page<OrdenCarritoDto> ordenesRecientesPage = ordenRepository.obtenerCarritoUFConDto(usuario.getId(), pageable);

                // Añadir las órdenes recientes al modelo
                model.addAttribute("ordenesRecientes", ordenesRecientesPage.getContent());
            }
        }
        return "UsuarioFinal/PaginaPrincipal/pagina_principalUF";
    }

    @GetMapping("/UsuarioFinal/buscarProductos")
    public String buscarProductos(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
            Model model) {
        List<Producto> productos = productoRepository.findByNombreContainingIgnoreCase(nombre);
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);
        if (minPrice != null && maxPrice != null) {
            productos = productos.stream()
                    .filter(producto -> producto.getPrecio() >= minPrice && producto.getPrecio() <= maxPrice)
                    .collect(Collectors.toList());
        }
        if ("asc".equalsIgnoreCase(sortOrder)) {
            productos.sort(Comparator.comparing(Producto::getPrecio));
        } else if ("desc".equalsIgnoreCase(sortOrder)) {
            productos.sort(Comparator.comparing(Producto::getPrecio).reversed());
        }
        model.addAttribute("productos", productos);
        model.addAttribute("nombreBusqueda", nombre);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortOrder", sortOrder);

        return "UsuarioFinal/Productos/listaProductos";
    }


    @PostMapping("/UsuarioFinal/solicitudAgente")
    public String enviarSolicitudaSerAgente(@ModelAttribute Solicitudagente solicitudagente) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            if (optUsuario.isPresent()) {
                Usuario usuario = optUsuario.get();
                solicitudagente.setIdUsuario(usuario);
                solicitudagente.setIndicadorSolicitud(0);
                solicitudagente.setCodigoRuc(usuario.getAgtRuc()); // Si es necesario
                solicitudagente.setCodigoJurisdiccion(String.valueOf(usuario.getDistrito().getCodigojurisdiccion()));
                solicitudAgenteRepository.save(solicitudagente);

                return "redirect:/UsuarioFinal";
            } else {
                return "redirect:/login?error";
            }
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/UsuarioFinal/producto/foto/{id}")
    public ResponseEntity<byte[]> obtenerFotoProducto(@PathVariable Integer id) {
        Optional<Fotosproducto> fotoOptional = fotosProductoRepository.findById(id);

        if (fotoOptional.isPresent()) {
            Fotosproducto fotoProducto = fotoOptional.get();
            byte[] imagenComoBytes = fotoProducto.getFoto();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(fotoProducto.getFotoContentType()));
            return new ResponseEntity<>(imagenComoBytes, httpHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/UsuarioFinal/foto/{id}")
    public ResponseEntity<byte[]> obtenerFotoUsuario(@PathVariable Integer id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario != null && usuario.getFoto() != null) {
            byte[] imagenComoBytes = usuario.getFoto();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.IMAGE_PNG);

            return new ResponseEntity<>(imagenComoBytes, httpHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/UsuarioFinal/miPerfil")
    public String miPerfil(Model model){

        List<Distrito> listaDistritos = distritoRepository.findAll();
        model.addAttribute("listaDistritos", listaDistritos);


        return "UsuarioFinal/Perfil/miperfil";
    }

    @PostMapping("/UsuarioFinal/savePerfil")
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
        return "redirect:/UsuarioFinal/miPerfil";
    }






    @GetMapping("/UsuarioFinal/listaMisOrdenes")
    public String mostrarListaMisOrdenes(Model model,
                                         @RequestParam(defaultValue = "0") int page,
                                         Authentication authentication,
                                         RedirectAttributes attr) {

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }

        // Obtén el email del usuario autenticado
        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();

            int pageSize = 6;
            Pageable pageable = PageRequest.of(page, pageSize);
            List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();

            // Aquí jalas dinámicamente el ID del usuario autenticado
            Page<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoUFConDto(usuario.getId(), pageable);

            model.addAttribute("listaEstadoOrden", listaEstadoOrden);
            model.addAttribute("ordenCarrito", ordenCarrito.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", ordenCarrito.getTotalPages());

            return "UsuarioFinal/Ordenes/listaMisOrdenes";
        } else {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }
    }


    @PostMapping("/UsuarioFinal/listaMisOrdenes/filtro")
    public String mostrarListaMisOrdenesFiltro(Model model,
                                               @RequestParam("idEstado") Integer idEstado,
                                               @RequestParam(defaultValue = "0") int page,
                                               Authentication authentication,
                                               RedirectAttributes attr) {

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }

        // Obtén el email del usuario autenticado
        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();

            int pageSize = 6;
            Pageable pageable = PageRequest.of(page, pageSize);

            System.out.println(idEstado);

            List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();

            // Ahora usamos el ID del usuario autenticado dinámicamente
            Page<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoUFConDtoFiltro(usuario.getId(), idEstado, pageable);

            model.addAttribute("listaEstadoOrden", listaEstadoOrden);
            model.addAttribute("ordenCarrito", ordenCarrito.getContent());
            model.addAttribute("idEstado", idEstado);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", ordenCarrito.getTotalPages());
            model.addAttribute("paginaFiltro", 1);

            return "UsuarioFinal/Ordenes/listaMisOrdenes";
        } else {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }
    }


    @GetMapping("/UsuarioFinal/detallesOrden")
    public String mostrarDetallesOrden(@RequestParam("idOrden") Integer idOrden,
                                       Model model,
                                       Authentication authentication,
                                       RedirectAttributes attr) {

        // Verifica si el usuario está autenticado
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }

        // Obtiene el email del usuario autenticado
        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();

            // Obtiene la orden por ID
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

            if (ordenOpt.isPresent()) {
                model.addAttribute("costoAdicional", costoAdicional);
                model.addAttribute("subtotal", subtotal);
                model.addAttribute("maxCostoEnvio", maxCostoEnvio);
                model.addAttribute("productosOrden", productosOrden);
                model.addAttribute("orden", ordenOpt.get());
                model.addAttribute("listaDistritos", listaDistritos);
                model.addAttribute("usuario", usuario);

                return "UsuarioFinal/Ordenes/detalleOrden";
            } else {
                return "UsuarioFinal/Ordenes/listaMisOrdenes";
            }
        } else {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
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
    public String mostrarListaProductos(Model model, Authentication authentication) {
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);

        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName(); // Obtener el email del usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                Zona zonaUsuario = usuario.getZona(); // Obtener la zona del usuario

                // Filtrar los productos por la zona del usuario
                List<Producto> productos = productoRepository.findProductosPorZona(zonaUsuario.getId());

                model.addAttribute("productos", productos);

                // Si hay productos, pasar el primero y sus detalles al modelo
                if (!productos.isEmpty()) {
                    Producto producto = productos.get(0); // Primer producto de la lista
                    model.addAttribute("producto", producto);
                    model.addAttribute("imagenes", fotosProductoRepository.findByProducto_Id(producto.getId()));
                    String fechaFormateada = productoRepository.findFechaFormateadaById(producto.getId());
                    model.addAttribute("fechaFormateada", fechaFormateada);
                }
            }
        }

        return "UsuarioFinal/Productos/listaProductos";
    }
    // **Nuevo Método para Listar Productos del Carrito via AJAX**
    @GetMapping("/UsuarioFinal/listaCarrito")
    @ResponseBody
    public ResponseEntity<?> getListaCarrito(Authentication authentication) {
        // Verificar si el usuario está autenticado
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("mensaje", "Usuario no autenticado."));
        }

        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (!optUsuario.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("mensaje", "Usuario no encontrado."));
        }

        Usuario usuario = optUsuario.get();
        Optional<Carritocompra> carritoOpt = carritoCompraRepository.findByIdUsuarioAndActivo(usuario, true);

        if (!carritoOpt.isPresent()) {
            return ResponseEntity.ok(Collections.emptyList()); // Carrito vacío
        }

        Carritocompra carrito = carritoOpt.get();
        List<ProductosCarritoDto> productosCarritoDTO = productoHasCarritocompraRepository.findProductosPorCarrito(carrito.getId());

        return ResponseEntity.ok(productosCarritoDTO);
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

            // Obtener productos recomendados de la misma categoría
            List<Producto> productosRecomendados = productoRepository.findByIdCategoriaAndIdNot(
                    producto.getIdCategoria(), idProducto);

            model.addAttribute("productosRecomendados", productosRecomendados.stream().limit(8).collect(Collectors.toList()));

            return "UsuarioFinal/Productos/detalleProducto";
        } else {
            return "redirect:/UsuarioFinal/listaProductos";
        }
    }


    @GetMapping("/UsuarioFinal/categorias/{idCategoria}")
    public String mostrarProductosPorCategorias(
            @PathVariable("idCategoria") Integer idCategoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "default") String sortOrder, // Parámetro de ordenamiento
            Authentication authentication,
            Model model) {

        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName(); // Obtener el email del usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            // Buscar la categoría por ID
            Optional<Categoria> categoriaOpt = categoriaRepository.findById(idCategoria);
            List<Categoria> categorias = categoriaRepository.findAll();
            model.addAttribute("categorias", categorias);

            if (usuarioOpt.isPresent() && categoriaOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                Zona zonaUsuario = usuario.getZona();  // Obtener la zona del usuario
                Categoria categoria = categoriaOpt.get();
                List<Subcategoria> subcategorias = categoria.getSubcategorias();

                // Añadir atributos de la categoría y subcategorías al modelo
                model.addAttribute("nombreCategoria", categoria.getNombreCategoria());
                model.addAttribute("subcategorias", subcategorias);

                // Definir el criterio de ordenamiento
                Sort sort = Sort.unsorted();  // Orden predeterminado
                if ("asc".equals(sortOrder)) {
                    sort = Sort.by("precio").ascending();  // Orden ascendente por precio
                } else if ("desc".equals(sortOrder)) {
                    sort = Sort.by("precio").descending();  // Orden descendente por precio
                }

                // Tamaño de página fijo
                int size = 12;  // Por ejemplo, 12 productos por página
                Pageable pageable = PageRequest.of(page, size, sort);

                // Consulta paginada con el criterio de zona y categoría
                Page<Producto> productosPage = productoRepository.findProductosPorZonaYCategoria(zonaUsuario.getId(), idCategoria, pageable);
                List<Producto> productos = productosPage.getContent();
                model.addAttribute("productos", productos);

                // Total de productos y número de páginas
                long totalProductos = productosPage.getTotalElements();
                model.addAttribute("totalProductos", totalProductos);
                int totalPages = productosPage.getTotalPages();
                model.addAttribute("totalPages", totalPages);
                model.addAttribute("currentPage", page);

                // Calcular el rango de páginas para mostrar en la paginación (3 botones visibles)
                int visiblePages = 3;
                int startPage = Math.max(0, page - (visiblePages / 2));
                int endPage = Math.min(totalPages - 1, page + (visiblePages / 2));

                // Ajustar el rango si es necesario
                if (endPage - startPage + 1 < visiblePages) {
                    if (startPage == 0) {
                        endPage = Math.min(totalPages - 1, startPage + visiblePages - 1);
                    } else if (endPage == totalPages - 1) {
                        startPage = Math.max(0, endPage - visiblePages + 1);
                    }
                }

                model.addAttribute("startPage", startPage);
                model.addAttribute("endPage", endPage);

                // Verificar si hay productos para mostrar información del producto principal
                if (!productos.isEmpty()) {
                    Producto producto = productos.get(0);  // Producto destacado
                    model.addAttribute("producto", producto);

                    // Añadir imágenes del producto y fecha formateada
                    model.addAttribute("imagenes", fotosProductoRepository.findByProducto_Id(producto.getId()));
                    String fechaFormateada = productoRepository.findFechaFormateadaById(producto.getId());
                    model.addAttribute("fechaFormateada", fechaFormateada);
                }
            } else {
                // Redirigir si no se encuentra la categoría o el usuario
                return "redirect:/UsuarioFinal/listaProductos";
            }
        }

        // Mantener el valor de sortOrder en la vista
        model.addAttribute("sortOrder", sortOrder);

        // Retornar la vista de la categoría con productos
        return "UsuarioFinal/Productos/categoria";
    }


    @GetMapping("/UsuarioFinal/subcategoria/{idSubcategoria}")
    public String mostrarProductosPorSubcategoria(
            @PathVariable("idSubcategoria") Integer idSubcategoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "default") String sortOrder,  // Parámetro de ordenamiento
            Authentication authentication,
            Model model) {

        // Buscar la subcategoría por ID
        Optional<Subcategoria> subcategoriaOpt = subcategoriaRepository.findById(idSubcategoria);
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);

        if (authentication != null && authentication.isAuthenticated() && subcategoriaOpt.isPresent()) {
            String email = authentication.getName();  // Obtener el email del usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                Zona zonaUsuario = usuario.getZona();  // Obtener la zona del usuario
                Subcategoria subcategoria = subcategoriaOpt.get();
                Categoria categoria = subcategoria.getCategoria();  // Obtener la categoría a la que pertenece
                List<Subcategoria> subcategorias = categoria.getSubcategorias();  // Obtener subcategorías relacionadas

                // Añadir atributos de la subcategoría y la categoría al modelo
                model.addAttribute("nombreSubcategoria", subcategoria.getNombreSubcategoria());
                model.addAttribute("nombreCategoria", categoria.getNombreCategoria());
                model.addAttribute("subcategorias", subcategorias);

                // Definir el criterio de ordenamiento
                Sort sort = Sort.unsorted();  // Orden predeterminado
                if ("asc".equals(sortOrder)) {
                    sort = Sort.by("precio").ascending();  // Orden ascendente por precio
                } else if ("desc".equals(sortOrder)) {
                    sort = Sort.by("precio").descending();  // Orden descendente por precio
                }

                // Tamaño de página fijo
                int size = 12;  // Por ejemplo, 12 productos por página
                Pageable pageable = PageRequest.of(page, size, sort);

                // Consulta paginada con el criterio de subcategoría y zona
                Page<Producto> productosPage = productoRepository.findProductosPorZonaYSubcategoria(zonaUsuario.getId(), idSubcategoria, pageable);
                List<Producto> productos = productosPage.getContent();
                model.addAttribute("productos", productos);

                // Total de productos y número de páginas
                long totalProductos = productosPage.getTotalElements();
                model.addAttribute("totalProductos", totalProductos);
                int totalPages = productosPage.getTotalPages();
                model.addAttribute("totalPages", totalPages);
                model.addAttribute("currentPage", page);

                // Calcular el rango de páginas para mostrar en la paginación (3 botones visibles)
                int visiblePages = 3;
                int startPage = Math.max(0, page - (visiblePages / 2));
                int endPage = Math.min(totalPages - 1, page + (visiblePages / 2));

                // Ajustar el rango si es necesario
                if (endPage - startPage + 1 < visiblePages) {
                    if (startPage == 0) {
                        endPage = Math.min(totalPages - 1, startPage + visiblePages - 1);
                    } else if (endPage == totalPages - 1) {
                        startPage = Math.max(0, endPage - visiblePages + 1);
                    }
                }

                model.addAttribute("startPage", startPage);
                model.addAttribute("endPage", endPage);

                // Verificar si hay productos para mostrar información del producto principal
                if (!productos.isEmpty()) {
                    Producto producto = productos.get(0);  // Producto destacado
                    model.addAttribute("producto", producto);

                    // Añadir imágenes del producto y fecha formateada
                    model.addAttribute("imagenes", fotosProductoRepository.findByProducto_Id(producto.getId()));
                    String fechaFormateada = productoRepository.findFechaFormateadaById(producto.getId());
                    model.addAttribute("fechaFormateada", fechaFormateada);
                }
            } else {
                // Redirigir si el usuario no se encuentra
                return "redirect:/UsuarioFinal/listaProductos";
            }
        } else {
            // Redirigir si la subcategoría no se encuentra
            return "redirect:/UsuarioFinal/listaProductos";
        }

        // Mantener el valor de sortOrder en la vista
        model.addAttribute("sortOrder", sortOrder);

        // Retornar la vista de la subcategoría con productos
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
    public String mostrarReview(Model model,
                                Authentication authentication,
                                RedirectAttributes attr) {

        // Verifica si el usuario está autenticado
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }

        // Obtiene el email del usuario autenticado
        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();

            // Obtener la lista de productos recibidos sin reseña por el usuario autenticado
            List<ProductoDTO> productosSinResena = ordenRepository.obtenerProductosPorUsuarioSinResena(usuario.getId());

            // Añadir la lista de productos al modelo para que se muestre en la vista
            model.addAttribute("productosSinResena", productosSinResena);

            // Inicializar un objeto vacío de Resena para el formulario
            model.addAttribute("resena", new Resena());

            return "UsuarioFinal/Productos/reviuw";
        } else {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }
    }


    @GetMapping("/UsuarioFinal/Resena/Fotos/{id}")
    public ResponseEntity<ByteArrayResource> obtenerFotoResena(@PathVariable Integer id) {
        Optional<Fotosresena> fotoResenaOpt = fotosResenaRepository.findById(id);

        if (fotoResenaOpt.isPresent()) {
            Fotosresena fotoResena = fotoResenaOpt.get();
            ByteArrayResource resource = new ByteArrayResource(fotoResena.getFoto());

            String mimeType = fotoResena.getTipo();
            // Si el tipo MIME es nulo o vacío, usar un tipo predeterminado (por ejemplo, image/jpeg)
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "image/jpeg";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"foto_resena_" + id + ".jpg\"")
                    .contentType(MediaType.parseMediaType(mimeType))  // Usar el tipo MIME corregido
                    .contentLength(fotoResena.getFoto().length)
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @PostMapping("/UsuarioFinal/Resena/{id}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable("id") Integer id, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Resena> optionalResena = resenaRepository.findById(id);
            if (optionalResena.isPresent()) {
                Resena resena = optionalResena.get();

                // Inicializa 'util' si es null
                if (resena.getUtil() == null) {
                    resena.setUtil(0);
                }

                String userEmail = principal.getName();  // Obtener email del usuario autenticado
                Usuario usuario = usuarioRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                // Verifica si el usuario ya dio like
                if (usuariosLikes.containsKey(resena.getId()) && usuariosLikes.get(resena.getId()).contains(userEmail)) {
                    // Si ya dio like, quitar el like
                    resena.setUtil(resena.getUtil() - 1);
                    usuariosLikes.get(resena.getId()).remove(userEmail);
                } else {
                    // Si no ha dado like, agregar el like
                    resena.setUtil(resena.getUtil() + 1);
                    usuariosLikes.computeIfAbsent(resena.getId(), k -> new HashSet<>()).add(userEmail);
                }

                // Guardar los cambios de la reseña
                resenaRepository.save(resena);

                response.put("success", true);
                response.put("newUtilCount", resena.getUtil());  // Devolver el nuevo conteo de "útil"
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Reseña no encontrada.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ocurrió un error al procesar la solicitud.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




    @PostConstruct
    public void inicializarLikesEnMemoria() {
        List<Resena> resenas = resenaRepository.findAll();

        for (Resena resena : resenas) {
            usuariosLikes.putIfAbsent(resena.getId(), new HashSet<>());
        }

        logger.info("Likes inicializados en memoria.");
    }








    @GetMapping("/UsuarioFinal/Reviews")
    public String mostrarReviews(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "3") int size,
                                 @RequestParam(required = false) String searchCriteria,
                                 @RequestParam(required = false) String searchKeyword,
                                 @RequestParam(required = false) Integer rating,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                 @RequestParam(defaultValue = "recent") String sortOrder,
                                 HttpServletRequest request) {

        // Validación de los parámetros del filtro de búsqueda
        if ((searchCriteria == null || searchCriteria.trim().isEmpty()) ||
                (searchKeyword == null || searchKeyword.trim().isEmpty())) {
            searchCriteria = null;
            searchKeyword = null;
        }

        // Validar que startDate no sea posterior a endDate
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            // Si las fechas están intercambiadas, las corregimos
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        // Orden de los resultados
        Sort sort;
        switch (sortOrder) {
            case "oldest":
                sort = Sort.by("fechaCreacion").ascending();
                break;
            case "mostHelpful":
                sort = Sort.by("util").descending();
                break;
            default:
                sort = Sort.by("fechaCreacion").descending();
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        // Llamar al método 'findByFilters' del repositorio
        Page<Resena> resenaPage = resenaRepository.findByFilters(
                searchCriteria, searchKeyword, rating, startDate, endDate, pageable
        );

        // Añadir datos al modelo
        model.addAttribute("listaResenas", resenaPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resenaPage.getTotalPages());
        model.addAttribute("pageSize", size);

        // Añadir los filtros actuales al modelo para mantenerlos en la vista
        model.addAttribute("searchCriteria", searchCriteria);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("rating", rating);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sortOrder", sortOrder);

        // Configuración de paginación avanzada
        int pageDisplayLimit = 5;
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(startPage + pageDisplayLimit - 1, resenaPage.getTotalPages());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "UsuarioFinal/Foro/foro";
    }




    @PostMapping("/UsuarioFinal/Resena/GuardarDatos")
    public String guardarResena(@Valid @ModelAttribute("resena") Resena resena,
                                BindingResult bindingResult,
                                @RequestParam(value = "uploadedPhotos", required = false) MultipartFile[] uploadedPhotos,
                                RedirectAttributes attr,
                                Model model) {

        // Obtain the authentication object
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if the user is authenticated
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }

        // Get the authenticated user's email
        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        // Check if the user exists
        if (!optUsuario.isPresent()) {
            attr.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/ExpressDealsLogin";
        }

        Usuario user = optUsuario.get();
        resena.setIdUsuario(user); // Assign the user to the review

        // Set the creation date
        resena.setFechaCreacion(LocalDate.now());

        // Handle validation errors
        if (bindingResult.hasErrors()) {
            // Re-add necessary data to the model
            List<ProductoDTO> productosSinResena = ordenRepository.obtenerProductosPorUsuarioSinResena(user.getId());
            model.addAttribute("productosSinResena", productosSinResena);
            return "UsuarioFinal/Productos/reviuw";
        }

        // Process uploaded photos
        if (uploadedPhotos != null && uploadedPhotos.length > 0) {
            List<Fotosresena> fotosResenaList = new ArrayList<>();

            for (MultipartFile uploadedPhoto : uploadedPhotos) {
                if (!uploadedPhoto.isEmpty()) {
                    if (uploadedPhoto.getSize() > 5000000) { // 5MB
                        attr.addFlashAttribute("error", "El tamaño de la foto excede el límite permitido.");
                        return "redirect:/UsuarioFinal/Review";
                    }
                    try {
                        Fotosresena fotosresena = new Fotosresena();
                        fotosresena.setFoto(uploadedPhoto.getBytes());
                        fotosresena.setTipo(uploadedPhoto.getContentType());
                        fotosresena.setIdResena(resena); // Associate the photo with the review
                        fotosResenaList.add(fotosresena);
                    } catch (IOException e) {
                        e.printStackTrace();
                        attr.addFlashAttribute("error", "Error al subir la foto. Intente nuevamente.");
                        return "redirect:/UsuarioFinal/Review";
                    }
                }
            }
            resena.setFotosresenas(fotosResenaList); // Assign the photos to the review
        }

        // Save the review
        resenaRepository.save(resena);
        attr.addFlashAttribute("msg", "Reseña creada exitosamente.");

        return "redirect:/UsuarioFinal/Reviews";
    }


    @GetMapping("/UsuarioFinal/chatbot")
    public String interactuarChatBot(){

        return "UsuarioFinal/ProcesoCompra/chatbot";
    }
    @GetMapping("/UsuarioFinal/chatSoporte")
    public String getChatPage(String room, String name, Model model) {
        model.addAttribute("room", room);
        model.addAttribute("name", name);
        int idUsuario= Integer.parseInt(room.split("_")[1]);
        Usuario usuario = usuarioRepository.findUsuarioById(idUsuario);
        List<Message> listaMensajesSala = messageRepository.findBySalaOrderByFechaEnvioAsc(room);
        model.addAttribute("ListaMensajesSala", listaMensajesSala);
        model.addAttribute("idSender", usuario.getId());

        return "UsuarioFinal/chatAntiguo";
    }
    @GetMapping("/UsuarioFinal/obtenerMensajesChat")
    @ResponseBody
    public List<Message> obtenerMensajes(String room){
        List<Message> ListaMensajesSala = messageRepository.findBySalaOrderByFechaEnvioAsc(room);
        ListaMensajesSala.forEach(mensaje -> Hibernate.initialize(mensaje.getIdUsuario()));
        return ListaMensajesSala;
    }

    @GetMapping("/UsuarioFinal/chatVista")
    public String chatRef(String room, String name, Model model) {
        //model.addAttribute("room", room);
        //model.addAttribute("name", name);
        //int idUsuario= Integer.parseInt(room.split("_")[1]);
        List<Message> listaMensajesSala = messageRepository.findBySalaOrderByFechaEnvioAsc("room_7");
        model.addAttribute("ListaMensajesSala", listaMensajesSala);
        model.addAttribute("idSender", 7);
        return "UsuarioFinal/chatAntiguo";
    }
    @GetMapping("UsuarioFinal/join-chat")
    public ModelAndView joinChat(@RequestParam("name") String name) {
        // Crear o redirigir al usuario a su sala
        String room = chatRoomService.createOrJoinRoom(name);
        Optional<Usuario> user1 = usuarioRepository.findById(Integer.parseInt(name));
        Usuario u = new Usuario();
        if(user1.isPresent()){
            u = user1.get();
        }
        String nombreUsuario = u.getNombre() + "_" + u.getApellidoPaterno();
        // Redirigir al usuario a la sala asignada
        ModelAndView modelAndView = new ModelAndView("redirect:/UsuarioFinal/chatSoporte");
        modelAndView.addObject("room", room);
        modelAndView.addObject("name", nombreUsuario);
        return modelAndView;
    }
    @GetMapping("/UsuarioFinal/foro")
    public String verForo(){

        return "UsuarioFinal/Foro/preguntasFrecuentes";
    }
    @GetMapping("/UsuarioFinal/faq")
    public String preguntasFrecuentes( @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "3") int size,Model model, @ModelAttribute("preguntaForm") Foropregunta preguntaForm){
        Page<Foropregunta> preguntasPage = foroPreguntaRepository.findAll(PageRequest.of(page, size));
        model.addAttribute("preguntasPage", preguntasPage);
        model.addAttribute("preguntas", preguntasPage.getContent());  // Las preguntas actuales
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", preguntasPage.getTotalPages());
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
    @GetMapping(value = "/prueba_api")
    public String pruebaDniApi(){

        List<String> datosRENIEC = DNIAPI.getDni("11111111");

        if (!datosRENIEC.isEmpty()){
            String apiDni = datosRENIEC.get(3);
            String apiNombres = datosRENIEC.get(0);
            String apiApellidos = (datosRENIEC.get(1) + " " + datosRENIEC.get(2));
            if(apiNombres.isEmpty()){
                System.out.println("El DNI no existe");
            }else{
                System.out.println("Datos de la persona");
                System.out.println("DNI: " + apiDni);
                System.out.println("Nombres: " + apiNombres);
                System.out.println("Apellidos: " + apiApellidos);
            }

        }

        return "redirect:/ExpressDealsLogin";
    }

    @PostMapping("/UsuarioFinal/faq/newPregunta")
    public String crearPregunta(@ModelAttribute("preguntaForm") Foropregunta preguntaForm,
                                Authentication authentication,
                                RedirectAttributes attr) {

        // Verifica si el usuario está autenticado
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }

        // Obtiene el email del usuario autenticado
        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();

            // Asigna el ID del usuario autenticado a la pregunta
            preguntaForm.setFechaCreacion(LocalDate.now());
            preguntaForm.setIdUsuario(usuario);

            // Guarda la pregunta en el repositorio
            foroPreguntaRepository.save(preguntaForm);

            return "redirect:/UsuarioFinal/faq";
        } else {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }
    }

    @PostMapping("/UsuarioFinal/faq/newRespuesta")
    public String crearRespuesta(@RequestParam("idPregunta") Integer idPregunta,
                                 @ModelAttribute("respuestaForm") Fororespuesta respuestaForm,
                                 Authentication authentication,
                                 RedirectAttributes attr) {

        // Verifica si el usuario está autenticado
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }

        // Obtiene el email del usuario autenticado
        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();

            // Encuentra la pregunta asociada al ID proporcionado
            Optional<Foropregunta> pregunta = foroPreguntaRepository.findById(idPregunta);
            pregunta.ifPresent(respuestaForm::setIdPregunta);

            // Asigna la fecha de la respuesta y el usuario autenticado que responde
            respuestaForm.setFechaRespuesta(LocalDate.now());
            respuestaForm.setIdUsuario(usuario);  // Asignar el usuario autenticado

            // Guarda la respuesta en el repositorio
            foroRespuestaRepository.save(respuestaForm);

            // Redirige a la vista de la pregunta con la respuesta
            return "redirect:/UsuarioFinal/faq/verPregunta?id=" + idPregunta;
        } else {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/ExpressDealsLogin";
        }
    }

    @GetMapping("/UsuarioFinal/descargarOrdenPDF")
    public void descargarOrdenPDF(@RequestParam("idOrden") Integer idOrden, HttpServletResponse response) throws DocumentException, IOException {
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);

        if (ordenOpt.isPresent()) {
            Orden orden = ordenOpt.get();
            List<ProductosxOrden> productosOrden = ordenRepository.obtenerProductosPorOrden(idOrden);

            // Configurar la respuesta para PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Orden_" + idOrden + ".pdf");

            // Crear el documento PDF con márgenes ajustados
            Document document = new Document(PageSize.A4, 36, 36, 90, 55);
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

            // Agregar evento para encabezado y pie de página
            HeaderFooterPageEvent event = new HeaderFooterPageEvent();
            writer.setPageEvent(event);

            document.open();

            // Estilos de fuente




            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD, BaseColor.BLACK);
            Font subtitleFont = new Font(baseFont, 14, Font.BOLD, BaseColor.DARK_GRAY);
            Font normalFont = new Font(baseFont, 11, Font.NORMAL, BaseColor.BLACK);





            Font boldFont = new Font(baseFont, 11, Font.BOLD, BaseColor.BLACK);
            Font tableHeaderFont = new Font(baseFont, 12, Font.BOLD, BaseColor.WHITE);
            DecimalFormat df = new DecimalFormat("0.00");


            // Información de la Orden
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new int[]{1, 2});
            infoTable.setSpacingAfter(20);

            // Número de Orden y Fecha
            infoTable.addCell(getCell("Número de Orden:", boldFont, Element.ALIGN_LEFT, Rectangle.NO_BORDER));
            infoTable.addCell(getCell("#" + idOrden, normalFont, Element.ALIGN_LEFT, Rectangle.NO_BORDER));
            infoTable.addCell(getCell("Fecha de Emisión:", boldFont, Element.ALIGN_LEFT, Rectangle.NO_BORDER));
            infoTable.addCell(getCell(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont, Element.ALIGN_LEFT, Rectangle.NO_BORDER));

            document.add(infoTable);

            // Sección "Vendido por" y "Enviado a"
            PdfPTable sellerBuyerTable = new PdfPTable(2);
            sellerBuyerTable.setWidthPercentage(100);
            sellerBuyerTable.setSpacingAfter(20);

            PdfPCell sellerCell = new PdfPCell();
            sellerCell.addElement(new Phrase("Vendido por:", boldFont));
            sellerCell.addElement(new Phrase("Nombre de la Empresa", normalFont));
            sellerCell.addElement(new Phrase("Dirección de la Empresa", normalFont));
            sellerCell.addElement(new Phrase("Teléfono: 123-456-789", normalFont));
            sellerCell.setBorder(Rectangle.NO_BORDER);
            sellerBuyerTable.addCell(sellerCell);

            PdfPCell buyerCell = new PdfPCell();
            buyerCell.addElement(new Phrase("Enviado a:", boldFont));
            buyerCell.addElement(new Phrase(orden.getIdCarritoCompra().getIdUsuario().getNombre(), normalFont));
            buyerCell.addElement(new Phrase(orden.getIdCarritoCompra().getIdUsuario().getDireccion(), normalFont));
            buyerCell.setBorder(Rectangle.NO_BORDER);
            sellerBuyerTable.addCell(buyerCell);

            document.add(sellerBuyerTable);

            // Tabla de Detalles de la Orden
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{1, 4, 1, 2, 2});
            table.setSpacingAfter(20);

            // Encabezados de la tabla
            String[] headers = {"N°", "Descripción", "Cant.", "Precio Unit. (S/.)", "Total (S/.)"};
            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, tableHeaderFont));
                headerCell.setBackgroundColor(BaseColor.GRAY);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(5);
                table.addCell(headerCell);
            }

            // Contenido de la tabla de productos
            double subtotal = 0;
            int itemNumber = 1;
            for (ProductosxOrden producto : productosOrden) {
                table.addCell(getCell(String.valueOf(itemNumber++), normalFont, Element.ALIGN_CENTER, Rectangle.BOX));
                table.addCell(getCell(producto.getNombreProducto(), normalFont, Element.ALIGN_LEFT, Rectangle.BOX));
                table.addCell(getCell(String.valueOf(producto.getCantidadProducto()), normalFont, Element.ALIGN_CENTER, Rectangle.BOX));
                table.addCell(getCell(df.format(producto.getPrecioUnidad()), normalFont, Element.ALIGN_RIGHT, Rectangle.BOX));
                double precioTotal = producto.getPrecioTotalPorProducto();
                subtotal += precioTotal;
                table.addCell(getCell(df.format(precioTotal), normalFont, Element.ALIGN_RIGHT, Rectangle.BOX));
            }

            // Filas de totales
            addEmptyRow(table, 5); // Agregar una fila vacía con 5 columnas
            table.addCell(getCell("", normalFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, 3));
            table.addCell(getCell("Subtotal:", boldFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER));
            table.addCell(getCell("S/." + df.format(subtotal), normalFont, Element.ALIGN_RIGHT, Rectangle.BOX));

            double maxCostoEnvio = productosOrden.stream()
                    .mapToDouble(ProductosxOrden::getCostoEnvio)
                    .max()
                    .orElse(0.0);

            table.addCell(getCell("", normalFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, 3));
            table.addCell(getCell("Costo de Envío:", boldFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER));
            table.addCell(getCell("S/." + df.format(maxCostoEnvio), normalFont, Element.ALIGN_RIGHT, Rectangle.BOX));

            Double costoAdicional = ordenRepository.obtenerCostoAdicionalxOrden(idOrden);
            table.addCell(getCell("", normalFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, 3));
            table.addCell(getCell("Costos Adicionales:", boldFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER));
            table.addCell(getCell("S/." + df.format(costoAdicional != null ? costoAdicional : 0.00), normalFont, Element.ALIGN_RIGHT, Rectangle.BOX));

            double total = subtotal + maxCostoEnvio + (costoAdicional != null ? costoAdicional : 0.00);
            table.addCell(getCell("", normalFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, 3));
            PdfPCell totalCell = getCell("Total a Pagar:", boldFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER);
            totalCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(totalCell);
            PdfPCell totalAmountCell = getCell("S/." + df.format(total), boldFont, Element.ALIGN_RIGHT, Rectangle.BOX);
            totalAmountCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(totalAmountCell);

            document.add(table);

            // Términos y Condiciones
            Paragraph terms = new Paragraph("Términos y Condiciones", subtitleFont);
            terms.setSpacingBefore(20);
            terms.setSpacingAfter(10);
            document.add(terms);

            Paragraph termsContent = new Paragraph("Este documento es válido para los fines de despacho aduanero. La mercancía detallada está sujeta a las regulaciones vigentes y debe ser manejada de acuerdo con las normativas establecidas por las autoridades competentes.", normalFont);
            termsContent.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(termsContent);

            document.close();
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Orden no encontrada");
        }
    }

    // Método para crear celdas de tabla con estilo
    private PdfPCell getCell(String text, Font font, int alignment, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(border);
        cell.setPadding(5);
        return cell;
    }

    // Sobrecarga para combinar celdas
    private PdfPCell getCell(String text, Font font, int alignment, int border, int colspan) {
        PdfPCell cell = getCell(text, font, alignment, border);
        cell.setColspan(colspan);
        return cell;
    }

    // Método para agregar una fila vacía en la tabla
    private void addEmptyRow(PdfPTable table, int cols) {
        PdfPCell emptyCell = new PdfPCell(new Phrase(" "));
        emptyCell.setColspan(cols);
        emptyCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(emptyCell);
    }

    // Clase para manejar encabezado y pie de página
    class HeaderFooterPageEvent extends PdfPageEventHelper {
        Font footerFont;
        Font boldFont;
        Font normalFont;

        public HeaderFooterPageEvent() throws DocumentException, IOException {
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            this.footerFont = new Font(baseFont, 9, Font.NORMAL, BaseColor.GRAY);
            this.boldFont = new Font(baseFont, 11, Font.BOLD, BaseColor.BLACK);
            this.normalFont = new Font(baseFont, 11, Font.NORMAL, BaseColor.BLACK);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable header = new PdfPTable(3);
            try {
                header.setWidths(new int[]{24, 24, 2});
                header.setTotalWidth(527);
                header.setLockedWidth(true);
                header.getDefaultCell().setFixedHeight(40);
                header.getDefaultCell().setBorder(Rectangle.BOTTOM);
                header.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

                ClassLoader classLoader = getClass().getClassLoader();
                String logoPath = classLoader.getResource("static/images/logo/logoGTICS.jpeg").getPath();
                Image logo = Image.getInstance(logoPath);
                logo.scaleToFit(50, 50);
                PdfPCell logoCell = new PdfPCell(logo, true);
                logoCell.setBorder(Rectangle.BOTTOM);
                logoCell.setBorderColor(BaseColor.LIGHT_GRAY);
                header.addCell(logoCell);

                PdfPCell titleCell = new PdfPCell();
                titleCell.setBorder(Rectangle.BOTTOM);
                titleCell.setBorderColor(BaseColor.LIGHT_GRAY);
                titleCell.addElement(new Phrase("Recibo de Orden para Aduanas", boldFont));
                header.addCell(titleCell);

                PdfPCell emptyCell = new PdfPCell();
                emptyCell.setBorder(Rectangle.BOTTOM);
                emptyCell.setBorderColor(BaseColor.LIGHT_GRAY);
                header.addCell(emptyCell);

                header.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }

            PdfPTable footer = new PdfPTable(1);
            footer.setTotalWidth(527);
            footer.setLockedWidth(true);
            footer.getDefaultCell().setFixedHeight(30);
            footer.getDefaultCell().setBorder(Rectangle.TOP);
            footer.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

            footer.addCell(new Phrase(String.format("Página %d", writer.getPageNumber()), footerFont));

            footer.writeSelectedRows(0, -1, 34, 50, writer.getDirectContent());
        }
    }








}