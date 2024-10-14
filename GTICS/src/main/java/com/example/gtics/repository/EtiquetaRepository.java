package com.example.gtics.repository;

import com.example.gtics.entity.Etiqueta;
import com.example.gtics.entity.Orden;
import com.example.gtics.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Integer> {
    List<Etiqueta> findByUsuario(Usuario usuario);
}
