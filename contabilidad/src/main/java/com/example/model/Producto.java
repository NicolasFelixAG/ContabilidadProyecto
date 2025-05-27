package com.example.model;

public class Producto {
    public String nombre;
    public int cantidad;
    public double precioUnitario;

    public Producto(String nombre, int cantidad, double precioUnitario) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }
}