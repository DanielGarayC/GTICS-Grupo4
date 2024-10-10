package com.example.gtics.repository;

import com.example.gtics.entity.Carritocompra;
import com.example.gtics.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoCompraRepository extends JpaRepository<Carritocompra,Integer> {
    Optional<Carritocompra> findByIdUsuario(Usuario usuario);
}
