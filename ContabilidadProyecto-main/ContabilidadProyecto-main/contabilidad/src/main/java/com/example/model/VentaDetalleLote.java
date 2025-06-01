package com.example.model;

public class VentaDetalleLote {
    public final int cantidadVendida;
    public final double precioUnitario;
    public final double costoDeEsteDetalle;

    public VentaDetalleLote(int cantidadVendida, double precioUnitario, double costoDeEsteDetalle) {
        this.cantidadVendida = cantidadVendida;
        this.precioUnitario = precioUnitario;
        this.costoDeEsteDetalle = costoDeEsteDetalle;
    }
}
