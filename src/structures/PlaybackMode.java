package structures;

import model.Song;

import java.util.List;

public interface PlaybackMode {

    void addSong(Song song);

    boolean removeSong(Song song);

    Song next();




    Song previous();

    Song getCurretSong();


    List<Song> getAllSongs();

    String getModeName();
}
