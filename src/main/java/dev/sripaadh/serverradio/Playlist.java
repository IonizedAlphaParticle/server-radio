package dev.sripaadh.serverradio;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private final List<String> songs = new ArrayList<>();
    private int index = 0;
    private boolean loop = false;

    public void add(String song){
        songs.add(song);
    }

    public int size(){
        return songs.size();
    }

    public boolean toggleLoop(){
        loop = !loop;
        return loop;
    }

    public String next(){
        if (songs.isEmpty()){
            return null;
        }

        if (index >= songs.size()){
            if (loop){
              index = 0;
            }
            else{
                index = 0;
                return null;
            }
        }

        String song = songs.get(index);
        index++;
        return song;
    }

}
