package com.example.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.model.Lote; // Asegúrate que Lote está en com.example.model

public class AlmacenPanel extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private String metodo;  // "PEPS" o "UEPS"
    private List<Lote> lotes = new ArrayList<>();  // Lista para manejar lotes de compra
    private File archivo = new File("inventario.txt");

    // --- Clases auxiliares para el detalle de la venta ---
    public static class VentaDetalleLote {
        public final int cantidadVendida;
        public final double precioUnitario;
        public final double costoDeEsteDetalle;

        public VentaDetalleLote(int cantidadVendida, double precioUnitario, double costoDeEsteDetalle) {
            this.cantidadVendida = cantidadVendida;
            this.precioUnitario = precioUnitario;
            this.costoDeEsteDetalle = costoDeEsteDetalle;
        }
    }

    public static class ResultadoDescuentoLotes {
        public final double costoTotalVentaGeneral;
        public final List<VentaDetalleLote> detallePorLoteConsumido;

        public ResultadoDescuentoLotes(double costoTotalVentaGeneral, List<VentaDetalleLote> detallePorLoteConsumido) {
            this.costoTotalVentaGeneral = costoTotalVentaGeneral;
            this.detallePorLoteConsumido = detallePorLoteConsumido;
        }
    }

    public static class InventarioInconsistenteException extends RuntimeException {
        public InventarioInconsistenteException(String message) {
            super(message);
        }
    }
    // --- Fin de clases auxiliares ---

    public AlmacenPanel(String metodo, Runnable onBack) {
        this.metodo = metodo;
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

        JLabel titulo = new JLabel("📋 Tarjeta de Almacén (" + metodo + ")", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        superior.add(btnRegresar, BorderLayout.WEST);
        superior.add(titulo, BorderLayout.CENTER);

        String[] columnas = {
                "Fecha", "Concepto", "Entradas", "Salidas", "Existencia",
                "Precio Unitario", "Debe", "Haber", "Saldo"
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
        JTextField fechaField = new JTextField();
        JTextField cantidadField = new JTextField();
        JTextField precioField = new JTextField();

        Object[] campos = {
            "Fecha:", fechaField,
            "Cantidad:", cantidadField,
            "Precio Unitario:", precioField
        };

        int opcion = JOptionPane.showConfirmDialog(this, campos, "Compra", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                String fecha = fechaField.getText();
                if (fecha.isBlank() || cantidadField.getText().isBlank() || precioField.getText().isBlank()) {
                    throw new IllegalArgumentException("Ningún campo puede estar vacío.");
                }

                int cant = Integer.parseInt(cantidadField.getText());
                double pu = Double.parseDouble(precioField.getText());

                if (cant <= 0 || pu <= 0) {
                    throw new IllegalArgumentException("Cantidad y precio deben ser mayores a cero.");
                }

                registrarOperacion(fecha, "Compra", cant, 0, pu);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Cantidad y precio deben ser números válidos.", "Entrada inválida", JOptionPane.WARNING_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error inesperado al registrar la compra:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void mostrarDialogoVenta() {
        JTextField fechaField = new JTextField();
        JTextField cantidadField = new JTextField();

        Object[] campos = {
            "Fecha:", fechaField,
            "Cantidad:", cantidadField
        };

        int opcion = JOptionPane.showConfirmDialog(this, campos, "Venta", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                String fecha = fechaField.getText();
                if (fecha.isBlank() || cantidadField.getText().isBlank()) {
                    throw new IllegalArgumentException("Ningún campo puede estar vacío.");
                }

                int cantidadAVender = Integer.parseInt(cantidadField.getText());

                if (cantidadAVender <= 0) {
                    throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
                }

                if (totalCantidad() < cantidadAVender) {
                    JOptionPane.showMessageDialog(this,
                        "No hay suficiente inventario para vender " + cantidadAVender + " unidades. Existencia total: " + totalCantidad() + ".",
                        "Inventario insuficiente",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Llama al método modificado que devuelve el detalle
                ResultadoDescuentoLotes resultadoVenta = procesarSalidaDeLotes(cantidadAVender); // Renombrado para claridad

                // Llama a un nuevo método para registrar los detalles en la tabla
                registrarVentaConDetallesEnTabla(fecha, resultadoVenta);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser un número válido.", "Entrada inválida", JOptionPane.WARNING_MESSAGE);
            } catch (IllegalArgumentException | InventarioInconsistenteException | IllegalStateException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error en Venta", JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error inesperado al registrar la venta:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void mostrarDialogoDevolucion() {
        JTextField fechaField = new JTextField();
        JTextField cantidadField = new JTextField();

        Object[] campos = {
            "Fecha:", fechaField,
            "Cantidad:", cantidadField
        };

        int opcion = JOptionPane.showConfirmDialog(this, campos, "Devolución", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                String fecha = fechaField.getText();
                if (fecha.isBlank() || cantidadField.getText().isBlank()) {
                    throw new IllegalArgumentException("Ningún campo puede estar vacío.");
                }

                int cant = Integer.parseInt(cantidadField.getText());

                if (cant <= 0) {
                    throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
                }

                // Política para devoluciones: se reintegran al costo promedio actual,
                // o al último costo de compra, o a un costo específico.
                // Usaremos el costo promedio actual del inventario ANTES de la devolución.
                double precioParaDevolucion = calcularPrecioPromedio();
                if (lotes.isEmpty() && precioParaDevolucion == 0) { // Si no hay inventario, no se puede promediar. Pedir costo.
                    String costoDevStr = JOptionPane.showInputDialog(this, "Inventario vacío. Ingrese el costo unitario para esta devolución:", "Costo Devolución", JOptionPane.QUESTION_MESSAGE);
                    if (costoDevStr == null || costoDevStr.isBlank()) throw new IllegalArgumentException("Se requiere costo para devolución en inventario vacío.");
                    precioParaDevolucion = Double.parseDouble(costoDevStr);
                    if(precioParaDevolucion <=0) throw new IllegalArgumentException("El costo de devolución debe ser mayor a cero.");
                }


                // IMPORTANTE: Agregar el lote a 'this.lotes' ANTES de llamar a registrarOperacion,
                // para que calcularPrecioPromedio() dentro de registrarOperacion lo considere.
                lotes.add(new Lote(cant, precioParaDevolucion));

                registrarOperacion(fecha, "Devolución", cant, 0, precioParaDevolucion);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Cantidad o costo deben ser números válidos.", "Entrada inválida", JOptionPane.WARNING_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error inesperado al registrar la devolución:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    // Método para procesar la salida de lotes (antes descontarLotes)
    private ResultadoDescuentoLotes procesarSalidaDeLotes(int cantidadAVender) {
        double costoTotalVentaGlobal = 0;
        int cantidadPendientePorDescontar = cantidadAVender;
        List<VentaDetalleLote> detallesDeVenta = new ArrayList<>();

        List<Lote> lotesParaProcesar;

        if (this.metodo == null) {
            throw new IllegalStateException("El método de costeo (PEPS/UEPS) no ha sido inicializado.");
        }

        if (this.metodo.equalsIgnoreCase("PEPS")) {
            lotesParaProcesar = new ArrayList<>(this.lotes);
        } else if (this.metodo.equalsIgnoreCase("UEPS")) {
            lotesParaProcesar = new ArrayList<>(this.lotes);
            Collections.reverse(lotesParaProcesar);
        } else if (this.metodo.equalsIgnoreCase("PROMEDIO")) {
            // Para el método promedio, no se procesan lotes específicos, sino que se calcula un costo promedio.
            double costoPromedio = calcularPrecioPromedio();
            if (costoPromedio <= 0) {
                throw new IllegalStateException("No hay lotes disponibles para calcular el costo promedio.");
            }
            int cantidadTotal = totalCantidad();
            if (cantidadTotal < cantidadAVender) {
                throw new InventarioInconsistenteException(
                        "No hay suficiente inventario para vender " + cantidadAVender + " unidades. Existencia total: " + cantidadTotal + ".");
            }
            // Si es promedio, simplemente calculamos el costo total y retornamos un resultado único
            double costoTotalVenta = cantidadAVender * costoPromedio;
            detallesDeVenta.add(new VentaDetalleLote(cantidadAVender, costoPromedio, costoTotalVenta));
            this.lotes.clear(); // Limpiar lotes ya que se usa el promedio
            return new ResultadoDescuentoLotes(costoTotalVenta, detallesDeVenta);
        } else {
            throw new IllegalArgumentException("Método de inventario no reconocido: '" + this.metodo + "'. Use PEPS o UEPS.");
        }

        for (Lote loteActual : lotesParaProcesar) {
            if (cantidadPendientePorDescontar <= 0) {
                break;
            }

            if (loteActual.getCantidad() > 0) {
                int cantidadTomadaDeLote = Math.min(cantidadPendientePorDescontar, loteActual.getCantidad());
                double precioDelLote = loteActual.getPrecio(); // Costo del lote
                double costoParcialEsteLote = cantidadTomadaDeLote * precioDelLote;

                costoTotalVentaGlobal += costoParcialEsteLote;
                detallesDeVenta.add(new VentaDetalleLote(cantidadTomadaDeLote, precioDelLote, costoParcialEsteLote));

                loteActual.setCantidad(loteActual.getCantidad() - cantidadTomadaDeLote);
                cantidadPendientePorDescontar -= cantidadTomadaDeLote;
            }
        }

        if (cantidadPendientePorDescontar > 0) {
            throw new InventarioInconsistenteException(
                    "Error interno: No se pudo procesar la salida total de " + cantidadAVender +
                    " unidades. Quedaron pendientes " + cantidadPendientePorDescontar + " unidades. " +
                    "Verifique la lógica de cálculo de inventario total.");
        }

        this.lotes.removeIf(l -> l.getCantidad() == 0);

        return new ResultadoDescuentoLotes(costoTotalVentaGlobal, detallesDeVenta);
    }


    // NUEVO: Registra los detalles de una venta (posiblemente múltiples filas)
    private void registrarVentaConDetallesEnTabla(String fecha, ResultadoDescuentoLotes resultadoDescuento) {

        for (VentaDetalleLote detalleLote : resultadoDescuento.detallePorLoteConsumido) {
            int existenciaAnteriorEnTabla = obtenerUltimaExistencia();
            double saldoAnteriorMonetarioEnTabla = obtenerUltimoSaldo();

            int cantidadSalidaEsteDetalle = detalleLote.cantidadVendida;
            double precioUnitarioEsteDetalle = detalleLote.precioUnitario; // Costo unitario del lote específico
            double costoHaberEsteDetalle = detalleLote.costoDeEsteDetalle;  // Costo total de esta parte de la venta

            int existenciaActualParaEstaLinea = existenciaAnteriorEnTabla - cantidadSalidaEsteDetalle;
            double saldoActualMonetarioParaEstaLinea = saldoAnteriorMonetarioEnTabla - costoHaberEsteDetalle;

            modelo.addRow(new Object[]{
                    fecha,
                    "Venta",
                    0,       // Entradas
                    cantidadSalidaEsteDetalle,
                    existenciaActualParaEstaLinea,
                    String.format("$%.2f", precioUnitarioEsteDetalle),
                    String.format("$%.2f", 0.0), // Debe
                    String.format("$%.2f", costoHaberEsteDetalle),
                    String.format("$%.2f", saldoActualMonetarioParaEstaLinea)
            });
        }
        guardarInventario(); // Guardar después de registrar todas las partes de la venta
    }


    // Registra operaciones de compra y devolución (entradas)
    private void registrarOperacion(String fecha, String concepto, int entradas, int salidasOperacion, double unitarioTransaccion) {
        int existenciaAnteriorEnTabla = obtenerUltimaExistencia();
        int existenciaActual = existenciaAnteriorEnTabla + entradas - salidasOperacion;

        double debe = 0;
        double haber = 0; // Para entradas, el 'haber' en costo es 0
        double saldoAnteriorMonetarioEnTabla = obtenerUltimoSaldo();
        double saldoActualMonetario = saldoAnteriorMonetarioEnTabla;

        if (concepto.equalsIgnoreCase("Compra")) {
            debe = entradas * unitarioTransaccion;
            saldoActualMonetario = saldoAnteriorMonetarioEnTabla + debe;
            // El lote se añade en 'mostrarDialogoCompra' ANTES de llamar aquí,
            // O se añade aquí. Para consistencia, lo manejaremos aquí.
            // La llamada original desde mostrarDialogoCompra no añade a this.lotes, así que se añade aquí.
            lotes.add(new Lote(entradas, unitarioTransaccion));
        } else if (concepto.equalsIgnoreCase("Devolución")) {
            debe = entradas * unitarioTransaccion;
            saldoActualMonetario = saldoAnteriorMonetarioEnTabla + debe;
            // El lote para devolución ya fue agregado en mostrarDialogoDevolucion ANTES de llamar aquí.
        }

        // 'lotes' ha sido actualizado por Compra (aquí) o Devolución (en el llamador)

        try {
            modelo.addRow(new Object[]{
                    fecha,
                    concepto,
                    entradas,
                    salidasOperacion, // Será 0 para Compra/Devolución
                    existenciaActual,
                    String.format("$%.2f", unitarioTransaccion),
                    String.format("$%.2f", debe),
                    String.format("$%.2f", haber), // Haber es 0 para entradas de inventario
                    String.format("$%.2f", saldoActualMonetario)
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al agregar fila a la tabla:\n" + e.getMessage(), "Error de GUI", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        guardarInventario();
    }

    // El método original 'registrarOperacionVenta' ya no es necesario si se usa 'registrarVentaConDetallesEnTabla'.
    // Se puede eliminar o comentar.
    /*
    private void registrarOperacionVenta(String fecha, int salidas, double costoTotal) {
        // ... código anterior ...
    }
    */

    // --- Métodos auxiliares existentes ---
    private double obtenerUltimoSaldo() {
        int filas = modelo.getRowCount();
        if (filas == 0) return 0;
        Object saldoObj = modelo.getValueAt(filas - 1, 8); // Columna Saldo
        if (saldoObj != null) {
            String str = saldoObj.toString().replace("$", "").replace(",", "");
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private int obtenerUltimaExistencia() {
        int filas = modelo.getRowCount();
        if (filas == 0) return 0;
        Object existenciaObj = modelo.getValueAt(filas - 1, 4); // Columna Existencia
        if (existenciaObj != null) {
            try {
                return Integer.parseInt(existenciaObj.toString());
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private double calcularPrecioPromedio() {
        if (lotes.isEmpty()) {
            return 0;
        }
        double sumaCostosValorados = 0;
        int cantidadTotalEnLotes = 0;
        for (Lote lote : lotes) {
            sumaCostosValorados += lote.getCantidad() * lote.getPrecio();
            cantidadTotalEnLotes += lote.getCantidad();
        }
        return cantidadTotalEnLotes == 0 ? 0 : sumaCostosValorados / cantidadTotalEnLotes;
    }

    private int totalCantidad() {
        int suma = 0;
        for (Lote lote : lotes) {
            suma += lote.getCantidad();
        }
        return suma;
    }

    private void guardarInventario() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
            int filas = modelo.getRowCount();
            int columnas = modelo.getColumnCount();
            for (int i = 0; i < filas; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < columnas; j++) {
                    Object valor = modelo.getValueAt(i, j);
                    sb.append(valor == null ? "" : valor.toString());
                    if (j < columnas - 1) sb.append(",");
                }
                pw.println(sb.toString());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar inventario:\n" + e.getMessage(), "Error de archivo", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cargarInventario() {
        // ADVERTENCIA IMPORTANTE:
        // La lógica actual de cargarInventario reconstruye 'this.lotes' ÚNICAMENTE desde las compras.
        // Esto significa que el estado real del inventario (cantidades reducidas por ventas) no se
        // restaura correctamente entre sesiones si solo se depende de este método para 'this.lotes'.
        // Para una persistencia correcta, 'this.lotes' (con sus cantidades actualizadas) debería
        // guardarse y cargarse directamente, o el estado del inventario debería reconstruirse
        // procesando TODAS las transacciones (compras Y ventas) del archivo.
        // Esta es una limitación significativa en la persistencia actual.

        if (!archivo.exists()) return;

        // Limpiar lotes existentes antes de cargar para evitar duplicados si se llama múltiples veces sin reiniciar
        lotes.clear(); 
        // Limpiar tabla antes de cargar
        while(modelo.getRowCount() > 0) {
            modelo.removeRow(0);
        }


        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",", -1); // -1 para incluir trailing empty strings
                if (partes.length == modelo.getColumnCount()) { // Asegurar que la línea tiene el número correcto de columnas
                    modelo.addRow(partes);

                    // Lógica de reconstrucción de 'this.lotes' (simplificada, ver advertencia arriba)
                    String concepto = partes[1];
                    if (concepto != null && concepto.equalsIgnoreCase("Compra")) {
                        try {
                            int entradas = Integer.parseInt(partes[2].trim().isEmpty() ? "0" : partes[2].trim());
                            String puStr = partes[5].replace("$", "").trim();
                            double precioUnitario = puStr.isEmpty() ? 0 : Double.parseDouble(puStr);

                            if (entradas > 0 && precioUnitario > 0) {
                                lotes.add(new Lote(entradas, precioUnitario));
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Error al parsear datos de compra al cargar: " + linea + " - " + e.getMessage());
                        }
                    }
                    // Para una reconstrucción completa de 'lotes', necesitarías simular también las ventas
                    // y devoluciones sobre los lotes cargados de las compras.
                } else {
                     System.err.println("Línea ignorada en inventario.txt (número incorrecto de columnas): " + linea);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar inventario:\n" + e.getMessage(), "Error de archivo", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}