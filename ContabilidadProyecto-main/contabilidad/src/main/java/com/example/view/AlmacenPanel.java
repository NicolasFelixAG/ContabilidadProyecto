package com.example.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AlmacenPanel extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private String metodo;
    private List<Object[]> inventario;
    private File archivo = new File("inventario.txt");

    public AlmacenPanel(String metodo, Runnable onBack) {
        this.metodo = metodo;
        this.inventario = new ArrayList<>();

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);

        // Panel superior con botón y título
        JPanel superior = new JPanel(new BorderLayout());
        superior.setBackground(Color.WHITE);

        JButton btnRegresar = new JButton("← Regresar");
        btnRegresar.setFocusPainted(false);
        btnRegresar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnRegresar.setBackground(new Color(220, 220, 220));
        btnRegresar.addActionListener(e -> onBack.run());

        JLabel titulo = new JLabel("📋 Tarjeta de Almacén", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        superior.add(btnRegresar, BorderLayout.WEST);
        superior.add(titulo, BorderLayout.CENTER);

        String[] columnas = {
                "Fecha", "Concepto", "Entradas", "Salidas", "Existencia",
                "Precio Unitario", "Promedio", "Debe", "Haber", "Saldo"
        };

        modelo = new DefaultTableModel(columnas, 0);
        tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(25);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(tabla);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnCompra = new JButton("Comprar");
        JButton btnVenta = new JButton("Vender");
        JButton btnDevolucion = new JButton("Devolver");

        btnCompra.addActionListener(e -> mostrarDialogoCompra());
        btnVenta.addActionListener(e -> mostrarDialogoVenta());
        btnDevolucion.addActionListener(e -> mostrarDialogoDevolucion());

        botones.add(btnCompra);
        botones.add(btnVenta);
        botones.add(btnDevolucion);

        add(superior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        cargarInventario();
    }

    private void mostrarDialogoCompra() {
        JTextField fecha = new JTextField();
        JTextField cantidad = new JTextField();
        JTextField precio = new JTextField();

        Object[] campos = {
                "Fecha:", fecha,
                "Cantidad:", cantidad,
                "Precio Unitario:", precio
        };

        int opcion = JOptionPane.showConfirmDialog(this, campos, "Compra", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            registrarOperacion(fecha.getText(), "Compra", Integer.parseInt(cantidad.getText()), 0, Double.parseDouble(precio.getText()));
        }
    }

    private void mostrarDialogoVenta() {
        JTextField fecha = new JTextField();
        JTextField cantidad = new JTextField();

        Object[] campos = {
                "Fecha:", fecha,
                "Cantidad:", cantidad
        };

        int opcion = JOptionPane.showConfirmDialog(this, campos, "Venta", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            registrarOperacion(fecha.getText(), "Venta", 0, Integer.parseInt(cantidad.getText()), 0);
        }
    }

    private void mostrarDialogoDevolucion() {
        JTextField fecha = new JTextField();
        JTextField cantidad = new JTextField();

        Object[] campos = {
                "Fecha:", fecha,
                "Cantidad:", cantidad
        };

        int opcion = JOptionPane.showConfirmDialog(this, campos, "Devolución", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            registrarOperacion(fecha.getText(), "Devolución", Integer.parseInt(cantidad.getText()), 0, 0);
        }
    }

    private void registrarOperacion(String fecha, String concepto, int entradas, int salidas, double unitario) {
    int existenciaAnterior = obtenerUltimaExistencia();
    int existencia = existenciaAnterior + entradas - salidas;

    double debe = entradas * unitario;
    double precioUnitarioMostrado = concepto.equals("Venta") ? obtenerUltimoPrecioUnitario() : unitario;
    double haber = salidas * obtenerUltimoPrecioUnitario();
    double saldo = obtenerUltimoSaldo() + debe - haber;

    modelo.addRow(new Object[]{
            fecha,
            concepto,
            entradas,
            salidas,
            existencia,
            precioUnitarioMostrado,
            "", 
            debe,
            haber,
            saldo
    });

    guardarInventario();
}


    private double obtenerUltimoPrecioUnitario() {
    int rowCount = modelo.getRowCount();
    for (int i = rowCount - 1; i >= 0; i--) {
        Object concepto = modelo.getValueAt(i, 1);
        if (concepto != null && concepto.toString().equalsIgnoreCase("Compra")) {
            Object precio = modelo.getValueAt(i, 5); // Columna 5 = Precio Unitario
            if (precio != null) {
                String str = precio.toString().replace("$", "").replace(",", "");
                try {
                    return Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
    }
    return 0;
}

    private double obtenerUltimoSaldo() {
        int rowCount = modelo.getRowCount();
        if (rowCount == 0) return 0;

        Object valor = modelo.getValueAt(rowCount - 1, 9);
        if (valor == null) return 0;

        String saldoStr = valor.toString().replace("$", "").replace(",", "");
        try {
            return Double.parseDouble(saldoStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int obtenerUltimaExistencia() {
    int rowCount = modelo.getRowCount();
    if (rowCount == 0) return 0;
    Object val = modelo.getValueAt(rowCount - 1, 4); // Columna 4 = existencia
    if (val == null) return 0;
    try {
        return Integer.parseInt(val.toString());
    } catch (NumberFormatException e) {
        return 0;
    }
}
    private void guardarInventario() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            for (int i = 0; i < modelo.getRowCount(); i++) {
                for (int j = 0; j < modelo.getColumnCount(); j++) {
                    writer.write(modelo.getValueAt(i, j).toString());
                    if (j < modelo.getColumnCount() - 1) writer.write(",");
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarInventario() {
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                modelo.addRow(partes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
