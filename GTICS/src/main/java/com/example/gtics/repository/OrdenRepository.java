
package com.example.gtics.repository;

import com.example.gtics.dto.*;
import com.example.gtics.entity.Carritocompra;
import com.example.gtics.entity.Orden;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.gtics.entity.Producto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            "    eo.idEstadoOrden AS estadoOrden, \n" +
            "    co.idControlOrden AS controlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno,\n" +
            "    (SELECT phc.idProducto \n" +
            "     FROM producto_has_carritocompra phc \n" +
            "     WHERE phc.idCarritoCompra = o.idCarritoCompra \n" +
            "     ORDER BY phc.idProducto ASC LIMIT 1) AS primerIdProducto  -- Subconsulta para obtener el primer idProducto de la orden\n" +
            "FROM \n" +
            "    usuario u \n" +
            "JOIN \n" +
            "    carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN \n" +
            "    orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN \n" +
            "    producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN \n" +
            "    producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN \n" +
            "    estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN \n" +
            "    controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "WHERE \n" +
            "    u.idUsuario = ?1\n" +
            "    AND ordenEliminada = 0\n" +
            "GROUP BY \n" +
            "    o.idOrden;\n")
    Page<OrdenCarritoDto> obtenerCarritoConDto(Integer idUsuario, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    o.fechaLlegada, \n" +
            "    SUM(phc.cantidadProducto * p.precio + COALESCE(o.costosAdicionales, 0.00)) + MAX(p.costoEnvio) AS montoTotal,  -- Cálculo del monto total con máximo costo de envío\n" +
            "    eo.idEstadoOrden AS estadoOrden, \n" +
            "    co.idControlOrden AS controlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno, \n" +
            "    o.solicitarCancelarOrden, \n" +
            "    o.ordenEliminada, \n" +
            "    o.idAgente \n" +
            "FROM usuario u \n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "WHERE u.idUsuario = ?1 and ordenEliminada=0 \n" +
            "GROUP BY o.idOrden;")
    Page<OrdenCarritoDto> obtenerCarritoUFConDto(Integer idUsuario, Pageable pageable);
    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio + COALESCE(o.costosAdicionales, 0.00)) + MAX(p.costoEnvio) AS montoTotal,  -- Cálculo del monto total con máximo costo de envío\n" +
            "    eo.idEstadoOrden AS estadoOrden, \n" +
            "    co.idControlOrden AS controlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno, \n" +
            "    o.solicitarCancelarOrden, \n" +
            "    o.ordenEliminada, \n" +
            "    o.idAgente \n" +
            "FROM usuario u \n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "WHERE u.idUsuario = ?1 and ordenEliminada=0 and o.idEstadoOrden=?2 \n" +
            "GROUP BY o.idOrden;")
    Page<OrdenCarritoDto> obtenerCarritoUFConDtoFiltro(Integer idUsuario,Integer idEstadoOrden, Pageable pageable);

    @Query(value = "SELECT \n" +
            "     p.idProducto, \n" +
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

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio + COALESCE(o.costosAdicionales, 0.00)) + MAX(p.costoEnvio) AS montoTotal,  \n" +
            "    eo.idEstadoOrden AS estadoOrden, \n" +
            "    co.idControlOrden AS controlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno,\n" +
            "    (SELECT phc2.idProducto \n" +
            "     FROM producto_has_carritocompra phc2 \n" +
            "     WHERE phc2.idCarritoCompra = o.idCarritoCompra \n" +
            "     ORDER BY phc2.idProducto ASC LIMIT 1) AS primerIdProducto\n" +
            "FROM \n" +
            "    usuario u \n" +
            "JOIN \n" +
            "    carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN \n" +
            "    orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN \n" +
            "    producto_has_carritocompra phc ON o.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN \n" +
            "    producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN \n" +
            "    estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN \n" +
            "    controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "WHERE \n" +
            "    o.idControlOrden = 1\n" +
            "GROUP BY \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    eo.idEstadoOrden, \n" +
            "    co.idControlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno\n" +
            "ORDER BY \n" +
            "    o.idOrden DESC\n" +
            "LIMIT 5;\n")
    List<OrdenCarritoDto> ultimasOrdenesSinAsignar();

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio + COALESCE(o.costosAdicionales, 0.00)) + MAX(p.costoEnvio) AS montoTotal,  \n" +
            "    eo.idEstadoOrden AS estadoOrden, \n" +
            "    co.idControlOrden AS controlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno,\n" +
            "    (SELECT phc2.idProducto \n" +
            "     FROM producto_has_carritocompra phc2 \n" +
            "     WHERE phc2.idCarritoCompra = o.idCarritoCompra \n" +
            "     ORDER BY phc2.idProducto ASC LIMIT 1) AS primerIdProducto\n" +
            "FROM \n" +
            "    usuario u \n" +
            "JOIN \n" +
            "    carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN \n" +
            "    orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN \n" +
            "    producto_has_carritocompra phc ON o.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN \n" +
            "    producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN \n" +
            "    estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN \n" +
            "    controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "where o.idControlOrden=2 and o.idAgente=?1\n" +
            "GROUP BY \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    eo.idEstadoOrden, \n" +
            "    co.idControlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno\n" +
            "ORDER BY \n" +
            "    o.idOrden DESC\n" +
            "LIMIT 5;\n")
    List<OrdenCarritoDto> ultimasOrdenesPendientes(Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio + COALESCE(o.costosAdicionales, 0.00)) + MAX(p.costoEnvio) AS montoTotal,  \n" +
            "    eo.idEstadoOrden AS estadoOrden, \n" +
            "    co.idControlOrden AS controlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno,\n" +
            "    (SELECT phc2.idProducto \n" +
            "     FROM producto_has_carritocompra phc2 \n" +
            "     WHERE phc2.idCarritoCompra = o.idCarritoCompra \n" +
            "     ORDER BY phc2.idProducto ASC LIMIT 1) AS primerIdProducto\n" +
            "FROM \n" +
            "    usuario u \n" +
            "JOIN \n" +
            "    carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN \n" +
            "    orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN \n" +
            "    producto_has_carritocompra phc ON o.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN \n" +
            "    producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN \n" +
            "    estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN \n" +
            "    controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "where o.idControlOrden=3 and o.idAgente=?1\n" +
            "GROUP BY \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    eo.idEstadoOrden, \n" +
            "    co.idControlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno\n" +
            "ORDER BY \n" +
            "    o.idOrden DESC\n" +
            "LIMIT 5;\n")
    List<OrdenCarritoDto> ultimasOrdenesenProceso(Integer idAgente);
    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    SUM(phc.cantidadProducto * p.precio + COALESCE(o.costosAdicionales, 0.00)) + MAX(p.costoEnvio) AS montoTotal,  \n" +
            "    eo.idEstadoOrden AS estadoOrden, \n" +
            "    co.idControlOrden AS controlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno,\n" +
            "    (SELECT phc2.idProducto \n" +
            "     FROM producto_has_carritocompra phc2 \n" +
            "     WHERE phc2.idCarritoCompra = o.idCarritoCompra \n" +
            "     ORDER BY phc2.idProducto ASC LIMIT 1) AS primerIdProducto\n" +
            "FROM \n" +
            "    usuario u \n" +
            "JOIN \n" +
            "    carritocompra c ON u.idUsuario = c.idUsuario \n" +
            "JOIN \n" +
            "    orden o ON c.idCarritoCompra = o.idCarritoCompra \n" +
            "JOIN \n" +
            "    producto_has_carritocompra phc ON o.idCarritoCompra = phc.idCarritoCompra \n" +
            "JOIN \n" +
            "    producto p ON phc.idProducto = p.idProducto \n" +
            "JOIN \n" +
            "    estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden \n" +
            "JOIN \n" +
            "    controlorden co ON o.idControlOrden = co.idControlOrden \n" +
            "where o.idControlOrden=4 and o.idAgente=?1\n" +
            "GROUP BY \n" +
            "    o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    eo.idEstadoOrden, \n" +
            "    co.idControlOrden, \n" +
            "    u.nombre, \n" +
            "    u.apellidoPaterno\n" +
            "ORDER BY \n" +
            "    o.idOrden DESC\n" +
            "LIMIT 5;\n")
    List<OrdenCarritoDto> ultimasOrdenesResueltas(Integer idAgente);


    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE orden SET ordenEliminada=1, razonEliminacion = ?2 WHERE (idOrden = ?1);")
    void eliminadoLogicoDeOrden(Integer idOrden,String razonEliminacion);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE usuario SET direccion = ?2,idDistrito = ?3 WHERE idUsuario= ?1")
    void actualizarOrdenParaUsuarioFinal(Integer idUsuario,String direccion, Integer idDistrito);
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE orden SET solicitarCancelarOrden=1 WHERE (idOrden = ?1);")
    void solicitarEliminarOrden(Integer idOrden);
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE `gticsdb`.`orden` SET `idAgente` = ?2 WHERE (`idOrden` = ?1);")
    void solicitarUnAgenteparaOrden(Integer idOrden,Integer idAgenteRandom);

    @Query(nativeQuery = true, value = "SELECT u.idUsuario FROM orden o\n" +
            "JOIN carritocompra cp ON cp.idCarritoCompra = o.idCarritoCompra\n" +
            "JOIN usuario u ON u.idUsuario = cp.idUsuario\n" +
            "where idOrden = ?1 ")
    Integer obtenerUsuarioDuenoDeOrden(Integer idOrden);
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE `gticsdb`.`usuario` SET `idAgente` = ?1 WHERE (`idUsuario` = ?2);\n")
    void solicitarUnAgenteParaUsuario(Integer idAgenteRandom,Integer idUsuarioDuenoOrden);

    @Query(nativeQuery = true, value = "SELECT p.idProducto AS idProducto, " +
            "p.nombreProducto AS nombreProducto, " +
            "phc.cantidadProducto AS cantidadProducto, " +
            "p.precio AS precioUnidad, " +
            "(phc.cantidadProducto * p.precio) AS precioTotalPorProducto, " +
            "fp.foto AS urlImagenProducto " +
            "FROM carritocompra c " +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra " +
            "JOIN producto p ON phc.idProducto = p.idProducto " +
            "LEFT JOIN fotosproducto fp ON p.idProducto = fp.idProducto " +
            "WHERE c.idUsuario = :idUsuario AND c.activo = 1")
    List<ProductosCarritoDto> obtenerProductosPorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT o FROM Orden o WHERE o.idCarritoCompra = :carrito")
    Optional<Orden> findByIdCarritoCompra(@Param("carrito") Carritocompra carrito);

    @Query(nativeQuery = true, value = "SELECT p.idProducto, p.nombreProducto " +
            "FROM orden o " +
            "JOIN carritocompra c ON o.idCarritoCompra = c.idCarritoCompra " +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra " +
            "JOIN producto p ON phc.idProducto = p.idProducto " +
            "WHERE c.idUsuario = :idUsuario " +  // Aquí usas el parámetro dinámico
            "AND o.idEstadoOrden = 7 " +
            "AND phc.resenaCreada = 0")
    List<ProductoDTO> obtenerProductosPorUsuarioSinResena(@Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT * FROM orden WHERE fechaOrden BETWEEN :fechaInicio AND :fechaFin ORDER BY fechaOrden ASC", nativeQuery = true)
    List<Orden> findOrdenesByFechaOrdenBetween(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);


    List<Orden> findByIdAgente_Id(Integer idAgente);
    // Nuevo método para encontrar órdenes asignadas a una lista de agentes
    // Método corregido
    List<Orden> findByIdAgente_IdIn(List<Integer> idAgentes);

    @Query(nativeQuery = true, value = "SELECT u.idZona FROM orden o\n" +
            "JOIN carritocompra cp ON cp.idCarritoCompra = o.idCarritoCompra\n" +
            "JOIN usuario u ON u.idUsuario = cp.idUsuario\n" +
            "where idOrden = ?1 ")
    Integer obtenerZonaDeUsuarioDuenoDeOrden(Integer idOrden);

    @Query(nativeQuery = true, value = "SELECT * FROM usuario where idRol=3 and activo=1 limit 1; ")
    Integer obtenerAgenteRandom();

    @Query(nativeQuery = true, value = "SELECT * FROM usuario where idZona=?1 and idRol=3 limit 1; ")
    Integer obtenerAgenteRandomDeSuZona(Integer zonaUsuario);

}
