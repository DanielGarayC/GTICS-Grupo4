package com.example.gtics.repository;

import com.example.gtics.dto.ProductoRelevanteDTO;
import com.example.gtics.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Query(value ="SELECT p.idProducto, p.nombreProducto, p.cantVentas FROM Producto p ORDER BY p.cantVentas DESC LIMIT 10", nativeQuery = true)
    List<ProductoRelevanteDTO> findProductosRelevantes();
}
