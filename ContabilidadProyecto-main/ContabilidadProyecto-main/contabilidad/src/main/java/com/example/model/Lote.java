package com.example.model;

public class Lote {
    int cantidad;
    double precioUnitario;

    public Lote(int cantidad, double precioUnitario) {
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }  
    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return precioUnitario;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setPrecio(double precio) {
        this.precioUnitario = precio;
    }
    @Override
    public String toString() {
        return "Lote{" +
                "cantidad=" + cantidad +
                ", precio=" + precioUnitario +
                '}';
    }
}