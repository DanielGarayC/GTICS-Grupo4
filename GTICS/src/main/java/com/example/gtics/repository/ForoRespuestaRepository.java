package com.example.gtics.repository;

import com.example.gtics.entity.Foropregunta;
import com.example.gtics.entity.Fororespuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForoRespuestaRepository extends JpaRepository<Fororespuesta,Integer> {
    List<Fororespuesta> findByIdPregunta(Foropregunta pregunta);
}
