package com.desarrollo.proyectocrudg3.Model;

public class Categoria {
    private int idCategorias;
    private String nombre;
    private String descripcion;
    private int estado;

    public Categoria() {
    }

    public Categoria(int idCategorias, String nombre, String descripcion, int estado) {
        this.idCategorias = idCategorias;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    // Constructor sin ID para inserciones (id_categorias es SERIAL)
    public Categoria(String nombre, String descripcion, int estado) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getIdCategorias() {
        return idCategorias;
    }

    public void setIdCategorias(int idCategorias) {
        this.idCategorias = idCategorias;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // Permite que ComboBox<Categoria> muestre el nombre directamente en la vista
    @Override
    public String toString() {
        return nombre;
    }
}
