package com.example.gtics.repository;

import com.example.gtics.dto.CantUsuariosActivos;
import com.example.gtics.dto.CantUsuariosBaneados;
import com.example.gtics.dto.CantUsuariosInactivos;
import com.example.gtics.dto.CantidadAgentes;
import com.example.gtics.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    // Método personalizado para encontrar usuarios por rol
    @Query("SELECT u from Usuario u WHERE u.rol.id = :idRol AND u.baneado = false ")
    List<Usuario> findByIdRol_Id(Integer idRol);

    @Query("SELECT COUNT(u) " +
            "    FROM Usuario u " +
            "    WHERE u.rol.id = 2 " +
            "    AND u.zona.id = :zonaId")
    Integer countCoordinadoresByZona(@Param("zonaId") Integer zonaId);

    //Método para el buscador de la lista de Admin Zonal para Superadmin (adrian chambea)
    @Query("SELECT u from Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) "+
            "OR LOWER(u.apellidoPaterno) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.apellidoMaterno) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.dni) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.telefono) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.zona.nombreZona) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Usuario>FiltroBuscador(@Param("busqueda") String busqueda);


    @Query("SELECT u FROM Usuario u WHERE u.rol.id = :idRol AND u.idAgente = :agente AND (u.baneado IS NULL OR u.baneado = false)")
    List<Usuario> findUsuariosFiltrados(@Param("idRol") Integer idRol, @Param("agente") Usuario agente);

    //Banear Usuario por id
    @Transactional
    @Modifying
    @Query("UPDATE Usuario u SET u.baneado = true WHERE u.id = :idUsuario AND u.baneado = false")
    void banUsuario(Integer idUsuario);

    //Actualizar Usuario Final
    @Transactional
    @Modifying
    @Query("UPDATE Usuario u SET u.dni = :dni, u.nombre = :nombre, u.apellidoPaterno = :apellidoPaterno, " +
            "u.apellidoMaterno = :apellidoMaterno, u.email = :email, u.direccion = :direccion, " +
            "u.telefono = :telefono, u.distrito.id = :idDistrito WHERE u.id = :idUsuario")
    void actualizarUsuarioFinal(String dni,
                                String nombre,
                                String apellidoPaterno,
                                String apellidoMaterno,
                                String email,
                                String direccion,
                                String telefono,
                                Integer idDistrito,
                                Integer idUsuario);

    //Metodo para mostrar solicitudes de agente
    @Query(nativeQuery = true, value = "SELECT * FROM gticsdb.usuario where idSolicitudAgente > 0 and idRol=4 ")
    List<Usuario> mostrarSolicitudesAgente();

    @Transactional
    @Modifying
    @Query(nativeQuery=true,value="update usuario set idRol = 3 where idUsuario= ?1")
    void actualizarRolAAgente(int idUsuario);

    @Query(value = "SELECT COUNT(u.idUsuario) as cantUsuariosBaneados FROM Usuario u WHERE u.baneado = 1 AND u.idRol = 4", nativeQuery = true)
    CantUsuariosBaneados getCantidadBaneados();

    @Query(value="SELECT COUNT(u.idUsuario) as cantUsuariosActivos FROM Usuario u WHERE u.activo = 1 AND u.idRol = 4", nativeQuery = true)
    CantUsuariosActivos getCantidadActivos();

    @Query(value = "SELECT COUNT(u.idUsuario) as cantUsuariosInactivos FROM Usuario u WHERE u.activo = 0 AND u.idRol = 4", nativeQuery = true)
    CantUsuariosInactivos getCantidadInactivos();

    @Query(value = "SELECT COUNT(u.idUsuario) as cantAgentes FROM Usuario u WHERE u.idRol = 3", nativeQuery = true)
    CantidadAgentes getCantidadAgentes();
}