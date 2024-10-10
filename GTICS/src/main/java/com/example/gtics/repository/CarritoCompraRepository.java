package com.example.gtics.repository;

import com.example.gtics.entity.Carritocompra;
import com.example.gtics.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoCompraRepository extends JpaRepository<Carritocompra,Integer> {
    Optional<Carritocompra> findByIdUsuarioAndActivoTrue(Usuario usuario);
    Optional<Carritocompra> findByIdUsuarioAndActivo(Usuario usuario, boolean activo);
    @Modifying
    @Query("UPDATE Carritocompra c SET c.activo = :activo WHERE c.id = :carritoId")
    void updateCarritoActivo(@Param("carritoId") Integer carritoId, @Param("activo") boolean activo);
}
