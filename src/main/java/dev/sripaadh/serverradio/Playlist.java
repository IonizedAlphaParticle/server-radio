package dev.sripaadh.serverradio;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private final List<String> songs = new ArrayList<>();

    public void add(String song){
        songs.add(song);
    }

    public int size(){
        return songs.size();
    }

    public String next(){
        if (songs.isEmpty()){
            return null;
        }
        else {
            return songs.remove(0);
        }
    }

}
