package ui;

import app.MusicLibraryManager;
import structures.PlaybackMode;
import structures.CircularDoubleLinkedList;
import structures.SimpleQueue;

import structures.BinarySearchTree;
import structures.PriorityQueueMode;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Ventana principal de la aplicación.
 *
 * Responsabilidad ÚNICA de esta clase:
 * - Armar el layout general (selector de modo arriba, biblioteca al centro,
 *   reproductor abajo).
 * - Mantener la referencia al MusicLibraryManager (que a su vez mantiene el
 *   PlaybackMode activo).
 * - Cambiar de estructura de datos cuando el usuario cambia de modo.
 *
 * Esta clase NO contiene lógica de biblioteca (agregar/editar/eliminar
 * canciones) ni lógica de reproducción (play/pause/progreso) — eso vive en
 * SongLibraryPanel y PlayerPanel
 */
public class MainFrame extends JFrame {

    private MusicLibraryManager libraryManager;
    private JComboBox<String> modeSelector;

    private SongLibraryPanel libraryPanel;
     private PlayerPanel playerPanel;

    public MainFrame() {
        super("Reproductor - Lenguajes y Compiladores");

        PlaybackMode initialMode = new CircularDoubleLinkedList();
        libraryManager = new MusicLibraryManager(initialMode);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildLibraryPanel(), BorderLayout.CENTER);
        add(buildPlayerPanel(), BorderLayout.SOUTH);

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

    // ==================== PANEL CENTRAL: biblioteca (ya conectada) ====================

    private SongLibraryPanel buildLibraryPanel() {
        libraryPanel = new SongLibraryPanel(libraryManager);
        return libraryPanel;
    }

    // ==================== PANEL INFERIOR: reproductor (placeholder, Persona 3) ====================


    private PlayerPanel buildPlayerPanel() {
        playerPanel = new PlayerPanel(libraryManager);
        return playerPanel;
    }

    // ==================== Cambio de modo ====================

    /**
     * Se ejecuta cuando el usuario cambia el modo en el selector.
     * Le pide a MusicLibraryManager que cambie de estructura de datos;
     * el manager se encarga de transferir las canciones y notificar
     * a los paneles suscritos (como SongLibraryPanel) para que se
     * refresquen solos, gracias al patrón Observer que ya implementó
     * Persona 2 (LibraryChangeListener).
     *
     * TODO (equipo): completar el switch cuando BinarySearchTree
     * y PriorityQueueMode implementen PlaybackMode correctamente.
     */
    private void onModeChanged(ActionEvent e) {
        String selected = (String) modeSelector.getSelectedItem();

        switch (selected) {
            case "Aleatorio (Lista Circular Doble)":
                libraryManager.setMode(new CircularDoubleLinkedList());
                break;
            case "Orden de llegada (Cola Simple)":
                libraryManager.setMode(new SimpleQueue());
                break;
            case "Alfabético (Árbol Binario de Búsqueda)":
                libraryManager.setMode(new BinarySearchTree());
                break;
            case "Por calificación (Priority Queue)":
                libraryManager.setMode(new PriorityQueueMode());
                break;
        }
    }

    // ==================== Utilidades ====================

    private void loadSampleData() {
        libraryManager.addSong(new model.Song("Bohemian Rhapsody", "Queen", "A Night at the Opera", 355, "Rock", 1975));
        libraryManager.addSong(new model.Song("Blinding Lights", "The Weeknd", "After Hours", 200, "Synthpop", 2020));
        libraryManager.addSong(new model.Song("Ojos Así", "Shakira", "¿Dónde Están los Ladrones?", 246, "Pop", 1998));
    }

    public MusicLibraryManager getLibraryManager() {
        return libraryManager;
    }
}