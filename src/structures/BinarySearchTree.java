package structures;

import model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Árbol Binario de Búsqueda (BST), implementado desde cero, usado por el
 * Modo 3: "Reproducción alfabética". Implementa la interfaz real del
 * equipo, structures.PlaybackMode.
 *
 *
 * MANEJO DE EMPATES: si dos canciones tienen el mismo título pero
 * distinto artista, se desempata con Song.equals() (título + artista)
 * para que buscar/eliminar no confundan una canción con otra (ver
 * comentarios en eliminarRecursivo/containsRecursivo).
 *
 * COMPLEJIDAD TEMPORAL (para sustentación)
 * ---------------------------------------------------------------------
 *  - addSong(Song):         O(h) -> O(log n) promedio, O(n) peor caso
 *                            (árbol degenerado) + O(n) reconstrucción de caché
 *  - removeSong(Song):       igual que addSong
 *  - contains (uso interno): O(h)
 *  - recorrido inorden completo: O(n)
 *  - next()/previous():      O(1) gracias a la caché de navegación
 *  - getAllSongs():          O(n) (recorrido inorden fresco)
 */
public class BinarySearchTree implements PlaybackMode {

    private class Node {
        Song data;
        Node left;
        Node right;

        Node(Song data) {
            this.data = data;
        }
    }

    private Node root;
    private int size;

    // --- Caché del recorrido inorden, usada para next()/previous() ---
    private List<Song> inorderCache;
    private int currentIndex;

    public BinarySearchTree() {
        this.root = null;
        this.size = 0;
        this.inorderCache = new ArrayList<>();
        this.currentIndex = -1;
    }

    /** Compara dos canciones por título (orden alfabético, ignorando mayúsculas). */
    private int comparar(Song a, Song b) {
        return a.getTitle().compareToIgnoreCase(b.getTitle());
    }

    // =====================================================================
    // INSERCIÓN
    // =====================================================================

    @Override
    public void addSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("No se puede agregar una canción nula al árbol.");
        }
        root = insertarRecursivo(root, song);
        size++;
        reconstruirCache();
    }

    private Node insertarRecursivo(Node nodoActual, Song song) {
        if (nodoActual == null) {
            return new Node(song);
        }
        int comparacion = comparar(song, nodoActual.data);
        if (comparacion < 0) {
            nodoActual.left = insertarRecursivo(nodoActual.left, song);
        } else {
            // comparacion >= 0: empates (mismo título) van a la derecha por convención
            nodoActual.right = insertarRecursivo(nodoActual.right, song);
        }
        return nodoActual;
    }

    // =====================================================================
    // ELIMINACIÓN
    // =====================================================================

    @Override
    public boolean removeSong(Song song) {
        if (song == null || !contains(song)) {
            return false;
        }
        root = eliminarRecursivo(root, song);
        size--;
        reconstruirCache();
        return true;
    }

    private Node eliminarRecursivo(Node nodoActual, Song song) {
        if (nodoActual == null) {
            return null;
        }

        int comparacion = comparar(song, nodoActual.data);
        if (comparacion < 0) {
            nodoActual.left = eliminarRecursivo(nodoActual.left, song);
        } else if (comparacion > 0) {
            nodoActual.right = eliminarRecursivo(nodoActual.right, song);
        } else if (!nodoActual.data.equals(song)) {
            // Mismo título, pero distinta canción real (título+artista
            // distintos según Song.equals). Como los empates siempre se
            // insertan a la derecha, la canción buscada -si existe- está ahí.
            nodoActual.right = eliminarRecursivo(nodoActual.right, song);
        } else {
            // Nodo exacto encontrado. Tres casos clásicos de eliminación en BST:
            if (nodoActual.left == null) {
                return nodoActual.right;
            }
            if (nodoActual.right == null) {
                return nodoActual.left;
            }
            // Dos hijos: se reemplaza por el sucesor inorden (mínimo del
            // subárbol derecho) y se elimina el sucesor de su posición original.
            Song sucesor = encontrarMinimo(nodoActual.right);
            nodoActual.data = sucesor;
            nodoActual.right = eliminarRecursivo(nodoActual.right, sucesor);
        }
        return nodoActual;
    }

    private Song encontrarMinimo(Node nodo) {
        while (nodo.left != null) {
            nodo = nodo.left;
        }
        return nodo.data;
    }

    // =====================================================================
    // BÚSQUEDA (uso interno)
    // =====================================================================

    private boolean contains(Song song) {
        return containsRecursivo(root, song);
    }

    private boolean containsRecursivo(Node nodoActual, Song song) {
        if (nodoActual == null) {
            return false;
        }
        int comparacion = comparar(song, nodoActual.data);
        if (comparacion == 0) {
            if (nodoActual.data.equals(song)) {
                return true;
            }
            return containsRecursivo(nodoActual.right, song);
        }
        return comparacion < 0
                ? containsRecursivo(nodoActual.left, song)
                : containsRecursivo(nodoActual.right, song);
    }

    // =====================================================================
    // RECORRIDO INORDEN (produce el orden alfabético)
    // =====================================================================

    private List<Song> recorridoInorden() {
        List<Song> resultado = new ArrayList<>();
        recorridoInordenRecursivo(root, resultado);
        return resultado;
    }

    private void recorridoInordenRecursivo(Node nodoActual, List<Song> resultado) {
        if (nodoActual == null) {
            return;
        }
        recorridoInordenRecursivo(nodoActual.left, resultado);
        resultado.add(nodoActual.data);
        recorridoInordenRecursivo(nodoActual.right, resultado);
    }

    private void reconstruirCache() {
        inorderCache = recorridoInorden();
        if (inorderCache.isEmpty()) {
            currentIndex = -1;
        } else if (currentIndex >= inorderCache.size()) {
            currentIndex = inorderCache.size() - 1;
        } else if (currentIndex < 0) {
            currentIndex = 0;
        }
    }

    // =====================================================================
    // NAVEGACIÓN (avanzar / retroceder) — NO destructiva
    // =====================================================================

    @Override
    public Song next() {
        if (inorderCache.isEmpty()) {
            return null;
        }
        if (currentIndex < inorderCache.size() - 1) {
            currentIndex++;
        }
        // El Modo 3 no es circular (a diferencia del Modo 1): al llegar
        // al final simplemente se queda en la última canción.
        return inorderCache.get(currentIndex);
    }

    @Override
    public Song previous() {
        if (inorderCache.isEmpty()) {
            return null;
        }
        if (currentIndex > 0) {
            currentIndex--;
        }
        return inorderCache.get(currentIndex);
    }

    @Override
    public Song getCurrentSong() {
        if (currentIndex < 0 || currentIndex >= inorderCache.size()) {
            return null;
        }
        return inorderCache.get(currentIndex);
    }

    // =====================================================================
    // CONSULTAS GENERALES
    // =====================================================================

    @Override
    public List<Song> getAllSongs() {
        // Recorrido fresco: garantiza reflejar el estado real del árbol,
        // sin depender de que la caché esté sincronizada.
        return recorridoInorden();
    }

    @Override
    public String getModeName() {
        return "Alfabético (Árbol Binario de Búsqueda)";
    }

    /** @return la altura del árbol (útil para explicar el balance en la sustentación). */
    public int altura() {
        return alturaRecursiva(root);
    }

    private int alturaRecursiva(Node nodo) {
        if (nodo == null) {
            return -1;
        }
        return 1 + Math.max(alturaRecursiva(nodo.left), alturaRecursiva(nodo.right));
    }
}
