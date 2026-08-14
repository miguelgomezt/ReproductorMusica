package ui;

import model.Song;
import structures.PlaybackMode;
import structures.CircularDoubleLinkedList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Ventana principal de la aplicación.
 * - Biblioteca de canciones (tabla).
 * - Información de la canción actual.
 * - Barra de progreso (simulada, no reproduce audio real).
 * - Botones: Agregar, Eliminar, Editar, Buscar, Reproducir, Pausar,
 *   Siguiente, Anterior.
 * - Selector del modo de reproducción.
 * - Portada de la canción actual.
 */

public class MainFrame extends JFrame {

    private PlaybackMode currentMode;

    // ---------- Componentes de biblioteca ----------
    private JTable libraryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    // ---------- Componentes de reproductor ----------
    private JLabel coverLabel;
    private JLabel titleLabel;
    private JLabel artistAlbumLabel;
    private JProgressBar progressBar;
    private Timer progressTimer;
    private int simulatedProgress;
    private boolean isPlaying;

    // ---------- Selector de modo ----------
    private JComboBox<String> modeSelector;

    private static final String[] COLUMN_NAMES = {
            "Título", "Artista", "Álbum", "Duración", "Género", "Año", "Calificación"
    };

    public MainFrame() {
        super("Reproductor - Lenguajes y Compiladores");

        currentMode = new CircularDoubleLinkedList();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildLibraryPanel(), BorderLayout.CENTER);
        add(buildPlayerPanel(), BorderLayout.SOUTH);

