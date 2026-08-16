package ui;

import app.MusicLibraryManager;
import model.Song;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


public class SongLibraryPanel extends JPanel implements MusicLibraryManager.LibraryChangeListener {

    private static final String[] COLUMN_NAMES = {
            "Título", "Artista", "Álbum", "Duración", "Género", "Año", "Calificación"
    };

    private final MusicLibraryManager libraryManager;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;


    private List<Song> currentRows = new ArrayList<>();

    public SongLibraryPanel(MusicLibraryManager libraryManager) {
        super(new BorderLayout(8, 8));
        if (libraryManager == null) {
            throw new IllegalArgumentException("libraryManager no puede ser nulo.");
        }
        this.libraryManager = libraryManager;
        setBorder(BorderFactory.createTitledBorder("Biblioteca de canciones"));

        buildUI();

        libraryManager.addListener(this);
        refreshTable();
    }

    private void buildUI() {
        add(buildSearchPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // la edición se hace por el formulario, no en la celda
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Buscar");
        JButton clearButton = new JButton("Mostrar todo");

        searchButton.addActionListener(this::onSearch);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });

        panel.add(new JLabel("Buscar:"));
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(clearButton);
        return panel;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Agregar");
        JButton editButton = new JButton("Editar");
        JButton removeButton = new JButton("Eliminar");
        JButton rateButton = new JButton("Calificar");

        addButton.addActionListener(this::onAdd);
        editButton.addActionListener(this::onEdit);
        removeButton.addActionListener(this::onRemove);
        rateButton.addActionListener(this::onRate);

        panel.add(addButton);
        panel.add(editButton);
        panel.add(removeButton);
        panel.add(rateButton);
        return panel;
    }


    private void onAdd(ActionEvent e) {
        Song newSong = SongFormDialog.showDialog(this, null);
        if (newSong == null) return;
        try {
            libraryManager.addSong(newSong);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void onEdit(ActionEvent e) {
        Song selected = getSelectedSong();
        if (selected == null) {
            showInfo("Selecciona una canción para editar.");
            return;
        }
        Song edited = SongFormDialog.showDialog(this, selected);
        if (edited == null) return;
        try {
            libraryManager.editSong(selected, edited);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void onRemove(ActionEvent e) {
        Song selected = getSelectedSong();
        if (selected == null) {
            showInfo("Selecciona una canción para eliminar.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar \"" + selected.getTitle() + "\"?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            libraryManager.removeSong(selected);
        }
    }

    private void onRate(ActionEvent e) {
        Song selected = getSelectedSong();
        if (selected == null) {
            showInfo("Selecciona una canción para calificar.");
            return;
        }
        String input = JOptionPane.showInputDialog(this,
                "Calificación (0-100) para \"" + selected.getTitle() + "\":",
                selected.getRating());
        if (input == null) return;
        try {
            int rating = Integer.parseInt(input.trim());
            libraryManager.rateSong(selected, rating);
        } catch (NumberFormatException ex) {
            showError("Ingresa un número entero válido entre 0 y 100.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void onSearch(ActionEvent e) {
        refreshTable();
    }


    @Override
    public void onLibraryChanged() {
        refreshTable();
    }


    private void refreshTable() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        currentRows = libraryManager.searchSongs(query);

        tableModel.setRowCount(0);
        for (Song song : currentRows) {
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

    private Song getSelectedSong() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentRows.size()) {
            return null;
        }
        return currentRows.get(row);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}