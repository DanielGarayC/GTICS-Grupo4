package com.example.gtics.repository;

import com.example.gtics.entity.UsuarioSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioSessionRepository extends JpaRepository<UsuarioSession, Integer> {

    public UsuarioSession findByEmail(String email);


}
