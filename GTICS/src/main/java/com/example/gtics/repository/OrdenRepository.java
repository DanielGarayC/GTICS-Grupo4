
package com.example.gtics.repository;

import com.example.gtics.dto.FindCantOrdenesPorEstado;
import com.example.gtics.dto.FindCantOrdenesPorMes;
import com.example.gtics.dto.MontoTotalOrdenDto;
import com.example.gtics.dto.OrdenCarritoDto;
import com.example.gtics.entity.Orden;
import jakarta.transaction.Transactional;
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

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE idEstadoOrden = ?1 AND idControlOrden = ?2 AND idAgente = ?3")
    List<Orden> findOrdenesByEstadoAndControl(Integer idEstado, Integer idControl,Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE idEstadoOrden = ?1 AND idAgente = ?2")
    List<Orden> findOrdenesByEstado(Integer idEstado,Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE idControlOrden = ?1 AND idAgente = ?2")
    List<Orden> findOrdenesByControl(Integer idControl,Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE idControlOrden = ?1 ")
    List<Orden> findOrdenesSinAsignar(Integer idControl);

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE idAgente = ?1 or idControlOrden = 1 ")
    List<Orden> buscarMisOrdenesYOrdenesSinAsignar(Integer idAgente);

    @Transactional
    @Modifying
    @Query(value = "UPDATE orden\n" +
            "SET idControlOrden = 2, idAgente = ?1\n" +
            "WHERE idOrden = ?2;\n",nativeQuery = true)
    void autoAsignarOrden(Integer idAgente,Integer idOrden);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "o.idOrden, \n" +
            "    o.fechaOrden, \n" +
            "    SUM(p.precio) AS montoTotal, \n" +
            "    eo.nombreEstado AS estadoOrden, \n" +
            "    co.nombreControl AS controlOrden\n" +
            "FROM usuario u\n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario\n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra\n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra\n" +
            "JOIN producto p ON phc.idProducto = p.idProducto\n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden\n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden\n" +
            "WHERE u.idUsuario = ?1\n" +
            "GROUP BY o.idOrden;")
    List <OrdenCarritoDto> obtenerCarritoConDto(Integer idUsuario);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden,\n" +
            "    SUM(p.precio) AS montoTotal\n" +
            "FROM usuario u\n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario\n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra\n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra\n" +
            "JOIN producto p ON phc.idProducto = p.idProducto\n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden\n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden\n" +
            "WHERE o.idAgente = ?1 or o.idControlOrden=1\n" +
            "GROUP BY o.idOrden\n" +
            "order by o.idOrden;")
    List <MontoTotalOrdenDto> obtenerMontoTotalConDto(Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden,\n" +
            "    SUM(p.precio) AS montoTotal\n" +
            "FROM usuario u\n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario\n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra\n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra\n" +
            "JOIN producto p ON phc.idProducto = p.idProducto\n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden\n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden\n" +
            "WHERE o.idAgente = ?3 AND o.idControlOrden = ?2 AND o.idEstadoOrden = ?1\n" +
            "GROUP BY o.idOrden\n" +
            "order by o.idOrden;")
    List <MontoTotalOrdenDto> obtenerMontoTotalDeOrdenesByEstadoAndControl(Integer idEstado, Integer idControl,Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden,\n" +
            "    SUM(p.precio) AS montoTotal\n" +
            "FROM usuario u\n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario\n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra\n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra\n" +
            "JOIN producto p ON phc.idProducto = p.idProducto\n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden\n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden\n" +
            "WHERE o.idAgente = ?2 AND o.idEstadoOrden = ?1\n" +
            "GROUP BY o.idOrden\n" +
            "order by o.idOrden;")
    List<MontoTotalOrdenDto> obtenerMontoTotalDeOrdenesByEstado(Integer idEstado,Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden,\n" +
            "    SUM(p.precio) AS montoTotal\n" +
            "FROM usuario u\n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario\n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra\n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra\n" +
            "JOIN producto p ON phc.idProducto = p.idProducto\n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden\n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden\n" +
            "WHERE o.idAgente = ?2 AND o.idControlOrden =?1\n" +
            "GROUP BY o.idOrden\n" +
            "order by o.idOrden;")
    List<MontoTotalOrdenDto> obtenerMontoTotalDeOrdenesByControl(Integer idControl,Integer idAgente);

    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden,\n" +
            "    SUM(p.precio) AS montoTotal\n" +
            "FROM usuario u\n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario\n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra\n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra\n" +
            "JOIN producto p ON phc.idProducto = p.idProducto\n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden\n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden\n" +
            "WHERE o.idAgente = ?1 or o.idControlOrden =1\n" +
            "GROUP BY o.idOrden\n" +
            "order by o.idOrden;")
    List<MontoTotalOrdenDto> obtenerMontoTotalMisOrdenesYOrdenesSinAsignar(Integer idAgente);



    @Query(nativeQuery = true, value = "SELECT \n" +
            "    o.idOrden,\n" +
            "    SUM(p.precio) AS montoTotal\n" +
            "FROM usuario u\n" +
            "JOIN carritocompra c ON u.idUsuario = c.idUsuario\n" +
            "JOIN orden o ON c.idCarritoCompra = o.idCarritoCompra\n" +
            "JOIN producto_has_carritocompra phc ON c.idCarritoCompra = phc.idCarritoCompra\n" +
            "JOIN producto p ON phc.idProducto = p.idProducto\n" +
            "JOIN estadoorden eo ON o.idEstadoOrden = eo.idEstadoOrden\n" +
            "JOIN controlorden co ON o.idControlOrden = co.idControlOrden\n" +
            "WHERE o.idControlOrden = 1\n" +
            "GROUP BY o.idOrden\n" +
            "order by o.idOrden; ")
    List<MontoTotalOrdenDto> obtenerMontoTotalOrdenesSinAsignar(Integer idControl);
}
