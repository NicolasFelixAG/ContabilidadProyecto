package com.example.logic;

import com.example.model.Producto;

import java.util.*;

public class InventarioManager {
    private LinkedList<Producto> inventario = new LinkedList<>();
    private String metodo = "PEPS"; // Puede ser "PEPS", "UEPS", "PROMEDIO"

    public void comprar(String nombre, int cantidad, double precio) {
        inventario.add(new Producto(nombre, cantidad, precio));
    }

    public void vender(String nombre, int cantidad) {
        switch (metodo) {
            case "PEPS":
                venderPEPS(nombre, cantidad);
                break;
            case "UEPS":
                venderUEPS(nombre, cantidad);
                break;
            case "PROMEDIO":
                venderPromedio(nombre, cantidad);
                break;
        }
    }

    public void devolver(String nombre, int cantidad) {
        // Se asume que se devuelve con el último precio de venta (simplificado)
        inventario.addFirst(new Producto(nombre, cantidad, 0));
    }

    private void venderPEPS(String nombre, int cantidad) {
        Iterator<Producto> it = inventario.iterator();
        while (it.hasNext() && cantidad > 0) {
            Producto p = it.next();
            if (p.nombre.equals(nombre)) {
                int extraido = Math.min(p.cantidad, cantidad);
                p.cantidad -= extraido;
                cantidad -= extraido;
                if (p.cantidad == 0) it.remove();
            }
        }
    }

    private void venderUEPS(String nombre, int cantidad) {
        ListIterator<Producto> it = inventario.listIterator(inventario.size());
        while (it.hasPrevious() && cantidad > 0) {
            Producto p = it.previous();
            if (p.nombre.equals(nombre)) {
                int extraido = Math.min(p.cantidad, cantidad);
                p.cantidad -= extraido;
                cantidad -= extraido;
                if (p.cantidad == 0) it.remove();
            }
        }
    }

    private void venderPromedio(String nombre, int cantidad) {
        double totalCosto = 0;
        int totalCantidad = 0;

        for (Producto p : inventario) {
            if (p.nombre.equals(nombre)) {
                totalCosto += p.precioUnitario * p.cantidad;
                totalCantidad += p.cantidad;
            }
        }

        if (totalCantidad < cantidad) return;

        double precioPromedio = totalCosto / totalCantidad;
        inventario.add(new Producto(nombre, -cantidad, precioPromedio));
    }

    public String mostrarInventario() {
        StringBuilder sb = new StringBuilder("Inventario actual:\n");
        for (Producto p : inventario) {
            sb.append(String.format("%s - %d unidades a $%.2f\n", p.nombre, p.cantidad, p.precioUnitario));
        }
        return sb.toString();
    }
}
