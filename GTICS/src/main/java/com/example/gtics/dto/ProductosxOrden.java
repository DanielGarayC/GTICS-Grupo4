package com.example.gtics.dto;



public interface ProductosxOrden {

    Byte[] getPrimeraFotoProducto();
    String getNombreProducto();
    String getNombreTienda();
    String getModelo();
    String getColor();
    Integer getCantidadProducto();
    Double getPrecioUnidad();
    Double getPrecioTotalPorProducto();
    Double getCostoEnvio();


}
