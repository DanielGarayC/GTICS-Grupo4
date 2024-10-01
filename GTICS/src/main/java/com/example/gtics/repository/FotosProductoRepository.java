package com.example.gtics.repository;

import com.example.gtics.entity.Fotosproducto;
import com.example.gtics.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotosProductoRepository extends JpaRepository<Fotosproducto, Integer> {
    List<Fotosproducto> findByProducto_Id(Integer id);
    List<Fotosproducto> findByProducto(Producto producto);

}
