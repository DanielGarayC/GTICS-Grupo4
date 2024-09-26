package com.example.gtics.repository;

import com.example.gtics.entity.ProductoZona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoZonaRepository extends JpaRepository<ProductoZona, Integer> {

    List<ProductoZona> findByProductoId(Integer productoId);
    Optional<ProductoZona> findByProductoIdAndZonaId(Integer productoId, Integer zonaId);

}