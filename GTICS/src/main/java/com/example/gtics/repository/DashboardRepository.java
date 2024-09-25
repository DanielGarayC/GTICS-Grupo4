package com.example.gtics.repository;

import com.example.gtics.entity.Orden;
import com.example.gtics.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Orden, Integer> {

    // Cantidad de usuarios baneados LISTO
    //@Query("SELECT COUNT(u) FROM Usuario u WHERE u.baneado = 1 AND u.rol.id = 4")
    //long countUsuariosBaneados();

    // Cantidad de usuarios activos (no baneados)
    //@Query("SELECT COUNT(u) FROM Usuario u WHERE u.baneado = 0 AND u.rol.id = 4")
    //long countUsuariosActivos();

    // Cantidad de usuarios registrados
    //@Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.id = 4")
    //long countUsuariosRegistrados();

    // Cantidad de proveedores baneados LISTO
   // @Query("SELECT COUNT(p) FROM Proveedor p WHERE p.baneado = 1")
    //long countProveedoresBaneados(Integer idRolProveedor);

    // Top 10 productos más importantes LISTO
   // @Query("SELECT p FROM Producto p ORDER BY p.cantVentas DESC")
    //List<Producto> findTop10Productos();

    // Proveedores más solicitados LISTO
    //@Query("SELECT p.nombreProveedor, SUM(CAST(prod.cantVentas AS INT)) as totalVentas FROM Proveedor p " +
      //      "JOIN Producto prod ON p.id = prod.idProveedor.id " +
        //    "GROUP BY p.nombreProveedor ORDER BY totalVentas DESC")
    //List<Object[]> findProveedoresMasSolicitados();


    // Proveedores con peores reseñas LISTO
    //@Query("SELECT p.nombreProveedor, AVG(c.opcionesCalidad) as promedioCalidad FROM Proveedor p " +
      //      "JOIN Producto prod ON p.id = prod.idProveedor.id " +
        //    "JOIN Resena r ON r.Producto.id = prod.id " +
          //  "JOIN Calidad c ON r.idCalidad.id = c.id " +
           // "GROUP BY p.nombreProveedor ORDER BY promedioCalidad ASC")
    //List<Object[]> findProveedoresPromedioCalidad();

    // Cantidad de órdenes por estado de seguimiento LISTO
    //@Query("SELECT eo.nombreEstado, COUNT(o.id) as totalOrdenes FROM Estadoorden eo " +
      //      "JOIN Orden o ON eo.id = o.EstadoOrden.id GROUP BY eo.nombreEstado")
    //List<Object[]> countOrdenesPorEstado();

    // Cantidad de órdenes por mes LISTO
    //@Query("SELECT MONTH(o.fechaOrden) as mes, COUNT(o.id) as totalOrdenes FROM Orden o " +
      //      "GROUP BY MONTH(o.fechaOrden)")
    //List<Object[]> countOrdenesPorMes();

    //Cantidad de agentes LISTO
    //@Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.id = 3")
    //long countAgentes();
}
