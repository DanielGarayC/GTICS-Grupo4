package com.example.gtics.repository;

import com.example.gtics.dto.*;

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
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {

    @Query(value = "SELECT u.idusuario, u.nombre, u.apellidopaterno, u.apellidomaterno, u.dni, u.telefono,\n" +
            "       s.indicadorSolicitud,\n" +
            "       s.codigoAduana as soladuana,\n" +
            "       s.codigoRuc as solruc,\n" +
            "       s.codigoJurisdiccion as soljurisdiccion,\n" +
            "       COALESCE((SELECT cj.estadoCodigo\n" +
            "                 FROM CodigosJurisdiccion cj\n" +
            "                 WHERE cj.codigoJurisdiccion = s.codigoJurisdiccion), 'No encontrado') as estadoCodigoJurisdiccion,\n" +
            "       COALESCE((SELECT ca.estadoCodigo\n" +
            "                 FROM CodigosAduaneros ca\n" +
            "                 WHERE ca.codigoAduanero = s.codigoAduana), 'No encontrado') as estadoCodigoAduana,\n" +
            "       z.nombrezona\n" +
            "FROM usuario u, solicitudagente s, zona z\n" +
            "WHERE s.idSolicitudAgente > 0\n" +
            "  AND s.idUsuario = u.idUsuario\n" +
            "  AND z.idZona = u.idZona",
            nativeQuery = true)
    Page<solAgente> mostrarSolicitudesAgenteConPaginacion(Pageable pageable);
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
            "WHERE u.idRol = 3 AND u.activo = 1 " +
            "GROUP BY u.idusuario",
            countQuery = "SELECT COUNT(*) FROM usuario u WHERE u.idRol = 3",
            nativeQuery = true)
    Page<Object[]> mostrarAgentesConPaginacion(Pageable pageable);
    // Método personalizado para buscar usuarios por el ID del rol con paginación


    // Método personalizado para encontrar usuarios por rol
    @Query(nativeQuery = true, value = "SELECT * FROM usuario u WHERE u.idRol = ?1 AND u.baneado = FALSE")
    Page<Usuario> findByRol_Id(int rolId, Pageable pageable);

    //Método para obtener la lista de los usuarios baneados
    @Query(nativeQuery = true, value = "SELECT * FROM usuario u WHERE u.baneado = TRUE")
    Page<Usuario> find_users_banned(Pageable pageable);

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
            "AND u.baneado  =0 and u.activo=1")

    Page<Usuario> findUsuariosAsignadosAlAgente(Integer idAgente, Pageable pageable);

    //Banear Usuario por id
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE usuario u \n" +
            "SET u.baneado = 1, \n" +
            "    u.razonBaneado = ?2 \n" +
            "WHERE u.idUsuario = ?1;")
    void banUsuario(Integer idUsuario,String razonBaneado);

    @Transactional
    @Modifying
    @Query(nativeQuery=true,value="update usuario set activo = 1 where idUsuario= ?1")
    void activarCuenta(int idUsuario);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE usuario u SET u.baneado = false WHERE u.idUsuario = ?1 AND u.baneado = true")
    void quitarBanUsuario(@Param("idUsuario") Integer idUsuario);

    //Actualizar Usuario Final
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE usuario SET dni = ?1, nombre = ?2, apellidoPaterno = ?3, " +
            "apellidoMaterno = ?4, email = ?5, direccion = ?6, " +
            "telefono = ?7, idDistrito = ?8, foto = ?9 WHERE idUsuario = ?10")
    void actualizarUsuarioFinal(@Param("dni") String dni,
                                @Param("nombre") String nombre,
                                @Param("apellidoPaterno") String apellidoPaterno,
                                @Param("apellidoMaterno") String apellidoMaterno,
                                @Param("email") String email,
                                @Param("direccion") String direccion,
                                @Param("telefono") String telefono,
                                @Param("idDistrito") Integer idDistrito,
                                byte [] foto,
                                @Param("idUsuario") Integer idUsuario);




    //Metodo para mostrar solicitudes de agente con filtro
    @Query(nativeQuery = true, value = "SELECT u.idusuario, u.nombre, u.apellidopaterno, u.apellidomaterno, u.dni, u.telefono,\n" +
            "       s.indicadorSolicitud,\n" +
            "       s.codigoAduana as soladuana,\n" +
            "       s.codigoRuc as solruc,\n" +
            "       s.codigoJurisdiccion as soljurisdiccion,\n" +
            "       COALESCE((SELECT cj.estadoCodigo\n" +
            "                 FROM CodigosJurisdiccion cj\n" +
            "                 WHERE cj.codigoJurisdiccion = s.codigoJurisdiccion), 'No encontrado') as estadoCodigoJurisdiccion,\n" +
            "       COALESCE((SELECT ca.estadoCodigo\n" +
            "                 FROM CodigosAduaneros ca\n" +
            "                 WHERE ca.codigoAduanero = s.codigoAduana), 'No encontrado') as estadoCodigoAduana,\n" +
            "       z.nombrezona\n" +
            "FROM usuario u, solicitudagente s, zona z\n" +
            "WHERE s.idSolicitudAgente > 0\n" +
            "  AND s.idUsuario = u.idUsuario\n" +
            "  AND z.idZona = u.idZona\n" +
            "  AND s.indicadorSolicitud= ?1")
    List<solAgente> mostrarSolicitudesAgenteFiltro(Integer indicador);
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

    @Query(value = "SELECT COUNT(u.idusuario) as cantUsuariosActivos " +
            "FROM usuario u WHERE u.activo = 1 AND u.idRol = 4 and u.idZona =?1", nativeQuery = true)
    CantUsuariosActivos getCantidadActivosZona(int idZona);

    @Query(value = "SELECT COUNT(u.idusuario) as cantUsuariosRegistrados FROM usuario u WHERE u.idRol = 4", nativeQuery = true)
    CantUsuariosRegistrados getCantidadRegistrados();

    @Query(value = "SELECT COUNT(u.idusuario) as cantUsuariosRegistrados FROM usuario u WHERE u.idRol = 4 and u.idZona =?1", nativeQuery = true)
    CantUsuariosRegistrados getCantidadRegistradosZona(int idZona);

    @Query(value = "SELECT COUNT(u.idusuario) as cantAgentes FROM usuario u WHERE u.idRol = 3", nativeQuery = true)
    CantidadAgentes getCantidadAgentes();

    //Para asignar una solicitud a un usuario
    @Transactional
    @Modifying
    @Query(nativeQuery=true,value="update usuario set idSolicitudAgente = ?1 where idUsuario= ?2")
    void asignarSolictudAusuario(int idSolicitud,int idUsuario);

    @Query(value = "SELECT * FROM usuario WHERE idusuario = :idUsuario", nativeQuery = true)
    Usuario findUsuarioById(@Param("idUsuario") int idUsuario);

    @Query(value = "SELECT usuario.*,\n" +
            "       (SELECT SUM(CAST(u2.u_cantImportaciones AS UNSIGNED))\n" +
            "        FROM (SELECT u_cantImportaciones\n" +
            "              FROM usuario\n" +
            "              WHERE usuario.idRol = 4\n" +
            "              ORDER BY CAST(usuario.u_cantImportaciones AS UNSIGNED) DESC\n" +
            "              LIMIT 10) AS u2) AS totalImportaciones\n" +
            "FROM usuario\n" +
            "WHERE usuario.idRol = 4\n" +
            "ORDER BY CAST(usuario.u_cantImportaciones AS UNSIGNED) DESC\n" +
            "LIMIT 10", nativeQuery = true)
    List<UsuarioImportante> getTopImportadores(); //para los top importadores

    @Query(value="SELECT u.*,\n" +
            "                   (SELECT COUNT(*)\n" +
            "                    FROM usuario u2\n" +
            "                    WHERE u2.idRol = 4\n" +
            "                      AND u2.idAgente = u.idUsuario) AS cantidadUsuarios,\n" +
            "                   (SELECT COUNT(*)\n" +
            "                    FROM orden o\n" +
            "                    WHERE o.idAgente = u.idUsuario) AS cantidadOrdenes\n" +
            "            FROM usuario u where activo = 1 and idRol = 3 and\n" +
            "             u.idAdminZonal = ?1", nativeQuery = true)
    List<Agente> findAgentesByAdminZonal(int idAdminZonal);

    @Query(value="SELECT COUNT(*) AS totalEntradas\n" +
            "FROM (\n" +
            "         SELECT u.*,\n" +
            "                (SELECT COUNT(*)\n" +
            "                 FROM usuario u2\n" +
            "                 WHERE u2.idRol = 4\n" +
            "                   AND u2.idAgente = u.idUsuario) AS cantidadUsuarios,\n" +
            "                (SELECT COUNT(*)\n" +
            "                 FROM orden o\n" +
            "                 WHERE o.idAgente = u.idUsuario) AS cantidadOrdenes\n" +
            "         FROM usuario u\n" +
            "         WHERE u.idAdminZonal = ?1 and activo = 1\n" +
            "     ) AS subquery", nativeQuery = true)
    Integer cantAgentesByAZ(int idAdminZonal);

    @Query(value = "SELECT usuario.*\n" +
            "FROM usuario\n" +
            "WHERE usuario.idRol = 2\n" +
            "AND usuario.idZona = :zonaId", nativeQuery = true)

    List<Usuario> findAZporZona(int zonaId);
    Optional<Usuario> findByEmail(String email);
    
    @Transactional
    @Modifying
    @Query(value="UPDATE usuario\n" +
            "SET activo = 0\n" +
            "WHERE idUsuario = ?1", nativeQuery = true)
    void logicalDelete(Integer id);

    @Query(nativeQuery = true, value = "SELECT * FROM usuario u " +
            "WHERE u.idRol = 4 " +
            "AND u.idAgente = ?1 " +
            "AND u.baneado  =0 and u.activo=1")

    List<Usuario> findUsuariosAsignadosAlAgenteNoPageable(Integer idAgente);

}
