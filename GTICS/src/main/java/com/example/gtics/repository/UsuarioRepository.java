package com.example.gtics.repository;

import com.example.gtics.entity.Usuario;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    // Método personalizado para encontrar usuarios por rol
    List<Usuario> findByIdRol_Id(Integer idRol);
}
