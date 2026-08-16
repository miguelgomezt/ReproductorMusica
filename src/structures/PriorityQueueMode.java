package structures;

import model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * PriorityQueueMode: max-heap binario (array-based) implementado desde
 * cero. Funcionalidad EXTRA de creatividad, NO exigida por el documento:
 * navega la biblioteca ordenada por calificación del usuario (0-100), de
 * mayor a menor. Implementa structures.PlaybackMode.
 *
 * CAMBIO DE DISEÑO IMPORTANTE respecto a la primera versión (por qué)
 * ---------------------------------------------------------------------
 * Originalmente pensé next() como una extracción destructiva (poll), al
 * estilo de la Cola Simple del Modo 2: la canción "reproducida" salía de
 * la estructura. Pero MusicLibraryManager.setMode() transfiere la
 * biblioteca completa entre modos usando getAllSongs(), y espera que
 * TODOS los modos conserven sus canciones mientras se navega (igual que
 * el Modo 1 y el Modo 3). Si next() aquí borrara canciones, cambiar de
 * "Por calificación" a otro modo y volver haría perder canciones.
 *
 * Por eso, igual que en BinarySearchTree, se separan dos cosas:
 *  - El HEAP (this.heap): estructura de datos real, usada para que
 *    addSong/removeSong sean eficientes (O(log n) / O(n)).
 *  - Una CACHÉ ORDENADA (this.sortedCache): lista de mayor a menor
 *    calificación, reconstruida tras cada cambio estructural mediante
 *    HEAP SORT sobre una copia. next()/previous() solo mueven un índice
 *    sobre esa caché — NO destructivo, igual que el árbol.
 *
 * COMPLEJIDAD TEMPORAL (para sustentación)
 * ---------------------------------------------------------------------
 *  - addSong(Song):        O(log n) en el heap (siftUp)
 *                           + O(n log n) por reconstruir la caché ordenada
 *  - removeSong(Song):      O(n) buscar en el heap + O(log n) reparar
 *                           + O(n log n) por reconstruir la caché
 *  - next()/previous():     O(1) (índice sobre la caché ya ordenada)
 *  - getCurrentSong():      O(1)
 *  - getAllSongs():         O(1) (ya está ordenada en sortedCache)
 *
 *  Nota honesta para sustentación: el costo O(n log n) por cada
 *  add/remove es más alto que en el árbol (O(n)) porque aquí se hace un
 *  heap-sort completo de la caché en cada cambio, priorizando código
 *  simple y reutilizable sobre eficiencia máxima. Para el tamaño de una
 *  biblioteca de canciones esto es imperceptible.
 */
public class PriorityQueueMode implements PlaybackMode {

    /** Heap real: array-based, heap.get(0) es siempre la canción con mayor calificación. */
    private final List<Song> heap;

    /** Caché de navegación: todas las canciones ordenadas de mayor a menor calificación. */
    private List<Song> sortedCache;
    private int currentIndex;

    public PriorityQueueMode() {
        this.heap = new ArrayList<>();
        this.sortedCache = new ArrayList<>();
        this.currentIndex = -1;
    }

    /** "a tiene más prioridad que b" si a.getRating() > b.getRating(). */
    private int comparar(Song a, Song b) {
        return Integer.compare(a.getRating(), b.getRating());
    }

    // =====================================================================
    // ARITMÉTICA DE ÍNDICES DEL HEAP
    // =====================================================================

    private int indicePadre(int i) {
        return (i - 1) / 2;
    }

    private int indiceHijoIzquierdo(int i) {
        return 2 * i + 1;
    }

    private int indiceHijoDerecho(int i) {
        return 2 * i + 2;
    }

