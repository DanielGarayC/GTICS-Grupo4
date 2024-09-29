package com.example.gtics.repository;

import com.example.gtics.dto.CantUsuariosActivos;
import com.example.gtics.dto.CantUsuariosBaneados;

import com.example.gtics.dto.CantUsuariosRegistrados;
import com.example.gtics.dto.CantidadAgentes;
import com.example.gtics.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {

    @Query(value = "SELECT u.idusuario, u.nombre, u.apellidopaterno, u.apellidomaterno, u.dni, u.telefono, " +
            "u.idSolicitudAgente AS solicitudId, s.indicadorSolicitud, " +
            "u.agt_codigoAduana, " +
            "CASE FLOOR(1 + (RAND() * 5)) " +
            "WHEN 1 THEN 'Habilitado' " +
            "WHEN 2 THEN 'Multado' " +
            "WHEN 3 THEN 'Cancelado' " +
            "WHEN 4 THEN 'Suspendido' " +
            "WHEN 5 THEN 'Anulado de jurisdicción' " +
            "END AS estadoCodigoAduana, " +
            "u.agt_codigoJurisdiccion, " +
            "CASE FLOOR(1 + (RAND() * 5)) " +
            "WHEN 1 THEN 'Habilitado' " +
            "WHEN 2 THEN 'Multado' " +
            "WHEN 3 THEN 'Cancelado' " +
            "WHEN 4 THEN 'Suspendido' " +
            "WHEN 5 THEN 'Anulado de jurisdicción' " +
            "END AS estadoCodigoJurisdiccion, " +
            "z.nombrezona " +
            "FROM usuario u " +
            "LEFT JOIN solicitudagente s ON u.idSolicitudAgente = s.idSolicitudAgente " +
            "JOIN zona z ON u.idzona = z.idzona " +
            "WHERE u.idSolicitudAgente > 0 AND u.idRol = 4 " +
            "GROUP BY u.idusuario, u.idSolicitudAgente",
            countQuery = "SELECT COUNT(u.idusuario) FROM usuario u WHERE u.idSolicitudAgente > 0 AND u.idRol = 4",
            nativeQuery = true)
    Page<Object[]> mostrarSolicitudesConEstadosAleatoriosConPaginacion(Pageable pageable);
    // Método personalizado para buscar agentes con paginación
    @Query(value = "SELECT u.idusuario, u.nombre, u.apellidopaterno, u.apellidomaterno, u.dni, u.telefono, " +
            "u.agt_codigoaduana, " +
            "CASE FLOOR(1) " +
            "WHEN 1 THEN 'Habilitado' " +
            "END AS estadoCodigoAduana, " +
            "u.agt_codigojurisdiccion, " +
            "CASE FLOOR(1) " +
            "WHEN 1 THEN 'Habilitado' " +
            "END AS estadoCodigoJurisdiccion, " +
            "u.agt_razonsocial, z.nombrezona " +
            "FROM usuario u " +
            "JOIN zona z ON u.idzona = z.idzona " +
            "WHERE u.idRol = 3 " +
            "GROUP BY u.idusuario",
            countQuery = "SELECT COUNT(*) FROM usuario u WHERE u.idRol = 3",
            nativeQuery = true)
    Page<Object[]> mostrarAgentesConPaginacion(Pageable pageable);
    // Método personalizado para buscar usuarios por el ID del rol con paginación
    Page<Usuario> findByRol_Id(int rolId, Pageable pageable);

    // Método personalizado para encontrar usuarios por rol
    @Query(value = "SELECT * FROM usuario u WHERE u.idRol = :idRol AND u.baneado = false", nativeQuery = true)
    List<Usuario> findByIdRol_Id(@Param("idRol") Integer idRol);

    @Query(value = "SELECT COUNT(*) " +
            "FROM usuario u " +
            "WHERE u.idRol = 2 " +
            "AND u.idZona = :zonaId", nativeQuery = true)
    Integer countCoordinadoresByZona(@Param("zonaId") Integer zonaId);

    //Método para el buscador de la lista de Admin Zonal para Superadmin (adrian chambea)
    @Query(value = "SELECT * FROM usuario u " +
            "WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.apellido_paterno) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.apellido_materno) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.dni) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(u.telefono) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(z.nombre_zona) LIKE LOWER(CONCAT('%', :busqueda, '%'))",
            nativeQuery = true)
    List<Usuario> FiltroBuscador(@Param("busqueda") String busqueda);


    @Query(nativeQuery = true, value = "SELECT * FROM usuario u " +
            "WHERE u.idRol = 4 " +
            "AND u.idAgente = ?1 " +
            "AND u.baneado  =0")
    List<Usuario> findUsuariosAsignadosAlAgente(Integer idAgente);

    //Banear Usuario por id
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE usuario u SET u.baneado = true WHERE u.idUsuario = ?1 AND u.baneado = false")
    void banUsuario(@Param("idUsuario") Integer idUsuario);

    //Actualizar Usuario Final
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE usuario SET dni = ?1, nombre = ?2, apellidoPaterno = ?3, " +
            "apellidoMaterno = ?4, email = ?5, direccion = ?6, " +
            "telefono = ?7, idDistrito = ?8 WHERE idUsuario = ?9")
    void actualizarUsuarioFinal(@Param("dni") String dni,
                                @Param("nombre") String nombre,
                                @Param("apellidoPaterno") String apellidoPaterno,
                                @Param("apellidoMaterno") String apellidoMaterno,
                                @Param("email") String email,
                                @Param("direccion") String direccion,
                                @Param("telefono") String telefono,
                                @Param("idDistrito") Integer idDistrito,
                                @Param("idUsuario") Integer idUsuario);


    //Metodo para mostrar solicitudes de agente
    @Query(nativeQuery = true, value = "SELECT u.idusuario, u.nombre, u.apellidopaterno, u.apellidomaterno, u.dni, u.telefono, " +
            "u.idSolicitudAgente AS solicitudId, s.indicadorSolicitud, " +
            "u.agt_codigoaduana, " +
            "CASE FLOOR(1 + (RAND() * 5)) " +
            "WHEN 1 THEN 'Habilitado' " +
            "WHEN 2 THEN 'Multado' " +
            "WHEN 3 THEN 'Cancelado' " +
            "WHEN 4 THEN 'Suspendido' " +
            "WHEN 5 THEN 'Anulado de jurisdicción' " +
            "END AS estadoCodigoAduana, " +
            "u.agt_codigojurisdiccion, " +
            "CASE FLOOR(1 + (RAND() * 5)) " +
            "WHEN 1 THEN 'Habilitado' " +
            "WHEN 2 THEN 'Multado' " +
            "WHEN 3 THEN 'Cancelado' " +
            "WHEN 4 THEN 'Suspendido' " +
            "WHEN 5 THEN 'Anulado de jurisdicción' " +
            "END AS estadoCodigoJurisdiccion, " +
            "z.nombrezona " +
            "FROM usuario u " +
            "LEFT JOIN solicitudagente s ON u.idSolicitudAgente = s.idSolicitudAgente " +
            "JOIN zona z ON u.idzona = z.idzona " +
            "WHERE u.idSolicitudAgente > 0 AND u.idRol = 4 " +
            "GROUP BY u.idusuario, u.idSolicitudAgente")
    List<Object[]> mostrarSolicitudesConEstadosAleatorios();
    //Metodo para mostrar solicitudes de agente con filtro
    @Query(nativeQuery = true, value = "SELECT u.idusuario, u.nombre, u.apellidopaterno, u.apellidomaterno, u.dni, u.telefono, " +
            "u.idSolicitudAgente AS solicitudId, s.indicadorSolicitud, " +
            "u.agt_codigoaduana, " +
            "CASE FLOOR(1 + (RAND() * 5)) " +
            "WHEN 1 THEN 'Habilitado' " +
            "WHEN 2 THEN 'Multado' " +
            "WHEN 3 THEN 'Cancelado' " +
            "WHEN 4 THEN 'Suspendido' " +
            "WHEN 5 THEN 'Anulado de jurisdicción' " +
            "END AS estadoCodigoAduana, " +
            "u.agt_codigojurisdiccion, " +
            "CASE FLOOR(1 + (RAND() * 5)) " +
            "WHEN 1 THEN 'Habilitado' " +
            "WHEN 2 THEN 'Multado' " +
            "WHEN 3 THEN 'Cancelado' " +
            "WHEN 4 THEN 'Suspendido' " +
            "WHEN 5 THEN 'Anulado de jurisdicción' " +
            "END AS estadoCodigoJurisdiccion, " +
            "z.nombrezona " +
            "FROM usuario u " +
            "LEFT JOIN solicitudagente s ON u.idSolicitudAgente = s.idSolicitudAgente " +
            "JOIN zona z ON u.idzona = z.idzona " +
            "WHERE u.idSolicitudAgente > 0 AND u.idRol = 4 and s.indicadorSolicitud= ?1 " +
            "GROUP BY u.idusuario, u.idSolicitudAgente")
    List<Object[]> mostrarSolicitudesConEstadosAleatoriosFiltro(Integer indicador);
    @Query(nativeQuery = true, value = "SELECT u.idusuario, u.nombre, u.apellidopaterno, u.apellidomaterno, u.dni, u.telefono, " +
            "u.agt_codigoaduana, 'Habilitado' AS estadoCodigoAduana, " +
            "u.agt_codigojurisdiccion, 'Habilitado' AS estadoCodigoJurisdiccion, " +
            "u.agt_razonsocial, z.nombrezona " +
            "FROM usuario u " +
            "JOIN zona z ON u.idzona = z.idzona " +
            "WHERE u.idRol = 3 " +
            "GROUP BY u.idusuario")
    List<Object[]> mostrarAgentesConEstadosYRazonSocial();




    @Transactional
    @Modifying
    @Query(nativeQuery=true,value="update usuario set idRol = 3 where idUsuario= ?1")
    void actualizarRolAAgente(int idUsuario);

    @Query(value = "SELECT COUNT(u.idusuario) as cantUsuariosBaneados " +
            "FROM usuario u WHERE u.baneado = 1 AND u.idRol = 4", nativeQuery = true)
    CantUsuariosBaneados getCantidadBaneados();

    @Query(value = "SELECT COUNT(u.idusuario) as cantUsuariosActivos " +
            "FROM usuario u WHERE u.activo = 1 AND u.idRol = 4", nativeQuery = true)
    CantUsuariosActivos getCantidadActivos();

    @Query(value = "SELECT COUNT(u.idusuario) as cantUsuariosRegistrados FROM usuario u WHERE u.idRol = 4", nativeQuery = true)
    CantUsuariosRegistrados getCantidadRegistrados();

    @Query(value = "SELECT COUNT(u.idusuario) as cantAgentes FROM usuario u WHERE u.idRol = 3", nativeQuery = true)
    CantidadAgentes getCantidadAgentes();

    //Para asignar una solicitud a un usuario
    @Transactional
    @Modifying
    @Query(nativeQuery=true,value="update usuario set idSolicitudAgente = ?1 where idUsuario= ?2")
    void asignarSolictudAusuario(int idSolicitud,int idUsuario);

    @Query(value = "SELECT * FROM usuario WHERE idusuario = :idUsuario", nativeQuery = true)
    Usuario findUsuarioById(@Param("idUsuario") Integer idUsuario);
}
