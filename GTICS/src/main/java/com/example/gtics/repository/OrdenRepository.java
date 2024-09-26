
package com.example.gtics.repository;

import com.example.gtics.dto.FindCantOrdenesPorEstado;
import com.example.gtics.dto.FindCantOrdenesPorMes;
import com.example.gtics.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
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
            "         LEFT JOIN Orden o ON MONTH(o.fechaOrden) = meses.mes\n" +
            "    AND o.fechaOrden >= CURDATE() - INTERVAL 6 MONTH\n" +
            "GROUP BY meses.mes\n" +
            "ORDER BY meses.mes;", nativeQuery = true)
    List<FindCantOrdenesPorMes> getOrdenesMes();

    @Query(value="SELECT eo.nombreEstado, COUNT(o.idOrden) AS totalOrdenes\n" +
            "FROM Estadoorden eo\n" +
            "         LEFT JOIN Orden o ON eo.idEstadoOrden = o.idEstadoOrden\n" +
            "GROUP BY eo.nombreEstado;", nativeQuery = true)
    List<FindCantOrdenesPorEstado> getOrdenesEstado();
}
