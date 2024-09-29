package com.example.gtics.dto;

public interface Agente {
    int getIdUsuario();
    int getCantidadUsuarios();
    int getCantidadOrdenes();
    String getNombre();
    String getApellidoMaterno();
    String getApellidoPaterno();
    String getEmail();
    String getDireccion();
    String getTelefono();
    byte[] getFoto();
    String getAGT_codigoAduana();
    String getAGT_ruc();
    String getAGT_codigoJurisdiccion();
    String getAGT_razonSocial();
    String getDistrito();

}
