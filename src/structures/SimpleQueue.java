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
    private Node head;
    private Node tail;
    private int size;
    private Song current;

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
            tail.next = newNode;
            tail = newNode;

        }
        size++;
    }
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
