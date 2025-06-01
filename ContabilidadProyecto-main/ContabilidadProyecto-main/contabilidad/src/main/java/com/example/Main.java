package com.example;
import com.formdev.flatlaf.FlatLightLaf;
import com.example.view.AlmacenPanel;
import com.example.view.BienvenidaPanel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tarjeta de Almacén");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
        
            // Crear panel de bienvenida y definir acción de continuación
            final BienvenidaPanel[] bienvenida = new BienvenidaPanel[1];
            bienvenida[0] = new BienvenidaPanel(() -> {
            frame.setContentPane(new AlmacenPanel(
                bienvenida[0].getMetodo(),
                () -> { // Acción para regresar
                    frame.setContentPane(bienvenida[0]);
                    frame.setTitle("Tarjeta de Almacén");
                    frame.setSize(800, 600);
                    frame.setLocationRelativeTo(null);
                    frame.revalidate();
                    frame.repaint();
                }
            ));
            frame.setTitle("Tarjeta de Almacén - " + bienvenida[0].getMetodo());
            frame.setSize(1000, 600);
            frame.setLocationRelativeTo(null);
            frame.revalidate();
            frame.repaint();
        });


            // Mostrar panel de bienvenida
            frame.setContentPane(bienvenida[0]);
            frame.setVisible(true);
        });
    }
}
