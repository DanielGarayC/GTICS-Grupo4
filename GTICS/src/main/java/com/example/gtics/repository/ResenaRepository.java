package com.example.gtics.repository;

import com.example.gtics.entity.Resena;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    @Query("SELECT r FROM Resena r JOIN r.producto p JOIN p.idProveedor prov " +
            "WHERE (" +
            "  (:searchKeyword IS NULL OR " +
            "   (:searchCriteria = 'name' AND p.nombreProducto LIKE CONCAT('%', :searchKeyword, '%')) OR " +
            "   (:searchCriteria = 'provider' AND prov.nombreProveedor LIKE CONCAT('%', :searchKeyword, '%')) OR " +
            "   (:searchCriteria = 'code' AND p.codigoProducto LIKE CONCAT('%', :searchKeyword, '%')) " +
            "  )" +
            ") " +
            "AND (:rating IS NULL OR r.idCalidad.id = :rating) " +
            "AND (:startDate IS NULL OR r.fechaCreacion >= :startDate) " +
            "AND (:endDate IS NULL OR r.fechaCreacion <= :endDate)")
    Page<Resena> findByFilters(
            @Param("searchCriteria") String searchCriteria,
            @Param("searchKeyword") String searchKeyword,
            @Param("rating") Integer rating,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Contar las reseñas por cada nivel de estrella para un producto
    @Query("SELECT COUNT(r) FROM Resena r WHERE r.producto.id = :idProducto AND r.idCalidad.id = :rating")
    Long countByProductoIdAndRating(Integer idProducto, Integer rating);

    // Contar el total de reseñas para un producto
    @Query("SELECT COUNT(r) FROM Resena r WHERE r.producto.id = :idProducto")
    Long countByProductoId(Integer idProducto);
    // Obtener todas las reseñas para un producto específico


    // Obtener el promedio de calificaciones para un producto
    @Query("SELECT AVG(c.id) FROM Resena r JOIN r.idCalidad c WHERE r.producto.id = :idProducto")
    Double findAverageRatingByProductoId(Integer idProducto);


    Page<Resena> findByProductoId(Long idProducto, Pageable pageable);

    List<Resena> findByProducto_Id(Integer idProducto);


}



