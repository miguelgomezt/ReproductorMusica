package structures;

import model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CircularDoubleLinkedList implements PlaybackMode{
    ///Nodo interno de la lista. Se encapsula
    private class Node {
        Song song;
        Node next; ///Nodo siguiente
        Node prev; ///Nodo anterior

        Node(Song song) {
            this.song = song;
        }
    }

    private Node current; ///Nodo de la cancion actual reproduciendo.
    private int size; ///Cantidad de canciones



    ///Agregamos una cancion a la lista. Complejidad 0(1)
    ///No hay necesidad de recorrer la lista
    @Override
    public void addSong(Song song){
        Node newNode = new Node(song);
        if(current == null){
            newNode.next = newNode;
            newNode.prev = newNode;
            current = newNode;
        }else{

            ///Se inserta el nuevo nodo justo antes de current
            ///al final de la ronda, es decir current.prev es el ultimo)

            Node last = current.prev;
            last.next = newNode;
            newNode.prev = last;
            newNode.next = current;
            current.prev = newNode;
        }
        size++;
    }

    ///Elimina la primera canción encontrar, song.equals
    /// En el peor de los casos hay que recorrer toda la lista
    @Override
    public boolean removeSong(Song song){
        if(current == null) return false;
        Node start = current;
        Node node = current;

        ///Recorremos la lista circular, no hay un null para detenerse
        ///Se compara con el punto de partida para saber cuando dimos la vuelta
        do{
            if(node.song.equals(song)){
                if(size == 1){
                    ///Unico nodo
                    current = null;
                }else{

                    ///Saltamos el nodo, conectamos el anterior
                    ///Sacandolo de la ronda.
                    node.prev.next = node.next;
                    node.next.prev = node.prev;

                    ///Si estabamos eliminando el nodo actual,
                    ///Movemos el current al siguiente para no perder la referencia
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


    ///Avanza a la siguiente cancion
    /// Complejidad: 0(1) solo se mueve el current un paso
    @Override
    public Song next(){
        if(current == null) return null;
        current =  current.next;
        return current.song;
    }

    ///Retrocedemos a la cancion anterior
    /// Igual que next pero en direccion contraria, gracias a prev
    /// Ventaja de ser circular doble
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


    ///Devuelve todas las canciones de la lista
    /// Empieca desde current
    /// Complejidad 0(n) ya que recorre cada nodo exactamente una vez
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

        ///Se vuelve a odernar la lista, de forma aleatoria
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
