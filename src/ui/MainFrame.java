package ui;

import model.Song;
import structures.PlaybackMode;
import structures.CircularDoubleLinkedList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Ventana principal de la aplicación.
 *
 * Responsabilidad ÚNICA de esta clase:
 * - Armar el layout general (selector de modo arriba, biblioteca al centro,
 *   reproductor abajo).
 * - Mantener la referencia al PlaybackMode actualmente activo.
 * - Cambiar de estructura de datos cuando el usuario cambia de modo, y
 *   notificar a los paneles para que se actualicen.
 *
 * Esta clase NO debe contener lógica de biblioteca (agregar/editar/eliminar
 * canciones) ni lógica de reproducción (play/pause/progreso) — eso vive en
 * SongLibraryPanel (Persona 2) y PlayerPanel (Persona 3) respectivamente.
 */
public class MainFrame extends JFrame {

    private PlaybackMode currentMode;
    private JComboBox<String> modeSelector;

    // Referencias a los paneles de los compañeros.
    // NOTA (equipo): reemplazar los placeholders de abajo por estas clases
    // reales una vez estén listas.
    // private SongLibraryPanel libraryPanel;
    // private PlayerPanel playerPanel;

    public MainFrame() {
        super("Reproductor - Lenguajes y Compiladores");

        currentMode = new CircularDoubleLinkedList();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildLibraryPlaceholder(), BorderLayout.CENTER);
        add(buildPlayerPlaceholder(), BorderLayout.SOUTH);

        loadSampleData();
    }

    // ==================== PANEL SUPERIOR: selector de modo ====================

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Modo de reproducción:"));

        modeSelector = new JComboBox<>(new String[]{
                "Aleatorio (Lista Circular Doble)",
                "Orden de llegada (Cola Simple)",
                "Alfabético (Árbol Binario de Búsqueda)",
                "Por calificación (Priority Queue)"
        });
        modeSelector.addActionListener(this::onModeChanged);
        topPanel.add(modeSelector);

        return topPanel;
    }

    // ==================== PLACEHOLDERS (reemplazar por las clases reales) ====================

    /**
     * TODO (equipo): reemplazar este placeholder por:
     * libraryPanel = new SongLibraryPanel(currentMode);
     * return libraryPanel;
     */
    private JPanel buildLibraryPlaceholder() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Biblioteca (Persona 2)"));
        panel.add(new JLabel("Aquí va SongLibraryPanel", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    /**
     * TODO (equipo): reemplazar este placeholder por:
     * playerPanel = new PlayerPanel(currentMode);
     * return playerPanel;
     */
    private JPanel buildPlayerPlaceholder() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Reproductor (Persona 3)"));
        panel.add(new JLabel("Aquí va PlayerPanel", SwingConstants.CENTER), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 150));
        return panel;
    }

    // ==================== Cambio de modo ====================

    /**
     * Se ejecuta cuando el usuario cambia el modo en el selector.
     * Transfiere las canciones actuales a la nueva estructura de datos
     * y notifica a los paneles para que se refresquen.
     *
     * TODO (equipo): completar el switch cuando SimpleQueue,
     * BinarySearchTree y PriorityQueueMode estén listas.
     */
    private void onModeChanged(ActionEvent e) {
        String selected = (String) modeSelector.getSelectedItem();
        List<Song> songs = currentMode.getAllSongs();

        switch (selected) {
            case "Aleatorio (Lista Circular Doble)":
                currentMode = new CircularDoubleLinkedList();
                break;
            case "Orden de llegada (Cola Simple)":
                // currentMode = new SimpleQueue();
                showNotImplemented();
                return;
            case "Alfabético (Árbol Binario de Búsqueda)":
                // currentMode = new BinarySearchTree();
                showNotImplemented();
                return;
            case "Por calificación (Priority Queue)":
                // currentMode = new PriorityQueueMode();
                showNotImplemented();
                return;
        }

        for (Song s : songs) {
            currentMode.addSong(s);
        }

        // TODO (equipo): cuando existan los paneles reales, notificarles
        // el cambio de modo, por ejemplo:
        // libraryPanel.setMode(currentMode);
        // playerPanel.setMode(currentMode);
    }

    private void showNotImplemented() {
        JOptionPane.showMessageDialog(this,
                "Este modo aún no está implementado por el equipo.");
        modeSelector.setSelectedIndex(0);
    }

    // ==================== Utilidades ====================

    /** Carga canciones de ejemplo para poder probar la app mientras se integra todo. */
    private void loadSampleData() {
        currentMode.addSong(new Song("Bohemian Rhapsody", "Queen", "A Night at the Opera", 355, "Rock", 1975));
        currentMode.addSong(new Song("Blinding Lights", "The Weeknd", "After Hours", 200, "Synthpop", 2020));
        currentMode.addSong(new Song("Ojos Así", "Shakira", "¿Dónde Están los Ladrones?", 246, "Pop", 1998));
    }

    public PlaybackMode getCurrentMode() {
        return currentMode;
    }
}