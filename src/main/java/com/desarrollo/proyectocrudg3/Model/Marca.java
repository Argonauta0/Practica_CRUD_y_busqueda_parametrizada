package com.desarrollo.proyectocrudg3.Model;

public class Marca {
    private int idMarca;
    private String nombre;
    private String paisOrigen;
    private int estado;

    public Marca() {
    }

    public Marca(int idMarca, String nombre, String paisOrigen, int estado) {
        this.idMarca = idMarca;
        this.nombre = nombre;
        this.paisOrigen = paisOrigen;
        this.estado = estado;
    }

    // Constructor sin ID para inserciones (id_marca es SERIAL)
    public Marca(String nombre, String paisOrigen, int estado) {
        this.nombre = nombre;
        this.paisOrigen = paisOrigen;
        this.estado = estado;
    }

    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // Permite que ComboBox<Marca> muestre el nombre directamente en la vista
    @Override
    public String toString() {
        return nombre;
    }
}
