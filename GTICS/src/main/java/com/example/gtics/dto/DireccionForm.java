package com.example.gtics.dto;

public class DireccionForm {
    private String nombreContacto;
    private String telefono;
    private String direccion;
    private Integer zonaId;
    private Integer distritoId;
    private String etiquetaId;
    private boolean predeterminado;
    private String ruc;

    public String getRuc() {return ruc;}

    public void setRuc(String ruc) {this.ruc = ruc;}

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getZonaId() {
        return zonaId;
    }

    public void setZonaId(Integer zonaId) {
        this.zonaId = zonaId;
    }

    public Integer getDistritoId() {
        return distritoId;
    }

    public void setDistritoId(Integer distritoId) {
        this.distritoId = distritoId;
    }

    public String getEtiquetaId() {
        return etiquetaId;
    }

    public void setEtiquetaId(String etiquetaId) {
        this.etiquetaId = etiquetaId;
    }

    public boolean isPredeterminado() {
        return predeterminado;
    }

    public void setPredeterminado(boolean predeterminado) {
        this.predeterminado = predeterminado;
    }
}
