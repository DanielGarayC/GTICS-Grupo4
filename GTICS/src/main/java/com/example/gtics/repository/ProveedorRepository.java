package com.example.gtics.repository;

import com.example.gtics.dto.FindProveedoresMasSolicitados;
import com.example.gtics.dto.FindProveeodresBaneados;
import com.example.gtics.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    //Proveedores más solicitados LISTO
    @Query(value ="SELECT p.nombreProveedor, SUM(CAST(prod.cantVentas AS SIGNED)) as totalVentas\n" +
            "FROM proveedor p\n" +
            "         JOIN producto prod ON p.idProveedor = prod.idProveedor\n" +
            "GROUP BY p.nombreProveedor\n" +
            "ORDER BY totalVentas DESC;", nativeQuery = true)
    List<FindProveedoresMasSolicitados> findProveedoresMasSolicitados();

    // Cantidad de proveedores baneados LISTO
    @Query(value = "SELECT COUNT(p.idProveedor) as cantBaneados FROM proveedor p WHERE p.baneado = 1", nativeQuery = true)
    FindProveeodresBaneados countProveedoresBaneados();

    @Query(value = "SELECT p.nombreProveedor, AVG(c.opcionesCalidad) as promedioCalidad FROM Proveedor p\n" +
            "          JOIN Producto prod ON p.idProveedor = prod.idProveedor\n" +
            "        JOIN Resena r ON r.idProducto = prod.idProducto\n" +
            "      JOIN Calidad c ON r.idCalidad = c.idCalidad\n" +
            "     GROUP BY p.nombreProveedor ORDER BY promedioCalidad ASC", nativeQuery = true)
    List<FindProveedoresMasSolicitados> findProveedoresPorCalidadASC();
}
