package com.example.gtics.repository;

import com.example.gtics.entity.Usuario;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    // Método personalizado para encontrar usuarios por rol
    @Query("SELECT u from Usuario u WHERE u.rol.id = :idRol ")
    List<Usuario> findByIdRol_Id(Integer idRol);

    //Método para el buscador de la lista de Admin Zonal para Superadmin (adrian chambea)
    @Query("SELECT u from Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) "+
            "OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.dni) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.telefono) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.zona.nombreZona) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Usuario>FiltroBuscador(@Param("busqueda") String busqueda);
}
