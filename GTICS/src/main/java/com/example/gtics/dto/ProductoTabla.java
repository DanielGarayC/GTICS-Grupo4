package com.example.gtics.dto;

import java.util.Date;

public interface ProductoTabla {
    Integer getIdProducto();
    String getNombreProducto();
    Integer getCantidadDisponible();
    Date getFechaArribo();
    Integer getCantidadSolicitada();
    byte[] getPrimeraFoto();
}
