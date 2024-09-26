package com.example.gtics.repository;

import com.example.gtics.entity.Categoria;
import com.example.gtics.entity.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Integer> {
    List<Subcategoria> findByCategoria_Id(Integer idCategoria);
}
