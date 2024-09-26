package com.example.gtics.repository;

import com.example.gtics.entity.Solicitudagente;
import org.springframework.stereotype.Repository;
import com.example.gtics.dto.CantidadProductos;
import com.example.gtics.dto.ProductoRelevanteDTO;
import com.example.gtics.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SolicitudAgenteRepository extends JpaRepository<Solicitudagente, Integer>{
    Solicitudagente findTopByOrderByIdDesc();



}
