package com.example.gtics.repository;

import com.example.gtics.dto.CantidadProductos;
import com.example.gtics.dto.ProductoRelevanteDTO;
import com.example.gtics.dto.ProductoTabla;
import com.example.gtics.entity.Producto;
import com.example.gtics.entity.Zona;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query(value = "SELECT p.idProducto, p.nombreProducto, p.cantidadDisponible, p.fechaArribo, " +
            "CASE WHEN sr.cantidadSolicitada = 0 THEN NULL ELSE sr.cantidadSolicitada END AS cantidadSolicitada, " +
            "(SELECT fp.foto FROM gticsdb.fotosproducto fp WHERE fp.idProducto = p.idProducto LIMIT 1) AS primeraFoto " +
            "FROM gticsdb.producto p " +
            "LEFT JOIN gticsdb.solicitudreposicion sr ON p.idProducto = sr.idProducto",
            nativeQuery = true)
    Page<ProductoTabla> getProductosTablaConPaginacion(Pageable pageable);

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

    Optional<Producto> findByNombreProductoAndZona(String nombreProducto, Zona zona);

    @Query(value = "SELECT COUNT(*) FROM producto WHERE idCategoria = :idCategoria", nativeQuery = true)
    long contarProductosPorCategoria(@Param("idCategoria") Integer idCategoria);

    @Query(value = "SELECT * FROM producto WHERE idCategoria = :categoriaId AND borrado = 0", nativeQuery = true)
    List<Producto> findProductosPorCategoria(@Param("categoriaId") Integer categoriaId);

    @Query(value = "SELECT * FROM producto WHERE idCategoria = :categoriaId AND borrado = 0", nativeQuery = true)
    Page<Producto> findProductosPorCategoriaConPaginacion(@Param("categoriaId") Integer categoriaId, Pageable pageable);

    @Query(value = "SELECT * FROM producto WHERE idCategoria = :categoriaId AND borrado = 0 ORDER BY CASE WHEN :sort = 'asc' THEN precio END ASC, CASE WHEN :sort = 'desc' THEN precio END DESC",
            countQuery = "SELECT count(*) FROM producto WHERE idCategoria = :categoriaId AND borrado = 0",
            nativeQuery = true)
    Page<Producto> findProductosPorCategoriaConPaginacionYOrden(@Param("categoriaId") Integer categoriaId, @Param("sort") String sort, Pageable pageable);


    @Query(value = "SELECT * FROM producto WHERE idSubcategoria = :idSubcategoria", nativeQuery = true)
    List<Producto> findProductosPorSubcategoria(@Param("idSubcategoria") Integer idSubcategoria);

    @Query(value = "SELECT * FROM producto WHERE idSubcategoria = :idSubcategoria AND borrado = 0", nativeQuery = true)
    Page<Producto> findProductosPorSubcategoriaConPaginacion(@Param("idSubcategoria") Integer idSubcategoria, Pageable pageable);


    @Modifying
    @Transactional
    @Query(value = "UPDATE producto SET disponibilidad = 'Fuera de stock' WHERE cantVentas >= cantidadDisponible", nativeQuery = true)
    void actualizarDisponibilidad();

    @Query(value = "SELECT DATE_FORMAT(p.fechaArribo, '%d-%m-%Y') FROM producto p WHERE p.idProducto = :id", nativeQuery = true)
    String findFechaFormateadaById(@Param("id") Integer id);

    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    @Query("SELECT p FROM Producto p WHERE p.zona.id = :zonaId")
    List<Producto> findProductosPorZona(@Param("zonaId") Integer zonaId);

    @Query("SELECT p FROM Producto p WHERE p.zona.id = :zonaId AND p.idCategoria.id = :categoriaId")
    Page<Producto> findProductosPorZonaYCategoria(@Param("zonaId") Integer zonaId, @Param("categoriaId") Integer categoriaId, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.idSubcategoria.id = :subcategoriaId AND p.zona.id = :zonaId")
    Page<Producto> findProductosPorZonaYSubcategoria(@Param("zonaId") Integer zonaId, @Param("subcategoriaId") Integer subcategoriaId, Pageable pageable);

}
