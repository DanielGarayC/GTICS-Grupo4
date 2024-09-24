package com.example.gtics.repository;

import com.example.gtics.entity.Orden;
import com.example.gtics.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Orden, Integer> {

    // Cantidad de usuarios baneados
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.baneado = 1")
    long countUsuariosBaneados();

    // Cantidad de usuarios activos (no baneados)
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.baneado = 0")
    long countUsuariosActivos();

    // Cantidad de usuarios registrados
    @Query("SELECT COUNT(u) FROM Usuario u")
    long countUsuariosRegistrados();

    // Cantidad de proveedores baneados
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.baneado = 1 AND u.rol.id = :idRolProveedor")
    long countProveedoresBaneados(Integer idRolProveedor);

    // Top 10 productos más importantes (por cantidad disponible)
    @Query("SELECT p FROM Producto p ORDER BY p.cantidadDisponible DESC")
    List<Producto> findTop10Productos();

    // Proveedores más solicitados (con más productos)
    @Query("SELECT p.nombreProveedor, COUNT(prod.id) as totalProductos FROM Proveedor p " +
            "JOIN Producto prod ON p.id = prod.id " +
            "GROUP BY p.nombreProveedor ORDER BY totalProductos DESC")
    List<Object[]> findProveedoresMasSolicitados();

    // Proveedores con peores comentarios
    @Query("SELECT p.nombreProveedor, COUNT(r.id) as totalResenasNegativas FROM Proveedor p " +
            "JOIN Producto prod ON p.id = prod.idProveedor.id " +
            "JOIN Resena r ON r.Producto.id = prod.id " +
            "WHERE r.opinion LIKE '%negativo%' " +
            "GROUP BY p.nombreProveedor ORDER BY totalResenasNegativas DESC")
    List<Object[]> findProveedoresPeoresComentarios();

    // Cantidad de órdenes por estado de seguimiento
    @Query("SELECT eo.nombreEstado, COUNT(o.id) as totalOrdenes FROM Estadoorden eo " +
            "JOIN Orden o ON eo.id = o.EstadoOrden.id GROUP BY eo.nombreEstado")
    List<Object[]> countOrdenesPorEstado();

    // Cantidad de órdenes por mes (asumiendo que existe un campo fecha en la entidad Orden)
    @Query("SELECT MONTH(o.fechaOrden) as mes, COUNT(o.id) as totalOrdenes FROM Orden o " +
            "GROUP BY MONTH(o.fechaOrden)")
    List<Object[]> countOrdenesPorMes();
}
