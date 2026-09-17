package com.example.album.model;

import java.io.Serializable;
import java.util.List;

public final class Album implements Serializable {
    private final String code;
    private final String artist;
    private final String title;
    private final List<Track> tracks;

    public Album(String code, String artist, String title, List<Track> tracks) {
        this.code = code;
        this.artist = artist;
        this.title = title;
        this.tracks = List.copyOf(tracks);
    }

    public String getCode() {
        return code;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getDisplayName() {
        return artist + " - " + title;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public static final class Track implements Serializable {
        private final String title;
        private final String format;

        public Track(String title, String format) {
            this.title = title;
            this.format = format;
        }

        public String getTitle() {
            return title;
        }

        public String getFormat() {
            return format;
        }
    }
}
