package com.example.gtics.controller;

import com.example.gtics.dto.MontoTotalOrdenDto;
import com.example.gtics.dto.OrdenCarritoDto;
import com.example.gtics.entity.*;
import com.example.gtics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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



    @GetMapping({"Agente"})
    public String Inicio(){

        return "Agente/inicio";
    }
    @GetMapping({"Agente/Chat"})
    public String ChatAgente(){

        return "Agente/chatVistaAgente";
    }

    @GetMapping({"Agente/UsuariosAsignados"})
    public String UsuariosAsignados(Model model){

        //asumiendo que el agente tiene id = 13
        Integer idAgente = 13;
        List<Usuario> usuariosAsignados = usuarioRepository.findUsuariosAsignadosAlAgente(idAgente);

        model.addAttribute("listaUsuariosAsignados", usuariosAsignados);

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



    @GetMapping({"Agente/Ordenes"})
    public String Ordenes(Model model){

        //Asumiendo que somos el agente con id 13 (esto se cambiará luego con login y session)
        Integer idAgente = 13;

        List<Orden> ordenesLista = ordenRepository.buscarMisOrdenesYOrdenesSinAsignar(idAgente);
        List<ControlOrden> listaControlOrden = controlOrdenRepository.findAll();
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        List<MontoTotalOrdenDto> listaMonto =  ordenRepository.obtenerMontoTotalConDto(idAgente);

        model.addAttribute("ordenesLista", ordenesLista);
        model.addAttribute("listaControlOrden",listaControlOrden);
        model.addAttribute("listaEstadoOrden",listaEstadoOrden);
        model.addAttribute("listaMonto",listaMonto);

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
                                @RequestParam(value = "idControl", defaultValue = "0") Integer idControl) {
        //Asumiendo que somos el agente con id 13 (esto se cambiará luego con login y session)
        Integer idAgente = 13;


        List<Orden> ordenesLista;
        List<MontoTotalOrdenDto> listaMonto;

        if (idEstado == 0 && idControl == 0) {
            ordenesLista = ordenRepository.buscarMisOrdenesYOrdenesSinAsignar(idAgente);
            listaMonto = ordenRepository.obtenerMontoTotalMisOrdenesYOrdenesSinAsignar(idAgente);
        }
        else if (idEstado > 0 && idControl > 0) {
            ordenesLista = ordenRepository.findOrdenesByEstadoAndControl(idEstado, idControl,idAgente);
            listaMonto = ordenRepository.obtenerMontoTotalDeOrdenesByEstadoAndControl(idEstado, idControl,idAgente);
        }
        else if (idEstado > 0 && idControl == 0 ) {
            ordenesLista = ordenRepository.findOrdenesByEstado(idEstado,idAgente);
            listaMonto = ordenRepository.obtenerMontoTotalDeOrdenesByEstado(idEstado,idAgente);
        }
        else if (idControl > 0&& idEstado == 0) {
            if(idControl==1){
                ordenesLista = ordenRepository.findOrdenesSinAsignar(idControl);
                listaMonto = ordenRepository.obtenerMontoTotalOrdenesSinAsignar(idAgente);
            }else{
                ordenesLista = ordenRepository.findOrdenesByControl(idControl,idAgente);
                listaMonto = ordenRepository.obtenerMontoTotalDeOrdenesByControl(idControl,idAgente);
            }

        } else {
            ordenesLista = ordenRepository.buscarMisOrdenesYOrdenesSinAsignar(idAgente);
            listaMonto = ordenRepository.obtenerMontoTotalMisOrdenesYOrdenesSinAsignar(idAgente);
        }

        List<ControlOrden> listaControlOrden = controlOrdenRepository.findAll();
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        model.addAttribute("ordenesLista", ordenesLista);
        model.addAttribute("listaControlOrden",listaControlOrden);
        model.addAttribute("listaEstadoOrden",listaEstadoOrden);
        model.addAttribute("idEstado",idEstado);
        model.addAttribute("idControl",idControl);
        model.addAttribute("listaMonto",listaMonto);

        return "Agente/OrdenesDeUsuario/ordeneslista";
    }




    @GetMapping({"Agente/Ordenes/Usuario"})
    public String OrdenesUsuario(@RequestParam("idUsuario") Integer idUsuario,Model model){
        List<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoConDto(idUsuario);
        Optional<Usuario> usr = usuarioRepository.findById(idUsuario);
        if (ordenCarrito == null) {
            ordenCarrito = new ArrayList<>();
        }
        model.addAttribute("ordenCarrito",ordenCarrito);
        model.addAttribute("usuario",usr.get());
        return "Agente/OrdenesDeUsuario/ordenesDeUsuario";
    }

    @GetMapping({"Agente/Ordenes/Usuario/Lista"})
    public String OrdenesUsuarioLista(@RequestParam("idUsuario") Integer idUsuario,Model model){
        List<OrdenCarritoDto> ordenCarrito = ordenRepository.obtenerCarritoConDto(idUsuario);
        Optional<Usuario> usr = usuarioRepository.findById(idUsuario);

        if (ordenCarrito == null) {
            ordenCarrito = new ArrayList<>();
        }
        model.addAttribute("ordenCarrito",ordenCarrito);
        model.addAttribute("usuario",usr.get());
        return "Agente/OrdenesDeUsuario/ordenesDeUsuarioLista";
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
    public String detalleOrdenSinAsignar(@RequestParam("idOrden") Integer idOrden,@RequestParam("numOrden") Integer numOrden,Model model,@RequestParam("indicadorVistaARegresar") Integer indicadorVistaARegresar){
        List<Distrito> listaDistritos   = distritoRepository.findAll();
        List<ControlOrden> listaControlOrden = controlOrdenRepository.findAll();
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);
        
        if(ordenOpt.isPresent()){
            model.addAttribute("orden",ordenOpt.get());
            model.addAttribute("listaDistritos",listaDistritos);
            model.addAttribute("numOrden",numOrden);
            model.addAttribute("listaControlOrden",listaControlOrden);
            model.addAttribute("listaEstadoOrden",listaEstadoOrden);
            model.addAttribute("indicadorVistaARegresar",indicadorVistaARegresar);

            return "Agente/OrdenesDeUsuario/detalleOrdenSinAsignar";

        }else{
            return "Agente/Ordenes";
        }


    }

    @GetMapping({"Agente/Ordenes/Detalles"})
    public String DetalleOrden(@RequestParam("idOrden") Integer idOrden,@RequestParam("numOrden") Integer numOrden,Model model,@RequestParam("indicadorVistaARegresar") Integer indicadorVistaARegresar){
        List<Distrito> listaDistritos   = distritoRepository.findAll();
        List<ControlOrden> listaControlOrden = controlOrdenRepository.findAll();
        List<Estadoorden> listaEstadoOrden = estadoOrdenRepository.findAll();
        Optional<Orden> ordenOpt = ordenRepository.findById(idOrden);

        if(ordenOpt.isPresent()){
            model.addAttribute("orden",ordenOpt.get());
            model.addAttribute("listaDistritos",listaDistritos);
            model.addAttribute("numOrden",numOrden);
            model.addAttribute("listaControlOrden",listaControlOrden);
            model.addAttribute("listaEstadoOrden",listaEstadoOrden);
            model.addAttribute("indicadorVistaARegresar",indicadorVistaARegresar);

            return "Agente/OrdenesDeUsuario/detalleOrden";

        }else{
            return "Agente/Ordenes";
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