        loadSampleData();
    }

    // ==================== PANEL SUPERIOR: selector de modo + búsqueda ====================

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(new JLabel("Modo de reproducción:"));
        modeSelector = new JComboBox<>(new String[]{
                "Aleatorio (Lista Circular Doble)",
                "Orden de llegada (Cola Simple)",
                "Alfabético (Árbol Binario de Búsqueda)",
                "Por calificación (Priority Queue)"
        });
        modeSelector.addActionListener(this::onModeChanged);
        modePanel.add(modeSelector);
        topPanel.add(modePanel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(18);
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(this::onSearch);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.EAST);

        return topPanel;
    }

    // ==================== PANEL CENTRAL: biblioteca ====================

    private JPanel buildLibraryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Biblioteca de canciones"));

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        libraryTable = new JTable(tableModel);
        libraryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(libraryTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Agregar");
        JButton editButton = new JButton("Editar");
        JButton removeButton = new JButton("Eliminar");
        JButton rateButton = new JButton("Calificar");

        addButton.addActionListener(this::onAddSong);
        editButton.addActionListener(this::onEditSong);
        removeButton.addActionListener(this::onRemoveSong);
        rateButton.addActionListener(this::onRateSong);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(rateButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== PANEL INFERIOR: reproductor ====================

    private JPanel buildPlayerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Reproduciendo"));
        panel.setPreferredSize(new Dimension(0, 180));

        // Portada
        coverLabel = new JLabel();
        coverLabel.setPreferredSize(new Dimension(90, 90));
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        coverLabel.setText("Sin portada");
        panel.add(coverLabel, BorderLayout.WEST);

        // Info de la canción actual + progreso
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel("Ninguna canción reproduciéndose");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

        artistAlbumLabel = new JLabel(" ");

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        infoPanel.add(titleLabel);
        infoPanel.add(artistAlbumLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(progressBar);
        panel.add(infoPanel, BorderLayout.CENTER);

        // Controles
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevButton = new JButton("Anterior");
        JButton playButton = new JButton("Reproducir");
        JButton pauseButton = new JButton("Pausar");
        JButton nextButton = new JButton("Siguiente");

        prevButton.addActionListener(this::onPrevious);
        playButton.addActionListener(this::onPlay);
        pauseButton.addActionListener(this::onPause);
        nextButton.addActionListener(this::onNext);

        controlsPanel.add(prevButton);
        controlsPanel.add(playButton);
        controlsPanel.add(pauseButton);
        controlsPanel.add(nextButton);
        panel.add(controlsPanel, BorderLayout.SOUTH);

        progressTimer = new Timer(500, e -> updateSimulatedProgress());

        return panel;
    }

    // ==================== Manejo de eventos: biblioteca ====================

    private void onAddSong(ActionEvent e) {
        Song song = showSongFormDialog(null);
        if (song != null) {
            currentMode.addSong(song);
            refreshTable();
        }
    }

    private void onEditSong(ActionEvent e) {
        int row = libraryTable.getSelectedRow();
        if (row == -1) {
            showInfo("Selecciona una canción para editar.");
            return;
        }
        Song original = getSongAt(row);
        Song edited = showSongFormDialog(original);
        if (edited != null) {
            currentMode.removeSong(original);
            currentMode.addSong(edited);
            refreshTable();
        }
    }

    private void onRemoveSong(ActionEvent e) {
        int row = libraryTable.getSelectedRow();
        if (row == -1) {
            showInfo("Selecciona una canción para eliminar.");
            return;
        }
        Song song = getSongAt(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar \"" + song.getTitle() + "\"?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            currentMode.removeSong(song);
            refreshTable();
        }
    }

    private void onRateSong(ActionEvent e) {
        int row = libraryTable.getSelectedRow();
        if (row == -1) {
            showInfo("Selecciona una canción para calificar.");
            return;
        }
        Song song = getSongAt(row);
        String input = JOptionPane.showInputDialog(this,
                "Calificación (0-100) para \"" + song.getTitle() + "\":",
                song.getRating());
        if (input != null) {
            try {
                int rating = Integer.parseInt(input.trim());
                song.setRating(rating);
                refreshTable();
            } catch (NumberFormatException ex) {
                showInfo("Ingresa un número válido entre 0 y 100.");
            } catch (IllegalArgumentException ex) {
                showInfo(ex.getMessage());
            }
        }
    }

    private void onSearch(ActionEvent e) {
        String query = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        for (Song song : currentMode.getAllSongs()) {
            if (query.isEmpty()
                    || song.getTitle().toLowerCase().contains(query)
                    || song.getArtist().toLowerCase().contains(query)) {
                addRow(song);
            }
        }
    }

    /**
     * Formulario simple para agregar/editar una canción.
     * Si songToEdit es null, se trata de una canción nueva.
     */
    private Song showSongFormDialog(Song songToEdit) {
        JTextField titleField = new JTextField(songToEdit != null ? songToEdit.getTitle() : "");
        JTextField artistField = new JTextField(songToEdit != null ? songToEdit.getArtist() : "");
        JTextField albumField = new JTextField(songToEdit != null ? songToEdit.getAlbum() : "");
        JTextField durationField = new JTextField(songToEdit != null ? String.valueOf(songToEdit.getDurationInSeconds()) : "");
        JTextField genreField = new JTextField(songToEdit != null ? songToEdit.getGenre() : "");
        JTextField yearField = new JTextField(songToEdit != null ? String.valueOf(songToEdit.getReleaseYear()) : "");

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Título:"));
        form.add(titleField);
        form.add(new JLabel("Artista:"));
        form.add(artistField);
        form.add(new JLabel("Álbum:"));
        form.add(albumField);
        form.add(new JLabel("Duración (segundos):"));
        form.add(durationField);
        form.add(new JLabel("Género:"));
        form.add(genreField);
        form.add(new JLabel("Año:"));
        form.add(yearField);

        int result = JOptionPane.showConfirmDialog(this, form,
                songToEdit == null ? "Agregar canción" : "Editar canción",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return null;

        try {
            Song song = new Song(
                    titleField.getText().trim(),
                    artistField.getText().trim(),
                    albumField.getText().trim(),
                    Integer.parseInt(durationField.getText().trim()),
                    genreField.getText().trim(),
                    Integer.parseInt(yearField.getText().trim())
            );
            if (songToEdit != null) {
                song.setRating(songToEdit.getRating());
                song.setCoverImage(songToEdit.getCoverImage());
            }
            return song;
        } catch (NumberFormatException ex) {
            showInfo("Duración y año deben ser números válidos.");
            return null;
        }
    }

    // ==================== Manejo de eventos: reproductor ====================

    private void onPlay(ActionEvent e) {
        if (currentMode.getCurrentSong() == null) {
            Song first = currentMode.next();
            updateNowPlaying(first);
        } else {
            updateNowPlaying(currentMode.getCurrentSong());
        }
        isPlaying = true;
        progressTimer.start();
    }

    private void onPause(ActionEvent e) {
        isPlaying = false;
        progressTimer.stop();
    }

    private void onNext(ActionEvent e) {
        Song song = currentMode.next();
        updateNowPlaying(song);
    }

    private void onPrevious(ActionEvent e) {
        try {
            Song song = currentMode.previous();
            updateNowPlaying(song);
        } catch (UnsupportedOperationException ex) {
            showInfo("Este modo no permite regresar a canciones anteriores.");
        }
    }

    private void updateNowPlaying(Song song) {
        simulatedProgress = 0;
        progressBar.setValue(0);
        if (song == null) {
            titleLabel.setText("Ninguna canción reproduciéndose");
            artistAlbumLabel.setText(" ");
            coverLabel.setText("Sin portada");
            return;
        }
        titleLabel.setText(song.getTitle());
        artistAlbumLabel.setText(song.getArtist() + " · " + song.getAlbum());
        coverLabel.setText(song.getCoverImage() == null ? "Sin portada" : "");
    }

    private void updateSimulatedProgress() {
        if (!isPlaying) return;
        simulatedProgress = Math.min(100, simulatedProgress + 5);
        progressBar.setValue(simulatedProgress);
        if (simulatedProgress >= 100) {
            onNext(null); // pasa automáticamente a la siguiente al "terminar"
        }
    }

    // ==================== Cambio de modo ====================

    /**
     * TODO (equipo): cuando SimpleQueue, BinarySearchTree y PriorityQueueMode
     * estén listas, reemplazar este switch para instanciar la estructura
     * correspondiente y transferirle las canciones actuales.
     */
    private void onModeChanged(ActionEvent e) {
        String selected = (String) modeSelector.getSelectedItem();
        List<Song> songs = currentMode.getAllSongs();

        if ("Aleatorio (Lista Circular Doble)".equals(selected)) {
            currentMode = new CircularDoubleLinkedList();
            for (Song s : songs) currentMode.addSong(s);
        } else {
            showInfo("Este modo aún no está implementado por el equipo.\nSeleccionando de vuelta el modo aleatorio.");
            modeSelector.setSelectedIndex(0);
            return;
        }

        refreshTable();
        updateNowPlaying(null);
    }

    // ==================== Utilidades ====================

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Song song : currentMode.getAllSongs()) {
            addRow(song);
        }
    }

    private void addRow(Song song) {
        tableModel.addRow(new Object[]{
                song.getTitle(), song.getArtist(), song.getAlbum(),
                song.getFormattedDuration(), song.getGenre(),
                song.getReleaseYear(), song.getRating()
        });
    }

    private Song getSongAt(int row) {
        String title = (String) tableModel.getValueAt(row, 0);
        String artist = (String) tableModel.getValueAt(row, 1);
        for (Song song : currentMode.getAllSongs()) {
            if (song.getTitle().equals(title) && song.getArtist().equals(artist)) {
                return song;
            }
        }
        return null;
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    /** Carga un par de canciones de ejemplo para poder probar la app de inmediato. */
    private void loadSampleData() {
        currentMode.addSong(new Song("Bohemian Rhapsody", "Queen", "A Night at the Opera", 355, "Rock", 1975));
        currentMode.addSong(new Song("Blinding Lights", "The Weeknd", "After Hours", 200, "Synthpop", 2020));
        currentMode.addSong(new Song("Ojos Así", "Shakira", "¿Dónde Están los Ladrones?", 246, "Pop", 1998));
        refreshTable();
    }

    public PlaybackMode getCurrentMode() {
        return currentMode;
    }
}