    private void intercambiar(int i, int j) {
        Song temporal = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temporal);
    }

    // =====================================================================
    // INSERCIÓN
    // =====================================================================

    @Override
    public void addSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("No se puede agregar una canción nula.");
        }
        heap.add(song);
        siftUp(heap.size() - 1);
        reconstruirCacheOrdenada();
    }

    private void siftUp(int i) {
        while (i > 0) {
            int padre = indicePadre(i);
            if (comparar(heap.get(i), heap.get(padre)) > 0) {
                intercambiar(i, padre);
                i = padre;
            } else {
                break;
            }
        }
    }

    private void siftDown(int i) {
        int tamano = heap.size();
        while (true) {
            int izquierdo = indiceHijoIzquierdo(i);
            int derecho = indiceHijoDerecho(i);
            int mayor = i;

            if (izquierdo < tamano && comparar(heap.get(izquierdo), heap.get(mayor)) > 0) {
                mayor = izquierdo;
            }
            if (derecho < tamano && comparar(heap.get(derecho), heap.get(mayor)) > 0) {
                mayor = derecho;
            }
            if (mayor == i) {
                break;
            }
            intercambiar(i, mayor);
            i = mayor;
        }
    }

    // =====================================================================
    // ELIMINACIÓN ARBITRARIA (necesaria porque el usuario puede eliminar
    // cualquier canción de la biblioteca, no solo la que está sonando)
    // =====================================================================

    @Override
    public boolean removeSong(Song song) {
        int indice = indiceDe(song);
        if (indice == -1) {
            return false;
        }
        int ultimoIndice = heap.size() - 1;
        intercambiar(indice, ultimoIndice);
        heap.remove(ultimoIndice);

        if (indice < heap.size()) {
            repararHeapEn(indice);
        }
        reconstruirCacheOrdenada();
        return true;
    }

    private int indiceDe(Song song) {
        if (song == null) {
            return -1;
        }
        for (int i = 0; i < heap.size(); i++) {
            if (heap.get(i).equals(song)) {
                return i;
            }
        }
        return -1;
    }

    private void repararHeapEn(int indice) {
        int padre = indicePadre(indice);
        if (indice > 0 && comparar(heap.get(indice), heap.get(padre)) > 0) {
            siftUp(indice);
        } else {
            siftDown(indice);
        }
    }

    // =====================================================================
    // RECONSTRUCCIÓN DE LA CACHÉ ORDENADA (algoritmo de HEAP SORT)
    // =====================================================================

    /**
     * Reconstruye sortedCache ordenando una COPIA del heap de mayor a
     * menor calificación, sin modificar el heap real. Técnica: heapify
     * de la copia en O(n) + extracción repetida de la raíz (heap-sort),
     * cada extracción O(log n) => O(n log n) total.
     */
    private void reconstruirCacheOrdenada() {
        List<Song> copia = new ArrayList<>(heap);
        heapify(copia);

        List<Song> resultado = new ArrayList<>();
        while (!copia.isEmpty()) {
            resultado.add(extraerRaizDe(copia));
        }
        this.sortedCache = resultado;

        if (sortedCache.isEmpty()) {
            currentIndex = -1;
        } else if (currentIndex >= sortedCache.size()) {
            currentIndex = sortedCache.size() - 1;
        } else if (currentIndex < 0) {
            currentIndex = 0;
        }
    }

    /** Convierte una lista arbitraria en un heap válido en O(n) (bottom-up heapify). */
    private void heapify(List<Song> lista) {
        for (int i = lista.size() / 2 - 1; i >= 0; i--) {
            siftDownEn(lista, i);
        }
    }

    private Song extraerRaizDe(List<Song> lista) {
        Song raiz = lista.get(0);
        int ultimo = lista.size() - 1;
        lista.set(0, lista.get(ultimo));
        lista.remove(ultimo);
        if (!lista.isEmpty()) {
            siftDownEn(lista, 0);
        }
        return raiz;
    }

    /** Misma lógica que siftDown(), pero operando sobre una lista auxiliar cualquiera. */
    private void siftDownEn(List<Song> lista, int i) {
        int tamano = lista.size();
        while (true) {
            int izquierdo = 2 * i + 1;
            int derecho = 2 * i + 2;
            int mayor = i;

            if (izquierdo < tamano && comparar(lista.get(izquierdo), lista.get(mayor)) > 0) {
                mayor = izquierdo;
            }
            if (derecho < tamano && comparar(lista.get(derecho), lista.get(mayor)) > 0) {
                mayor = derecho;
            }
            if (mayor == i) {
                break;
            }
            Song temporal = lista.get(i);
            lista.set(i, lista.get(mayor));
            lista.set(mayor, temporal);
            i = mayor;
        }
    }

    // =====================================================================
    // NAVEGACIÓN — NO destructiva (ver nota de diseño arriba)
    // =====================================================================

    @Override
    public Song next() {
        if (sortedCache.isEmpty()) {
            return null;
        }
        if (currentIndex < sortedCache.size() - 1) {
            currentIndex++;
        }
        return sortedCache.get(currentIndex);
    }

    @Override
    public Song previous() {
        if (sortedCache.isEmpty()) {
            return null;
        }
        if (currentIndex > 0) {
            currentIndex--;
        }
        return sortedCache.get(currentIndex);
    }

    @Override
    public Song getCurrentSong() {
        if (currentIndex < 0 || currentIndex >= sortedCache.size()) {
            return null;
        }
        return sortedCache.get(currentIndex);
    }

    // =====================================================================
    // CONSULTAS GENERALES
    // =====================================================================

    @Override
    public List<Song> getAllSongs() {
        return new ArrayList<>(sortedCache);
    }

    @Override
    public String getModeName() {
        return "Por calificación (Priority Queue)";
    }
}
