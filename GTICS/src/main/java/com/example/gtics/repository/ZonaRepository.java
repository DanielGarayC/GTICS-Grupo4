package com.example.gtics.repository;

import com.example.gtics.entity.Usuario;
import com.example.gtics.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ZonaRepository extends JpaRepository<Zona,Integer> {
}
