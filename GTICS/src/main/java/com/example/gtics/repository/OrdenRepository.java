
package com.example.gtics.repository;

import com.example.gtics.dto.*;
import com.example.gtics.entity.Orden;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Integer> {

    @Query(value = "SELECT\n" +
            "    DATE_FORMAT(DATE(CONCAT(YEAR(CURDATE()), '-', LPAD(meses.mes, 2, '0'), '-01')), '%M') as mes,\n" +
            "    COALESCE(COUNT(o.idOrden), 0) as totalOrdenes\n" +
            "FROM (\n" +
            "         SELECT MONTH(CURDATE() - INTERVAL 5 MONTH) as mes\n" +
            "         UNION SELECT MONTH(CURDATE() - INTERVAL 4 MONTH)\n" +
            "         UNION SELECT MONTH(CURDATE() - INTERVAL 3 MONTH)\n" +
            "         UNION SELECT MONTH(CURDATE() - INTERVAL 2 MONTH)\n" +
            "         UNION SELECT MONTH(CURDATE() - INTERVAL 1 MONTH)\n" +
            "         UNION SELECT MONTH(CURDATE())\n" +
            "     ) meses\n" +
            "         LEFT JOIN orden o ON MONTH(o.fechaOrden) = meses.mes\n" +
            "    AND o.fechaOrden >= CURDATE() - INTERVAL 6 MONTH\n" +
            "GROUP BY meses.mes\n" +
            "ORDER BY meses.mes;", nativeQuery = true)
    List<FindCantOrdenesPorMes> getOrdenesMes();

    @Query(value="SELECT eo.nombreEstado, COUNT(o.idOrden) AS totalOrdenes\n" +
            "FROM estadoorden eo\n" +
            "         LEFT JOIN orden o ON eo.idEstadoOrden = o.idEstadoOrden\n" +
            "GROUP BY eo.nombreEstado;", nativeQuery = true)
    List<FindCantOrdenesPorEstado> getOrdenesEstado();

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE ordenEliminada=0 and  idEstadoOrden = ?1 AND idControlOrden = ?2 AND idAgente = ?3")
    Page<Orden> findOrdenesByEstadoAndControl(Integer idEstado, Integer idControl,Integer idAgente, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE ordenEliminada=0 and idEstadoOrden = ?1 AND idAgente = ?2")
    Page<Orden> findOrdenesByEstado(Integer idEstado,Integer idAgente, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE ordenEliminada=0 and idControlOrden = ?1 AND idAgente = ?2")
    Page<Orden> findOrdenesByControl(Integer idControl,Integer idAgente, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE ordenEliminada=0 and idControlOrden = ?1 ")
    Page<Orden> findOrdenesSinAsignar(Integer idControl, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE ordenEliminada=0 and idAgente = ?1 or idControlOrden = 1 ")
    List<Orden> buscarMisOrdenesYOrdenesSinAsignar(Integer idAgente);


    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE ordenEliminada=0 and idAgente = ?1 or idControlOrden = 1 ")
    Page<Orden> buscarMisOrdenesYOrdenesSinAsignarPage(Integer idAgente, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "UPDATE orden\n" +
            "SET idControlOrden = 2, idAgente = ?1\n" +
            "WHERE idOrden = ?2;\n",nativeQuery = true)
    void autoAsignarOrden(Integer idAgente,Integer idOrden);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio + COALESCE(o.costosAdicionales, 0.00)) + MAX(p.costoEnvio) AS montoTotal,  -- Cálculo del monto total con máximo costo de envío\n" +
            "    eo.nombreEstado AS estadoOrden, \n" +
            "    co.nombreControl AS controlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno \n" +
            "FROM usuario u \n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "WHERE u.idUsuario = :idUsuario and ordenEliminada=0\n" +
            "GROUP BY o.idOrden;")
    Page<OrdenCarritoDto> obtenerCarritoConDto(Integer idUsuario, Pageable pageable);


    @Query(value = "SELECT \n" +
            "    (SELECT fp.foto FROM fotosproducto fp WHERE fp.idProducto = p.idProducto LIMIT 1) AS primeraFotoProducto,\n" +
            "    p.nombreProducto, \n" +
            "    t.nombreTienda, \n" +
            "    p.modelo, \n" +
            "    p.color, \n" +
            "    phc.cantidadProducto, \n" +
            "    p.precio AS precioUnidad, \n" +
            "    (phc.cantidadProducto * p.precio) AS precioTotalPorProducto, \n" +
            "    p.costoEnvio\n" +
            "FROM \n" +
            "    producto_has_carritocompra phc \n" +
            "INNER JOIN \n" +
            "    producto p ON p.idProducto = phc.idProducto \n" +
            "INNER JOIN \n" +
            "    proveedor pr ON pr.idProveedor = p.idProveedor \n" +
            "INNER JOIN \n" +
            "    tienda t ON t.idTienda = pr.idTienda \n" +
            "INNER JOIN \n" +
            "    orden o ON o.idCarritoCompra = phc.idCarritoCompra \n" +
            "WHERE \n" +
            "    o.idOrden = :idOrden", nativeQuery = true)
    List<ProductosxOrden> obtenerProductosPorOrden(Integer idOrden);


    @Query(value = "SELECT o.costosAdicionales FROM gticsdb.orden o WHERE o.idOrden = :idOrden", nativeQuery = true)
    Double obtenerCostoAdicionalxOrden(Integer idOrden);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden AS idOrden,                                       -- ID de la orden\n" +
            "    SUM(phc.cantidadProducto * p.precio) AS subtotal,           -- Subtotal (productos * cantidad)\n" +
            "    MAX(p.costoEnvio) AS maxCostoEnvio,                         -- Mayor costo de envío\n" +
            "    COALESCE(o.costosAdicionales, 0.00) AS costosAdicionales,   -- Reemplaza NULL con 0\n" +
            "    (SUM(phc.cantidadProducto * p.precio) + MAX(p.costoEnvio) + COALESCE(o.costosAdicionales, 0.00)) AS montoTotal  -- Monto total\n" +
            "FROM \n" +
            "    producto_has_carritocompra phc\n" +
            "INNER JOIN \n" +
            "    producto p ON p.idProducto = phc.idProducto\n" +
            "INNER JOIN \n" +
            "    orden o ON o.idCarritoCompra = phc.idCarritoCompra\n" +
            "WHERE \n" +
            "    (o.idAgente = :idAgente OR o.idControlOrden = 1)            -- Condición: asignado o sin asignar\n" +
            "GROUP BY \n" +
            "    o.idOrden;")
    List<MontoTotalOrdenDto> obtenerMontoTotalConDto(Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio) + COALESCE(o.costosAdicionales, 0.00) + MAX(p.costoEnvio) AS montoTotal \n" +
            "FROM usuario u \n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "WHERE o.idAgente = ?3 AND o.idControlOrden = ?2 AND o.idEstadoOrden = ?1 \n" +
            "GROUP BY o.idOrden \n" +
            "ORDER BY o.idOrden;")
    List<MontoTotalOrdenDto> obtenerMontoTotalDeOrdenesByEstadoAndControl(Integer idEstado, Integer idControl, Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio) + COALESCE(o.costosAdicionales, 0.00) + MAX(p.costoEnvio) AS montoTotal \n" +
            "FROM usuario u \n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "WHERE o.idAgente = ?2 AND o.idEstadoOrden = ?1 \n" +
            "GROUP BY o.idOrden \n" +
            "ORDER BY o.idOrden;")
    List<MontoTotalOrdenDto> obtenerMontoTotalDeOrdenesByEstado(Integer idEstado, Integer idAgente);


    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio) + COALESCE(o.costosAdicionales, 0.00) + MAX(p.costoEnvio) AS montoTotal \n" +
            "FROM usuario u \n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "WHERE o.idAgente = ?2 AND o.idControlOrden = ?1 \n" +
            "GROUP BY o.idOrden \n" +
            "ORDER BY o.idOrden;")
    List<MontoTotalOrdenDto> obtenerMontoTotalDeOrdenesByControl(Integer idControl, Integer idAgente);


    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio) + COALESCE(o.costosAdicionales, 0.00) + MAX(p.costoEnvio) AS montoTotal \n" +
            "FROM usuario u \n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "WHERE o.idAgente = ?1 OR o.idControlOrden = 1 \n" +
            "GROUP BY o.idOrden \n" +
            "ORDER BY o.idOrden;")
    List<MontoTotalOrdenDto> obtenerMontoTotalMisOrdenesYOrdenesSinAsignar(Integer idAgente);



    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio) + COALESCE(o.costosAdicionales, 0.00) + MAX(p.costoEnvio) AS montoTotal \n" +
            "FROM usuario u \n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "WHERE o.idControlOrden = 1 \n" +
            "GROUP BY o.idOrden \n" +
            "ORDER BY o.idOrden;")
    List<MontoTotalOrdenDto> obtenerMontoTotalOrdenesSinAsignar(Integer idControl);

    @Query(nativeQuery = true, value = "SELECT * from orden where idControlORden=1 order by idOrden desc limit 5;")
    List<Orden> ultimasOrdenesSinAsignar();

    @Query(nativeQuery = true, value = "SELECT * from orden where idControlOrden=2 and idAgente=?1 order by idOrden desc limit 5;")
    List<Orden> ultimasOrdenesPendientes(Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT * from orden where idControlOrden=3 and idAgente=?1 order by idOrden desc limit 5;")
    List<Orden> ultimasOrdenesenProceso(Integer idAgente);
    @Query(nativeQuery = true, value = "SELECT * from orden where idControlOrden=4 and idAgente=?1 order by idOrden desc limit 5;")
    List<Orden> ultimasOrdenesResueltas(Integer idAgente);


    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE orden SET ordenEliminada=1, razonEliminacion = ?2 WHERE (idOrden = ?1);")
    void eliminadoLogicoDeOrden(Integer idOrden,String razonEliminacion);
}
