package com.example.view;

import com.example.logic.InventarioManager;
import com.example.model.Operacion;

import javax.swing.*;
import java.awt.*;

public class AlmacenPanel extends JPanel {
    private InventarioManager manager = new InventarioManager();

    public AlmacenPanel() {
        setLayout(new BorderLayout());

        JTextArea salida = new JTextArea();
        salida.setEditable(false);

        JPanel controles = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton btnCompra = new JButton("Comprar");
        JButton btnVenta = new JButton("Vender");
        JButton btnDevolucion = new JButton("Devolver");

        btnCompra.addActionListener(e -> {
            manager.comprar("Producto A", 10, 100);
            salida.setText(manager.mostrarInventario());
        });

        btnVenta.addActionListener(e -> {
            manager.vender("Producto A", 5);
            salida.setText(manager.mostrarInventario());
        });

        btnDevolucion.addActionListener(e -> {
            manager.devolver("Producto A", 2);
            salida.setText(manager.mostrarInventario());
        });

        controles.add(btnCompra);
        controles.add(btnVenta);
        controles.add(btnDevolucion);

        add(controles, BorderLayout.NORTH);
        add(new JScrollPane(salida), BorderLayout.CENTER);
    }
}