package com.example.gtics.repository;

import com.example.gtics.entity.Rol;
import com.example.gtics.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository  extends JpaRepository<Rol,Integer> {
}
