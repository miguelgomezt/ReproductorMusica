package structures;

import model.Song;
import ui.SongLibraryPanel;

import java.util.ArrayList;
import java.util.List;


public class SimpleQueue implements PlaybackMode {








    @Override
    public Song getCurrentSong() {
        return null;
    }



    private static class Node{
        private final Song song;
        private Node next;
        public Node(Song song){
            this.song = song;
        }
    }
    private Node head; ///Primer nodo (Proximo a reproducirse)
    private Node tail; ///Ultimo nodo (Donde se agregan los nuevos)
    private int size;  ///cantidad de canciones
    private Song current; ///Cancion reproducida recientemente




    ///Agregamos una cancion al final de la cola.
    /// Complejidad: 0(1) como ya se tiene la referencia
    /// No hace falta recorrer la cola
    @Override
    public void addSong (Song song){
        if(song == null){
            throw new IllegalArgumentException("No se puede agregar una cancion nula");

        }
        Node newNode = new Node(song);
        if(head == null) {
            head = newNode;
            tail = newNode;
        }else  {
            ///Se conecta al final y "tail" se actualiza.
            ///Apunta al nuevo ultimo
            tail.next = newNode;
            tail = newNode;

        }
        size++;
    }

    ///Elimina cancion
    /// Diferente del next que elimina siempre el primero
    /// Complejidad 0(n) en el peor de los casos hay que recorrer toda la cola.
    @Override
    public boolean removeSong(Song song) {
        if (song == null || head == null) {
            return false;
        }
        Node previous = null;
        Node curr = head;
        while (curr != null) {
            if (curr.song.equals(song)) {
                if (previous == null) {
                    head = curr.next;
                } else {
                    previous.next = curr.next;
                }
                if (curr == tail) {
                    tail = previous;
                }
                curr.next = null;
                size--;
                return true;
            }
            previous = curr;
            curr = curr.next;
        }
        return false;
    }


    ///Avanza a la siguiente cancion
    /// Trae la cancion que esta al frente de la cola
    /// Complejidad 0(1)
    @Override
    public Song next() {
        if (head == null) {
            current = null;
            return null;
        }
        Node oldHead = head;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        oldHead.next = null;
        size--;
        current = oldHead.song;
        return current;
    }

    ///No permite regresar a diferencia de las otras estructuras
    @Override
    public Song previous() {
        throw new UnsupportedOperationException("La cola no permite regresar a canciones anteriores");

    }
    @Override
    public List<Song> getAllSongs() {
        List<Song> all = new ArrayList<>(size);
        Node curr = head;
        while (curr != null) {
            all.add(curr.song);
            curr = curr.next;
        }
        return all;
    }

    @Override
    public String getModeName() {
        return "Orden de llegada";
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public int size() {
        return size;
    }
}
