package com.example.gtics.repository;

import com.example.gtics.entity.Producto;
import com.example.gtics.entity.Solicitudreposicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolicitudreposicionRepository extends JpaRepository<Solicitudreposicion, Integer> {
    Optional<Solicitudreposicion> findByIdProducto(Producto producto);

}
