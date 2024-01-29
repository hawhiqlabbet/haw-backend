package com.example.springboot.models;

// GameLobby.java
import ch.qos.logback.core.joran.sanity.Pair;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class GameLobby {
    private String host;
    private int timeout;
    private String gameChoice;
    private List<Player> players;



    public GameLobby(String host, int timeout, String gameChoice) {
        this.host = host;
        this.timeout = timeout;
        this.gameChoice = gameChoice;
        this.players = new ArrayList<>();
    }

    public String getHost() {
        return host;
    }

    public String getGameChoice(){
        return this.gameChoice;
    }

    public void addPlayer(String player, String imageUrl) {
        players.add(new Player(player, imageUrl));
    }

    public void removePlayer(String username) {
        players.removeIf(player -> player.getUsername().equals(username));
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public boolean playerExists(String username) {
        for (Player player : players) {
            if(username.equals(player.getUsername())) {
                return true;
            }
        }
        return false;
    }

    // Other getters and methods as needed
}
