package com.example.gtics.repository;

import com.example.gtics.entity.Usuario;
import com.example.gtics.entity.UsuarioSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioSessionRepository extends JpaRepository<UsuarioSession, Integer> {

    public UsuarioSession findByEmail(String email);
    @Query(value = "SELECT * FROM usuario WHERE email = ? OR AGT_codigoAduana = ?;", nativeQuery = true)
    Usuario findByEmailOrCodigoAduana(String identifier1, String identifier2);


}
