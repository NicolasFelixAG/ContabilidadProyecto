package com.example.logic;

import com.example.model.Producto;

import java.util.*;

public class InventarioManager {
    private LinkedList<Producto> inventario = new LinkedList<>();
    private String metodo;

    public InventarioManager(String metodo) {
        this.metodo = metodo;
    }

    public double comprar(int cantidad, double precioUnitario) {
        inventario.add(new Producto("Producto", cantidad, precioUnitario));
        return cantidad * precioUnitario;
    }

    public double vender(int cantidad) {
        switch (metodo) {
            case "PEPS":
                return venderPEPS(cantidad);
            case "UEPS":
                return venderUEPS(cantidad);
            case "PROMEDIO":
                return venderPromedio(cantidad);
            default:
                throw new IllegalArgumentException("Método no válido: " + metodo);
        }
    }

    public double devolver(int cantidad) {
        double precio = getUltimoPrecioUnitario();
        inventario.addFirst(new Producto("Producto", cantidad, precio));
        return cantidad * precio;
    }

    private double venderPEPS(int cantidad) {
        double total = 0;
        Iterator<Producto> it = inventario.iterator();
        while (it.hasNext() && cantidad > 0) {
            Producto p = it.next();
            int usado = Math.min(p.getCantidad(), cantidad);
            total += usado * p.getPrecioUnitario();
            p.setCantidad(p.getCantidad() - usado);
            cantidad -= usado;
            if (p.getCantidad() == 0) it.remove();
        }
        return total;
    }

    private double venderUEPS(int cantidad) {
        double total = 0;
        ListIterator<Producto> it = inventario.listIterator(inventario.size());
        while (it.hasPrevious() && cantidad > 0) {
            Producto p = it.previous();
            int usado = Math.min(p.getCantidad(), cantidad);
            total += usado * p.getPrecioUnitario();
            p.setCantidad(p.getCantidad() - usado);
            cantidad -= usado;
            if (p.getCantidad() == 0) it.remove();
        }
        return total;
    }

    private double venderPromedio(int cantidad) {
        int totalCantidad = 0;
        double totalCosto = 0;

        for (Producto p : inventario) {
            totalCantidad += p.getCantidad();
            totalCosto += p.getCantidad() * p.getPrecioUnitario();
        }

        if (totalCantidad < cantidad) return 0;

        double precioPromedio = totalCosto / totalCantidad;
        int restante = cantidad;
        Iterator<Producto> it = inventario.iterator();
        while (it.hasNext() && restante > 0) {
            Producto p = it.next();
            int usado = Math.min(p.getCantidad(), restante);
            p.setCantidad(p.getCantidad() - usado);
            restante -= usado;
            if (p.getCantidad() == 0) it.remove();
        }

        return cantidad * precioPromedio;
    }

    public int getExistencia() {
        return inventario.stream().mapToInt(Producto::getCantidad).sum();
    }

    public double getUltimoPrecioUnitario() {
        return inventario.isEmpty() ? 0 : inventario.getLast().getPrecioUnitario();
    }

    public String mostrarInventario() {
        StringBuilder sb = new StringBuilder("Inventario actual:\n");
        for (Producto p : inventario) {
            sb.append(String.format("Producto - %d unidades a $%.2f\n", p.getCantidad(), p.getPrecioUnitario()));
        }
        return sb.toString();
    }
} 
