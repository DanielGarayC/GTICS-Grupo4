package com.example.gtics.repository;

import com.example.gtics.dto.CantidadProductos;
import com.example.gtics.dto.ProductoRelevanteDTO;
import com.example.gtics.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    @Query(value = "SELECT * FROM producto p WHERE p.borrado = 0", nativeQuery = true)
    Page<Producto> findAllActiveConpaginacion(Pageable pageable);
    // Consulta nativa para obtener productos activos
    @Query(value="SELECT * FROM producto p WHERE p.borrado = 0", nativeQuery = true)
    List<Producto> findAllActive();

    // Consulta nativa para obtener productos relevantes
    @Query(value = "SELECT p.idProducto, p.nombreProducto, p.cantVentas FROM producto p ORDER BY p.cantVentas DESC LIMIT 10", nativeQuery = true)
    List<ProductoRelevanteDTO> findProductosRelevantes();

    // Consulta nativa para obtener la cantidad total de ventas
    @Query(value = "SELECT SUM(p.cantVentas) AS totalVentas FROM producto p", nativeQuery = true)
    CantidadProductos getCantidadProductos();
}
