package app;

import model.Song;
import structures.PlaybackMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MusicLibraryManager {


    public interface LibraryChangeListener {
        void onLibraryChanged();
    }

    private PlaybackMode currentMode;
    private final List<LibraryChangeListener> listeners = new ArrayList<>();

    public MusicLibraryManager(PlaybackMode initialMode) {
        if (initialMode == null) {
            throw new IllegalArgumentException("El modo inicial no puede ser nulo.");
        }
        this.currentMode = initialMode;
    }


    public void addListener(LibraryChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(LibraryChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (LibraryChangeListener listener : listeners) {
            listener.onLibraryChanged();
        }
    }


    public void addSong(Song song) {
        validateSong(song);
        if (currentMode.getAllSongs().contains(song)) {
            throw new IllegalArgumentException(
                    "Ya existe una canción con ese título y artista en la biblioteca.");
        }
        currentMode.addSong(song);
        notifyListeners();
    }


    public void removeSong(Song song) {
        if (song == null) {
            return;
        }
        boolean removed = currentMode.removeSong(song);
        if (removed) {
            notifyListeners();
        }
    }


    public void editSong(Song original, Song edited) {
        if (original == null || edited == null) {
            throw new IllegalArgumentException("La canción original y la editada no pueden ser nulas.");
        }
        validateSong(edited);
        currentMode.removeSong(original);
        currentMode.addSong(edited);
        notifyListeners();
    }

    public void rateSong(Song song, int rating) {
        if (song == null) {
            throw new IllegalArgumentException("Selecciona una canción válida.");
        }
        song.setRating(rating);
        notifyListeners();
    }



    public Song next() {
        Song song = currentMode.next();
        notifyListeners();
        return song;
    }


    public Song previous() {
        Song song = currentMode.previous();
        notifyListeners();
        return song;
    }

    public Song getCurrentSong() {
        return currentMode.getCurrentSong();
    }

    public String getModeName() {
        return currentMode.getModeName();
    }


    public List<Song> getAllSongs() {
        return currentMode.getAllSongs();
    }

    public List<Song> searchSongs(String query) {
        if (query == null || query.isBlank()) {
            return currentMode.getAllSongs();
        }
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        List<Song> results = new ArrayList<>();
        for (Song song : currentMode.getAllSongs()) {
            if (containsIgnoreCase(song.getTitle(), normalized)
                    || containsIgnoreCase(song.getArtist(), normalized)
                    || containsIgnoreCase(song.getAlbum(), normalized)
                    || containsIgnoreCase(song.getGenre(), normalized)) {
                results.add(song);
            }
        }
        return results;
    }

    private boolean containsIgnoreCase(String field, String normalizedQuery) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }



    public void setMode(PlaybackMode newMode) {
        if (newMode == null) {
            throw new IllegalArgumentException("El nuevo modo no puede ser nulo.");
        }
        List<Song> songs = currentMode.getAllSongs();
        for (Song song : songs) {
            newMode.addSong(song);
        }
        this.currentMode = newMode;
        notifyListeners();
    }

    private void validateSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("La canción no puede ser nula.");
        }
        if (song.getTitle() == null || song.getTitle().isBlank()) {
            throw new IllegalArgumentException("El título de la canción no puede estar vacío.");
        }
        if (song.getArtist() == null || song.getArtist().isBlank()) {
            throw new IllegalArgumentException("El artista de la canción no puede estar vacío.");
        }
    }
}