package structures;

import model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CircularDoubleLinkedList implements PlaybackMode{
    private class Node {
        Song song;
        Node next;
        Node prev;

        Node(Song song) {
            this.song = song;
        }
    }

    private Node current; ///Nodo de la cancion actual reproduciendo.
    private int size;

    @Override
    public void addSong(Song song){
        Node newNode = new Node(song);
        if(current == null){
            newNode.next = newNode;
            newNode.prev = newNode;
            current = newNode;
        }else{
            Node last = current.prev;
            last.next = newNode;
            newNode.prev = last;
            newNode.next = current;
            current.prev = newNode;
        }
        size++;
    }

    @Override
    public boolean removeSong(Song song){
        if(current == null) return false;
        Node start = current;
        Node node = current;
        do{
            if(node.song.equals(song)){
                if(size == 1){
                    current = null;
                }else{
                    node.prev.next = node.next;
                    node.next.prev = node.prev;
                    if(current == node){
                        current = node.next;
                    }
                }
                size--;
                return true;
            }
            node = node.next;
        }while (node!= start);
        return false;
    }

    @Override
    public Song next(){
        if(current == null) return null;
        current =  current.next;
        return current.song;
    }

    @Override
    public Song previous(){
        if(current == null) return null;
        current = current.prev;
        return current.song;
    }

    @Override
    public Song getCurrentSong(){
        return current != null ? current.song : null;
    }

    @Override
    public List<Song> getAllSongs(){
        List<Song> songs = new ArrayList<>();
        if(current == null) return songs;

        Node node = current;
        do{
            songs.add(node.song);
            node = node.next;
        }while(node != current);

        return songs;
    }

    ///Reordenar aleatoriamente las canciones
    public void shuffle(){
        List<Song> songs = getAllSongs();
        Collections.shuffle(songs);
        current = null;
        size = 0;
        for(Song song : songs){
            addSong(song);
        }
    }

    @Override
    public String getModeName(){
        return "Aleatorio (Lista circular)";
    }
    public int size(){
        return size;
    }

}
