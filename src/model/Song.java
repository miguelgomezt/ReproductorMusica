package model;

import java.util.Objects;

public class Song {

    private String title;
    private String artist;
    private String album;
    private int durationInSeconds;
    private String genre;
    private int releaseYear;
    private int releaseMonth;
    private int rating;
    private String coverImage;

    public Song(String title, String artist, String album, int durationInSeconds,
                String genre, int releaseYear) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.durationInSeconds = durationInSeconds;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.rating = 0;
        this.coverImage = null;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {return artist;}
    public void setArtist(String artist) {this.artist = artist;}

    public String getAlbum() {return album;}
    public void setAlbum(String album) {this.album = album;}

    public int getDurationInSeconds() {return durationInSeconds;}
    public void setDurationInSeconds(int durationInSeconds) {
        if(durationInSeconds < 0) {
            throw new IllegalArgumentException("la duración no puded ser negativa");
        }
        this.durationInSeconds = durationInSeconds;
    }

    public String getGenre() {return genre;}
    public void setGenre(String genre) {this.genre = genre;}

    public int getReleaseYear() {return releaseYear;}
    public void setReleaseYear(int releaseYear) {this.releaseYear = releaseYear;}

    public int getRating() {return rating;}



    ///Establecemos la calificacion del usuario.

    public void setRating(int rating){
        if(rating < 0 || rating > 100){
            throw new IllegalArgumentException("La calificación debe estar entre 0 y 100");
        }
        this.rating = rating;
    }

    public String getCoverImage() {return coverImage;}
    public void setCoverImage(String coverImage) {this.coverImage = coverImage;}

    public String getFormattedDuration(){
        int minutes = durationInSeconds/60;
        int seconds = durationInSeconds%60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return title + " "  + artist + " ( " + getFormattedDuration() + " )";
    }

    ///Canciones iguales si tienen el mismo nombre y el mismo artista.

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Song)) return false;
        Song song = (Song) o;
        return title.equalsIgnoreCase(song.title) && artist.equalsIgnoreCase(song.artist);
    }
    @Override
    public int hashCode() {
        return Objects.hash(title.toLowerCase(), artist.toLowerCase());
    }
}
