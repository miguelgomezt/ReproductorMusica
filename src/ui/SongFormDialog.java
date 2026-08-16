package ui;

import model.Song;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;


public class SongFormDialog extends JDialog {

    private final JTextField titleField = new JTextField(20);
    private final JTextField artistField = new JTextField(20);
    private final JTextField albumField = new JTextField(20);
    private final JTextField durationField = new JTextField(6);
    private final JTextField genreField = new JTextField(15);
    private final JTextField yearField = new JTextField(6);
    private final JSpinner ratingSpinner =
            new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JLabel coverPathLabel = new JLabel("Sin portada");

    private String selectedCoverPath;
    private Song result;

    private SongFormDialog(Window owner, Song songToEdit) {
        super(owner, songToEdit == null ? "Agregar canción" : "Editar canción",
                ModalityType.APPLICATION_MODAL);
        buildUI();
        if (songToEdit != null) {
            populateFields(songToEdit);
        }
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }


    public static Song showDialog(Component parent, Song songToEdit) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        SongFormDialog dialog = new SongFormDialog(owner, songToEdit);
        dialog.setVisible(true); // modal: se bloquea aquí hasta dispose()
        return dialog.result;
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, c, 0, "Título:", titleField);
        addRow(form, c, 1, "Artista:", artistField);
        addRow(form, c, 2, "Álbum:", albumField);
        addRow(form, c, 3, "Duración (segundos):", durationField);
        addRow(form, c, 4, "Género:", genreField);
        addRow(form, c, 5, "Año de lanzamiento:", yearField);
        addRow(form, c, 6, "Calificación (0-100):", ratingSpinner);

        c.gridx = 0;
        c.gridy = 7;
        c.weightx = 0;
        form.add(new JLabel("Portada:"), c);

        JPanel coverPanel = new JPanel(new BorderLayout(6, 0));
        JButton coverButton = new JButton("Seleccionar...");
        coverButton.addActionListener(e -> onSelectCover());
        coverPanel.add(coverPathLabel, BorderLayout.CENTER);
        coverPanel.add(coverButton, BorderLayout.EAST);
        c.gridx = 1;
        c.gridy = 7;
        c.weightx = 1;
        form.add(coverPanel, c);

        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.gridy = row;
        c.weightx = 1;
        form.add(field, c);
    }

    private void populateFields(Song song) {
        titleField.setText(song.getTitle());
        artistField.setText(song.getArtist());
        albumField.setText(song.getAlbum());
        durationField.setText(String.valueOf(song.getDurationInSeconds()));
        genreField.setText(song.getGenre());
        yearField.setText(String.valueOf(song.getReleaseYear()));
        ratingSpinner.setValue(song.getRating());
        selectedCoverPath = song.getCoverImage();
        updateCoverLabel();
    }

    private void onSelectCover() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imágenes (jpg, jpeg, png, gif)", "jpg", "jpeg", "png", "gif"));
        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            selectedCoverPath = chooser.getSelectedFile().getAbsolutePath();
            updateCoverLabel();
        }
    }

    private void updateCoverLabel() {
        coverPathLabel.setText(selectedCoverPath == null
                ? "Sin portada"
                : new File(selectedCoverPath).getName());
    }

    private void onSave() {
        try {
            this.result = buildSongFromFields();
            dispose();
        } catch (NumberFormatException ex) {
            showError("Duración, año y calificación deben ser números enteros válidos.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private Song buildSongFromFields() {
        String title = titleField.getText().trim();
        String artist = artistField.getText().trim();
        String album = albumField.getText().trim();
        String genre = genreField.getText().trim();

        if (title.isEmpty()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        if (artist.isEmpty()) {
            throw new IllegalArgumentException("El artista es obligatorio.");
        }

        int duration = Integer.parseInt(durationField.getText().trim());
        int year = Integer.parseInt(yearField.getText().trim());
        if (duration < 0) {
            throw new IllegalArgumentException("La duración no puede ser negativa.");
        }

        Song song = new Song(title, artist, album, duration, genre, year);
        song.setRating((Integer) ratingSpinner.getValue());
        song.setCoverImage(selectedCoverPath);
        return song;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Datos inválidos", JOptionPane.ERROR_MESSAGE);
    }
}