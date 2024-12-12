package com.example.gtics.dto;

import java.time.LocalDate;

public interface OrdenCarritoDto{

    Integer getPrimerIdProducto();
    LocalDate getFechaOrden();
    LocalDate getFechaLlegada();
    double getMontoTotal();
    Integer getEstadoOrden();
    Integer getControlOrden();
    Integer getIdOrden();
    String getNombre();
    String getApellidoPaterno();
    Integer getSolicitarCancelarOrden();
    Integer getordenEliminada();
    Integer getIdAgente();
}