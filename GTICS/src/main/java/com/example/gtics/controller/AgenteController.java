package com.example.gtics.controller;

import com.example.gtics.dto.MontoTotalOrdenDto;
import com.example.gtics.dto.OrdenCarritoDto;
import com.example.gtics.dto.ProductosxOrden;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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


    @ModelAttribute
    public void addUsuarioToModel(Model model) {
        Optional<Usuario> optUsuario = usuarioRepository.findById(13);
        optUsuario.ifPresent(usuarioLogueado -> model.addAttribute("usuarioLogueado", usuarioLogueado));
    }

    @GetMapping({"/Agente/Perfil"})
    public String pefil(){

        return "Agente/miperfil";
    }


    @GetMapping({"Agente"})
    public String Inicio(Model model){
        Integer idAgente = 13;
        List<Orden> listaOrdenesSinAsignar = ordenRepository.ordenesSinAsignar();
        List<Orden> listaOrdenesPendientes = ordenRepository.ordenesPendientes(idAgente);
        List<Orden> listaOrdenesEnProceso = ordenRepository.ordenesenProceso(idAgente);
        List<Orden> listaOrdenesResueltas = ordenRepository.ordenesResueltas(idAgente);

        model.addAttribute("listaOrdenesSinAsignar",listaOrdenesSinAsignar);
        model.addAttribute("listaOrdenesPendientes",listaOrdenesPendientes);
        model.addAttribute("listaOrdenesEnProceso",listaOrdenesEnProceso);
        model.addAttribute("listaOrdenesResueltas",listaOrdenesResueltas);



        return "Agente/inicio";
    }
    @GetMapping({"Agente/Chat"})
    public String ChatAgente(){

        return "Agente/chatVistaAgente";
    }

    @GetMapping({"Agente/UsuariosAsignados"})
    public String UsuariosAsignados(Model model,
                                    @RequestParam(defaultValue = "0") int page){

            int pageSize = 6;
            Pageable pageable = PageRequest.of(page, pageSize);
            Integer idAgente = 13;
            Page<Usuario> usuariosAsignados = usuarioRepository.findUsuariosAsignadosAlAgente(idAgente,pageable);


            model.addAttribute("listaUsuariosAsignados", usuariosAsignados.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usuariosAsignados.getTotalPages());


            return "Agente/UsuariosAsignados/usuariosAsignados";
    }


    // Método para banear a un usuario usando RequestParam
    @GetMapping("/Agente/banear")
    public String banearUsuario(@RequestParam Integer id,
                                RedirectAttributes redirectAttributes) {

        Optional<Usuario> optUsuario = usuarioRepository.findById(id);

        if (optUsuario.isPresent()) {
            usuarioRepository.banUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "El usuario ha sido baneado éxitosamente.");

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
                          @RequestParam(defaultValue = "0") int page){


        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);
        //Asumiendo que somos el agente con id 13 (esto se cambiará luego con login y session)
        Integer idAgente = 13;
        List<ControlOrden> listaControlOrden = controlOrdenRepository.findAll();
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        List<MontoTotalOrdenDto> listaMonto =  ordenRepository.obtenerMontoTotalConDto(idAgente);
        Page<Orden> ordenesLista = ordenRepository.buscarMisOrdenesYOrdenesSinAsignarPage(idAgente, pageable);

        model.addAttribute("ordenesLista", ordenesLista.getContent());
        model.addAttribute("listaControlOrden",listaControlOrden);
        model.addAttribute("listaEstadoOrden",listaEstadoOrden);
        model.addAttribute("listaMonto",listaMonto);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordenesLista.getTotalPages());
        return "Agente/OrdenesDeUsuario/ordeneslista";
    }

    @GetMapping({"/Agente/Ordenes/AsignarOrden"})
    public String AutoAsignarOrden(Model model,@RequestParam("idOrden") Integer idOrden,RedirectAttributes attr ){
        //Asumiendo que somos el agente con id 13 (esto se cambiará luego con login y session)
        Integer idAgente = 13;

        try{
            ordenRepository.autoAsignarOrden(idAgente,idOrden);
            attr.addFlashAttribute("msg","Se ha autoasignado la orden con éxito");
        }catch (Exception e){
            attr.addFlashAttribute("msg","No se pudo autoasignar la orden");
        }


        return "redirect:/Agente/Ordenes";
    }


    @PostMapping({"Agente/OrdenesPost"})
    public String OrdenesFiltro(Model model,
                                @RequestParam(value = "idEstado", defaultValue = "0") Integer idEstado,
                                @RequestParam(value = "idControl", defaultValue = "0") Integer idControl,
                                @RequestParam(defaultValue = "0") int page) {

        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);
        //Asumiendo que somos el agente con id 13 (esto se cambiará luego con login y session)
        Integer idAgente = 13;

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

        return "Agente/OrdenesDeUsuario/ordeneslista";
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

        try {
            ordenRepository.save(orden);
            attr.addFlashAttribute("msg", "La orden se ha actualizado exitosamente");

        }catch (Exception e){
            attr.addFlashAttribute("msg", "no se pudo actualizar");
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
    @GetMapping({"Agente/Ordenes/Descargar"})
    public String DescargarOrden(){

        return "Agente/OrdenesDeUsuario/DescargarOrdenesPorUsuario";
    }


    @GetMapping({"Agente/Reportes/Descargar"})
    public String DescargarReporte(){

        return "Agente/Reportes/descargarReporte";
    }
}

