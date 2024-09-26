package com.example.gtics.repository;

import com.example.gtics.dto.FindProveedoresMasSolicitados;
import com.example.gtics.dto.FindProveedoresPorCalidad;
import com.example.gtics.dto.FindProveeodresBaneados;
import com.example.gtics.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    //Proveedores más solicitados LISTO
    @Query(value ="SELECT CONCAT(p.nombreProveedor, ' ', p.apellidoProveedor) AS nombreCompleto,\n" +
            "       SUM(prod.cantVentas) AS totalVentas\n" +
            "FROM proveedor p\n" +
            "         JOIN producto prod ON p.idProveedor = prod.idProveedor\n" +
            "GROUP BY p.nombreProveedor, p.apellidoProveedor\n" +
            "ORDER BY totalVentas DESC\n" +
            "LIMIT 5;", nativeQuery = true)
    List<FindProveedoresMasSolicitados> findProveedoresMasSolicitados();

    // Cantidad de proveedores baneados LISTO
    @Query(value = "SELECT COUNT(p.idProveedor) as cantBaneados FROM proveedor p WHERE p.baneado = 1", nativeQuery = true)
    FindProveeodresBaneados countProveedoresBaneados();

    @Query(value = "SELECT CONCAT(p.nombreProveedor, ' ', p.apellidoProveedor) AS nombreCompleto, AVG(c.idcalidad) AS promedioCalidad\n" +
            "FROM Proveedor p\n" +
            "         JOIN Producto prod ON p.idProveedor = prod.idProveedor\n" +
            "         JOIN Resena r ON r.idProducto = prod.idProducto\n" +
            "         JOIN Calidad c ON r.idCalidad = c.idCalidad\n" +
            "GROUP BY nombreCompleto\n" +
            "ORDER BY promedioCalidad ASC\n" +
            "LIMIT 5;", nativeQuery = true)
    List<FindProveedoresPorCalidad> findProveedoresPorCalidadASC();
}
