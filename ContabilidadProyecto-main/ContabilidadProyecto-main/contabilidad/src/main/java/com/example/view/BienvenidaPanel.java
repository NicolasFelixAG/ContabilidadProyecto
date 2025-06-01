package com.example.view;

import javax.swing.*;
import java.awt.*;

public class BienvenidaPanel extends JPanel {
    private String metodo;

    public BienvenidaPanel(Runnable onContinue) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Línea superior decorativa
        JPanel lineaSuperior = new JPanel();
        lineaSuperior.setBackground(new Color(22, 44, 105));
        lineaSuperior.setPreferredSize(new Dimension(20, 60)); // altura de la línea
     

        // Línea inferior decorativa
        JPanel lineaInferior = new JPanel();
        lineaInferior.setBackground(new Color(22, 44, 105));
        lineaInferior.setPreferredSize(new Dimension(20, 60)); // altura de la línea

        // Panel central con GridBagLayout para centrar el contenido
        JPanel contenidoCentral = new JPanel(new GridBagLayout());
        contenidoCentral.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Contenedor de contenido con BoxLayout
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(new Color(245, 245, 245));
        panelContenido.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Título
        JLabel titulo = new JLabel("Proyecto Tarjeta De Almacen", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Integrantes
        JLabel integrantes = new JLabel(
            "<html><div style='text-align: center;'>Proyecto desarrollado por:<br>"
            + "<b>Nicolás Felix Aguilasocho</b><br>"
            + "<b>Sahory Estrada Trejo</b><br>"
            + "<b>Baez Rubio Jose Saul</b><br>"
            + "<b>Rodriguez Cisneros Kevin Jasiel</b><br>"
            + "<b>Núñez Beltrán Darel Eduardo</div></html>",
            SwingConstants.CENTER
        );
        integrantes.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        integrantes.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Etiqueta de método
        JLabel metodoLabel = new JLabel("Elige un método:");
        metodoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        metodoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        botonesPanel.setBackground(new Color(245, 245, 245));

        String[] metodos = {"UEPS", "PEPS", "PROMEDIO"};
        for (String nombreMetodo : metodos) {
            JButton boton = new JButton(nombreMetodo);
            boton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            boton.setBackground(new Color(33, 40, 150));
            boton.setForeground(Color.WHITE);
            boton.setFocusPainted(false);
            boton.addActionListener(e -> {
                setMetodo(nombreMetodo);
                onContinue.run();
            });
            botonesPanel.add(boton);
        }

        // Añadir componentes al panel contenido
        panelContenido.add(titulo);
        panelContenido.add(Box.createVerticalStrut(10));
        panelContenido.add(integrantes);
        panelContenido.add(Box.createVerticalStrut(20));
        panelContenido.add(metodoLabel);
        panelContenido.add(Box.createVerticalStrut(10));
        panelContenido.add(botonesPanel);

        contenidoCentral.add(panelContenido, gbc);

        // Añadir todo al layout principal
        add(lineaSuperior, BorderLayout.NORTH);
        add(contenidoCentral, BorderLayout.CENTER);
        add(lineaInferior, BorderLayout.SOUTH);
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public String getMetodo() {
        return metodo;
    }
}
