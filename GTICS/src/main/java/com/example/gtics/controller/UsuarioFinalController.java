package com.example.gtics.controller;

import com.example.gtics.dto.*;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import com.example.gtics.service.ChatRoomService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
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
    // Aquí usaremos un HashMap en memoria (o una caché) para simular la relación
    private final Map<Integer, Set<String>> usuariosLikes = new HashMap<>();
    private final ZonaRepository zonaRepository;
    private final EtiquetaRepository etiquetaRepository;
    @Autowired
    private ChatRoomService chatRoomService;
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
        this.etiquetaRepository=etiquetaRepository;
        this.messageRepository = messageRepository;
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
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);
    }

    @PostMapping("/UsuarioFinal/agregarAlCarrito")
    public String agregarAlCarrito(@RequestParam("idProducto") Integer idProducto,
                                   @RequestParam("cantidad") Integer cantidad,
                                   RedirectAttributes attr) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica si el usuario está autenticado
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            attr.addFlashAttribute("error", "Debes iniciar sesión para agregar productos al carrito.");
            return "redirect:/ExpressDealsLogin";
        }

        // Obtiene el usuario autenticado por su email
        String email = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        Usuario usuario = optUsuario.get();

        // Busca un carrito activo del usuario o crea uno nuevo
        Carritocompra carrito = carritoCompraRepository.findByIdUsuarioAndActivoTrue(usuario)
                .orElseGet(() -> {
                    Carritocompra nuevoCarrito = new Carritocompra();
                    nuevoCarrito.setIdUsuario(usuario);
                    nuevoCarrito.setActivo(true); // Marca el carrito como activo
                    return carritoCompraRepository.save(nuevoCarrito);
                });

        // Busca si el producto ya está en el carrito
        Optional<ProductoHasCarritocompra> productoEnCarritoOpt =
                productoHasCarritocompraRepository.findById_IdCarritoCompraAndId_IdProducto(carrito.getId(), idProducto);

        // Si el producto ya está en el carrito, incrementa la cantidad
        if (productoEnCarritoOpt.isPresent()) {
            ProductoHasCarritocompra productoEnCarrito = productoEnCarritoOpt.get();
            productoEnCarrito.setCantidadProducto(productoEnCarrito.getCantidadProducto() + cantidad);
            productoHasCarritocompraRepository.save(productoEnCarrito);
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
        }

        attr.addFlashAttribute("msg", "Producto agregado al carrito exitosamente.");
        return "redirect:/UsuarioFinal/listaProductos";
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
        List<Zona> zonas = zonaRepository.findAll(); // Asegúrate de que estás trayendo todas las zonas
        model.addAttribute("zonas", zonas);

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            if (optUsuario.isPresent()) {
                Usuario usuario = optUsuario.get();

                // Obtener etiquetas predefinidas
                List<String> etiquetasPredefinidas = Arrays.asList("Casa", "Oficina");

                // Obtener etiquetas personalizadas del usuario
                List<Etiqueta> etiquetasPersonalizadas = etiquetaRepository.findByUsuario(usuario);

                // Pasar etiquetas al modelo para mostrarlas en la vista
                model.addAttribute("etiquetasPredefinidas", etiquetasPredefinidas);
                model.addAttribute("etiquetasPersonalizadas", etiquetasPersonalizadas);

                // Buscar las direcciones del usuario
                List<Direccion> direcciones = direccionRepository.findByUsuario(usuario);
                model.addAttribute("direcciones", direcciones);

                // Si el usuario no tiene su dirección generada, crear una automáticamente
                if (!usuario.isDirecciongenerada()) {
                    Direccion nuevaDireccion = new Direccion();
                    nuevaDireccion.setUsuario(usuario);

                    String nombreCompleto = usuario.getNombre() + " " + usuario.getApellidoPaterno() + " " + usuario.getApellidoMaterno();
                    nuevaDireccion.setNombreContacto(nombreCompleto);
                    nuevaDireccion.setTelefono(usuario.getTelefono());
                    nuevaDireccion.setDireccion(usuario.getDireccion());
                    nuevaDireccion.setDistrito(usuario.getDistrito());
                    nuevaDireccion.setZona(usuario.getZona());
                    nuevaDireccion.setRuc(usuario.getAgtRuc() != null ? usuario.getAgtRuc() : "-");
                    nuevaDireccion.setPredeterminado(true);
                    direccionRepository.save(nuevaDireccion);

                    usuario.setDirecciongenerada(true);
                    usuarioRepository.save(usuario); // Actualizar el campo en la base de datos
                }

                // Buscar el carrito activo del usuario
                Optional<Carritocompra> carritoOpt = carritoCompraRepository.findByIdUsuarioAndActivo(usuario, true);
                if (carritoOpt.isPresent()) {
                    Carritocompra carrito = carritoOpt.get();

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
                } else {
                    model.addAttribute("error", "No tienes un carrito activo para proceder con la compra.");
                    return "redirect:/UsuarioFinal/listaProductos";
                }
            }
        }
        return "UsuarioFinal/ProcesoCompra/proceso_compra";
    }


    @PostMapping("/UsuarioFinal/agregarEtiquetaPersonalizada")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarEtiquetaPersonalizada(@RequestBody EtiquetaForm etiquetaForm, Principal principal) {
        String email = principal.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Guardar la etiqueta personalizada
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombreEtiqueta(etiquetaForm.getNombreEtiqueta());
        etiqueta.setUsuario(usuario);
        etiquetaRepository.save(etiqueta);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }




    @PostMapping("/UsuarioFinal/agregarDireccion")
    public String agregarDireccion(@ModelAttribute DireccionForm direccionForm, RedirectAttributes attr) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

            if (optUsuario.isPresent()) {
                Usuario usuario = optUsuario.get();

                // Si la dirección es predeterminada, actualizar las demás
                if (direccionForm.isPredeterminado()) {
                    direccionRepository.updatePredeterminadaByUsuario(usuario.getId());
                }

                // Crear y guardar la nueva dirección
                Direccion nuevaDireccion = new Direccion();
                nuevaDireccion.setUsuario(usuario);
                nuevaDireccion.setNombreContacto(direccionForm.getNombreContacto());
                nuevaDireccion.setTelefono(direccionForm.getTelefono());
                nuevaDireccion.setDireccion(direccionForm.getDireccion());
                nuevaDireccion.setDistrito(distritoRepository.findById(direccionForm.getDistritoId()).orElse(null));
                nuevaDireccion.setZona(zonaRepository.findById(direccionForm.getZonaId()).orElse(null));
                nuevaDireccion.setPredeterminado(direccionForm.isPredeterminado());

                // Asignar etiqueta (predefinida o personalizada)
                if (direccionForm.getEtiquetaId() != null && direccionForm.getEtiquetaId().matches("\\d+")) {
                    Etiqueta etiqueta = etiquetaRepository.findById(Integer.parseInt(direccionForm.getEtiquetaId())).orElse(null);
                    nuevaDireccion.setEtiqueta(etiqueta);
                } else if (direccionForm.getEtiquetaId() != null) {
                    Etiqueta nuevaEtiqueta = new Etiqueta();
                    nuevaEtiqueta.setNombreEtiqueta(direccionForm.getEtiquetaId());
                    nuevaEtiqueta.setUsuario(usuario);
                    etiquetaRepository.save(nuevaEtiqueta);
                    nuevaDireccion.setEtiqueta(nuevaEtiqueta);
                }

                // Guardar la nueva dirección
                direccionRepository.save(nuevaDireccion);
                attr.addFlashAttribute("msg", "Dirección agregada exitosamente.");
            } else {
                attr.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/login";
            }
        } else {
            attr.addFlashAttribute("error", "Usuario no autenticado.");
            return "redirect:/login";
        }

        return "redirect:/UsuarioFinal/procesoCompra";
    }


    @PostMapping("/UsuarioFinal/procesarOrden")
    public String procesarOrden(@RequestParam("idOrden") Integer idOrden, RedirectAttributes attr) {
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);

        if (ordenOpt.isPresent()) {
            Orden orden = ordenOpt.get();

            // Verifica el estado de la orden
            if (orden.getEstadoorden().getId() == 8) {
                // Si el estado es 8, el carrito asociado debe estar activo
                carritoCompraRepository.updateCarritoActivo(orden.getIdCarritoCompra().getId(), true);
            } else {
                // Si el estado no es 8, el carrito debe estar desactivado
                carritoCompraRepository.updateCarritoActivo(orden.getIdCarritoCompra().getId(), false);
            }

            attr.addFlashAttribute("msg", "El estado de la orden se ha procesado correctamente.");
        } else {
            attr.addFlashAttribute("error", "Orden no encontrada.");
        }

        return "redirect:/UsuarioFinal/listaMisOrdenes";
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
// Verifica si ya tiene la dirección generada
                if (!usuario.isDirecciongenerada()) {
                    Direccion nuevaDireccion = new Direccion();
                    nuevaDireccion.setUsuario(usuario);

                    // Concatenamos el nombre completo del usuario
                    String nombreCompleto = usuario.getNombre() + " " + usuario.getApellidoPaterno() + " " + usuario.getApellidoMaterno();
                    nuevaDireccion.setNombreContacto(nombreCompleto);

                    // Asignamos el teléfono y la dirección del usuario
                    nuevaDireccion.setTelefono(usuario.getTelefono());
                    nuevaDireccion.setDireccion(usuario.getDireccion());

                    // Asignamos el distrito y la zona del usuario
                    nuevaDireccion.setDistrito(usuario.getDistrito());
                    nuevaDireccion.setZona(usuario.getZona());

                    // Si el usuario no tiene RUC, dejamos el campo vacío
                    nuevaDireccion.setRuc(usuario.getAgtRuc() != null ? usuario.getAgtRuc() : "-");

                    // Establecemos como predeterminada
                    nuevaDireccion.setPredeterminado(true);

                    // Guardamos la dirección en la base de datos
                    direccionRepository.save(nuevaDireccion);

                    // Marcamos en el usuario que ya tiene su dirección generada
                    usuario.setDirecciongenerada(true);
                    usuarioRepository.save(usuario); // Actualizamos el campo en la base de datos
                }

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

        return "UsuarioFinal/Perfil/miperfil";
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
    public String mostrarReview(Model model){
// Obtener el usuario autenticado (en este caso, estoy usando un ID estático para el ejemplo)
        Integer idUsuario = 7;  // Cambia esto por el método de autenticación real

        // Obtener la lista de productos recibidos sin reseña
        List<ProductoDTO> productosSinResena = ordenRepository.obtenerProductosPorUsuarioSinResena(idUsuario);

        // Añadir la lista de productos al modelo para que se muestre en la vista
        model.addAttribute("productosSinResena", productosSinResena);
// Inicializar un objeto vacío de Resena para el formulario
        model.addAttribute("resena", new Resena());
        return "UsuarioFinal/Productos/reviuw";
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

        // Set fields before validation check
        Usuario user = usuarioRepository.findUsuarioById(7); // Replace with actual user retrieval logic
        if (user == null) {
            attr.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/UsuarioFinal/Review";
        }
        resena.setIdUsuario(user); // Assign the user to the review

        // Set the creation date
        resena.setFechaCreacion(LocalDate.now());

        if (bindingResult.hasErrors()) {
            // Re-add necessary data to the model
            Integer idUsuario = 3;  // Replace with actual user ID
            List<ProductoDTO> productosSinResena = ordenRepository.obtenerProductosPorUsuarioSinResena(idUsuario);
            model.addAttribute("productosSinResena", productosSinResena);
            return "UsuarioFinal/Productos/reviuw";
        }

        // Process uploaded photos
        if (uploadedPhotos != null && uploadedPhotos.length > 0) {
            List<Fotosresena> fotosResenaList = new ArrayList<>();

            for (MultipartFile uploadedPhoto : uploadedPhotos) {
                if (!uploadedPhoto.isEmpty()) {
                    if (uploadedPhoto.getSize() > 5000000) { //5MB

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
        List<Message> listaMensajesSala = messageRepository.findBySala(room);
        model.addAttribute("ListaMensajesSala", listaMensajesSala);
        return "UsuarioFinal/chatUsuario";
    }

    @GetMapping("/UsuarioFinal/chatVista")
    public String chatRef() {

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
