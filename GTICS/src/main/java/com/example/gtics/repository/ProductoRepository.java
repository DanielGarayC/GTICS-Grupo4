package com.example.gtics.repository;

import com.example.gtics.dto.CantidadProductos;
import com.example.gtics.dto.ProductoRelevanteDTO;
import com.example.gtics.dto.ProductoTabla;
import com.example.gtics.dto.ProductosCarritoDto;
import com.example.gtics.entity.Producto;
import com.example.gtics.entity.Zona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Query(value = "SELECT p.idProducto, p.nombreProducto, p.cantVentas FROM producto p where p.zona_idZona = ?1 ORDER BY p.cantVentas DESC LIMIT 10", nativeQuery = true)
    List<ProductoRelevanteDTO> findProductosRelevantesZona(int idZona);

    // Consulta nativa para obtener la cantidad total de ventas
    @Query(value = "SELECT SUM(p.cantVentas) AS totalVentas FROM producto p", nativeQuery = true)
    CantidadProductos getCantidadProductos();

    @Query(value = "SELECT SUM(p.cantVentas) AS totalVentas FROM producto p where zona_idZona = ?1", nativeQuery = true)
    CantidadProductos getCantidadProductosZona(int idZona);

    @Query(value = "SELECT * FROM producto where zona_idZona = ?1\n" +
            "            ORDER BY CAST(cantidadDisponible AS UNSIGNED) ASC LIMIT 3", nativeQuery = true)
    List<Producto> prductosPocoStockZona(int idZona);

    @Query(value = "SELECT\n" +
            "                p.idProducto,\n" +
            "                p.nombreProducto,\n" +
            "                p.cantidadDisponible,\n" +
            "                p.fechaArribo,\n" +
            "                CASE\n" +
            "                    WHEN sr.cantidadSolicitada = 0 THEN NULL\n" +
            "                    ELSE sr.cantidadSolicitada\n" +
            "                END AS cantidadSolicitada,\n" +
            "                (SELECT fp.foto\n" +
            "                 FROM gticsdb.fotosproducto fp\n" +
            "                 WHERE fp.idProducto = p.idProducto\n" +
            "\n" +
            "                 LIMIT 1) AS primeraFoto\n" +
            "            FROM\n" +
            "                gticsdb.producto p\n" +
            "            LEFT JOIN\n" +
            "                gticsdb.solicitudreposicion sr\n" +
            "            ON\n" +
            "                p.idProducto = sr.idProducto;", nativeQuery = true)
    List<ProductoTabla> getProductosTabla();

    @Query(value = "SELECT\n" +
            "                p.idProducto,\n" +
            "                p.nombreProducto,\n" +
            "                p.cantidadDisponible,\n" +
            "                p.fechaArribo,\n" +
            "                CASE\n" +
            "                    WHEN sr.cantidadSolicitada = 0 THEN NULL\n" +
            "                    ELSE sr.cantidadSolicitada\n" +
            "                END AS cantidadSolicitada,\n" +
            "                (SELECT fp.foto\n" +
            "                 FROM gticsdb.fotosproducto fp\n" +
            "                 WHERE fp.idProducto = p.idProducto\n" +
            "\n" +
            "                 LIMIT 1) AS primeraFoto\n" +
            "            FROM\n" +
            "                gticsdb.producto p\n" +
            "            LEFT JOIN\n" +
            "                gticsdb.solicitudreposicion sr\n" +
            "            ON\n" +
            "                p.idProducto = sr.idProducto\n" +
            "            where p.idProducto = ?1", nativeQuery = true)
    ProductoTabla getProductosTablaId(int id);

    List<Producto> findByNombreProducto(String nombreProducto);
    Optional<Producto> findByNombreProductoAndZona(String nombreProducto, Zona zona);
    @Query(nativeQuery = true, value = "SELECT p.idProducto, p.nombreProducto, phc.cantidadProducto, p.precio AS precioUnidad, (phc.cantidadProducto * p.precio) AS precioTotalPorProducto " +
            "FROM orden o " +
            "JOIN carritocompra c ON o.idCarritoCompra = c.idCarritoCompra " +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra " +
            "JOIN producto p ON phc.idProducto = p.idProducto " +
            "WHERE c.idUsuario = :idUsuario AND o.idEstadoOrden = 8 AND o.ordenEliminada = 0")
    List<ProductosCarritoDto> obtenerProductosPorUsuario(@Param("idUsuario") Integer idUsuario);


}
