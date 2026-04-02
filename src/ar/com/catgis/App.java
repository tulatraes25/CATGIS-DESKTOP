package ar.com.catgis;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        JFrame frame = new JFrame("CATGIS Desktop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        JMenu menuCapas = new JMenu("Capas");
        JMenu menuVista = new JMenu("Vista");
        JMenu menuHerramientas = new JMenu("Herramientas");
        JMenu menuAyuda = new JMenu("Ayuda");

        menuBar.add(menuArchivo);
        menuBar.add(menuCapas);
        menuBar.add(menuVista);
        menuBar.add(menuHerramientas);
        menuBar.add(menuAyuda);
        frame.setJMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();
        toolBar.add(new JButton("Abrir"));
        toolBar.add(new JButton("Guardar"));
        toolBar.addSeparator();
        toolBar.add(new JButton("Zoom +"));
        toolBar.add(new JButton("Zoom -"));
        toolBar.add(new JButton("Mover"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolBar, BorderLayout.CENTER);

        JPanel panelCapas = new JPanel(new BorderLayout());
        panelCapas.setPreferredSize(new Dimension(260, 0));
        panelCapas.setBorder(BorderFactory.createTitledBorder("Capas"));

        DefaultListModel<String> capasModel = new DefaultListModel<>();
        capasModel.addElement("Sin capas cargadas");
        JList<String> listaCapas = new JList<>(capasModel);
        panelCapas.add(new JScrollPane(listaCapas), BorderLayout.CENTER);

        JPanel panelMapa = new JPanel(new BorderLayout());
        panelMapa.setBorder(BorderFactory.createTitledBorder("Mapa"));
        JLabel mapaLabel = new JLabel("Área de mapa CATGIS", SwingConstants.CENTER);
        panelMapa.add(mapaLabel, BorderLayout.CENTER);

        JLabel barraEstado = new JLabel("Listo");
        barraEstado.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(panelCapas, BorderLayout.WEST);
        frame.add(panelMapa, BorderLayout.CENTER);
        frame.add(barraEstado, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}