package com.example.model;

import java.util.List;

public class ResultadoDescuentoLotes {
    public final double costoTotalVentaGeneral; // Suma de todos los costoDeEsteDetalle
    public final List<VentaDetalleLote> detallePorLoteConsumido;

    public ResultadoDescuentoLotes(double costoTotalVentaGeneral, List<VentaDetalleLote> detallePorLoteConsumido) {
        this.costoTotalVentaGeneral = costoTotalVentaGeneral;
        this.detallePorLoteConsumido = detallePorLoteConsumido;
    }
}
