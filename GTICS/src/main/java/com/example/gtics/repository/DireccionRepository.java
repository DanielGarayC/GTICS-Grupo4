package com.example.gtics.repository;

import com.example.gtics.entity.Direccion;
import com.example.gtics.entity.Tienda;
import com.example.gtics.entity.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DireccionRepository extends JpaRepository<Direccion, Integer> {

    List<Direccion> findByUsuario(Usuario usuario);

    @Modifying
    @Transactional
    @Query(value = "UPDATE direcciones SET predeterminado = false WHERE idusuario = :userId", nativeQuery = true)
    void updatePredeterminadaByUsuario(@Param("userId") Integer userId);

}
