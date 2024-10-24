package com.example.gtics.repository;

import com.example.gtics.entity.Message;
import com.example.gtics.entity.Usuario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Integer> {
    List<Message> findBySalaOrderByFechaEnvioAsc(String sala);
}
