
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

    @Query(value = "SELECT MONTH(o.fechaOrden) as mes, COUNT(o.idOrden) as totalOrdenes FROM Orden o\n" +
            "GROUP BY MONTH(o.fechaOrden)", nativeQuery = true)
    List<FindCantOrdenesPorMes> getOrdenesMes();

    @Query(value="SELECT eo.nombreEstado, COUNT(o.idOrden) as totalOrdenes FROM Estadoorden eo\n" +
            "JOIN Orden o ON eo.idEstadoOrden = o.idEstadoOrden GROUP BY eo.nombreEstado", nativeQuery = true)
    List<FindCantOrdenesPorEstado> getOrdenesEstado();
}
