package com.example.gtics.repository;

import com.example.gtics.dto.CantidadProductos;
import com.example.gtics.dto.ProductoRelevanteDTO;
import com.example.gtics.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Consulta JPQL, referenciando la clase Producto
    @Query("SELECT p FROM Producto p WHERE p.borrado = 0")
    List<Producto> findAllActive();

    // Consulta nativa para obtener productos relevantes
    @Query(value = "SELECT p.idProducto, p.nombreProducto, p.cantVentas FROM producto p ORDER BY p.cantVentas DESC LIMIT 10", nativeQuery = true)
    List<ProductoRelevanteDTO> findProductosRelevantes();

    // Consulta nativa para obtener la cantidad total de ventas
    @Query(value = "SELECT SUM(p.cantVentas) AS totalVentas FROM producto p", nativeQuery = true)
    CantidadProductos getCantidadProductos();
}
